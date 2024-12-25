# Maven is getting unusable for native
# (and gradle indexing is too slow on my computer rip)
# This is a glorified makefile

import time
from pathlib import Path
import subprocess
import shutil
from typing import Literal
import platform

BuildConfig = Literal['Debug', 'Release', 'RelWithDebInfo']

def cmake_generate(src_dir: Path, build_dir: Path, lib_output_dir: Path, extra_args: None | list[str] = None):
    cmd = [
        'cmake',
        '-G', 'Ninja Multi-Config',
        f'-DCMAKE_LIBRARY_OUTPUT_DIRECTORY={lib_output_dir.absolute()}',
        '-S', f'{src_dir}',
        '-B', f'{build_dir}'
    ]

    cmd += [] if extra_args is None else extra_args
    subprocess.run(cmd, check=True)


def cmake_build(build_dir: Path, config: BuildConfig, extra_args: None | list[str] = None):
    cmd = ['cmake', '--build', f'{build_dir}', '--config', config]

    cmd += [] if extra_args is None else extra_args
    subprocess.run(cmd, check=True)


def jextract(package: str, header: Path, jextract_bin: str = 'jextract'):
    subprocess.run([jextract_bin, '--output', 'src/main/java', '--target-package', package, f'{header}'], check=True)

#==========

PROJECT_NAME = 'ShimejiEE'
MVN_BUILD_DIR = Path('./target')
MVN_JAR_PATH = MVN_BUILD_DIR / f"{PROJECT_NAME}.jar"

BUILD_DIR = Path('./build')
NATIVE_BUILD_DIR = BUILD_DIR / 'native'
NATIVE_LIB_DIR = NATIVE_BUILD_DIR / 'lib'

def build_java(no_panama: bool, skip_tests: bool):
    cmd = ['mvn', 'package', '-T4']
    if no_panama: cmd.append('-Pno_panama')
    if skip_tests: cmd.append('-Dmaven.test.skip=true')
    subprocess.run(cmd, check=True)

def copy_ext_to_install(lib_build_dir: Path, install_dir: Path):
    if not MVN_JAR_PATH.is_file(): raise AssertionError(f'jar does not exist: {MVN_JAR_PATH.absolute()}')

    install_dir.mkdir(parents=True, exist_ok=True)

    shutil.copytree('ext-resources', install_dir, dirs_exist_ok=True)

    if lib_build_dir.is_dir():
        shutil.copytree(lib_build_dir, install_dir / 'lib', dirs_exist_ok=True)

    shutil.copy2('LICENSE.md', install_dir)
    shutil.copy2('README.md', install_dir)
    shutil.copy2(MVN_JAR_PATH, install_dir)

def clean_panama_bindings():
    panama_bindings = Path('src/main/java/com/group_finity/mascotnative/panama/bindings/')
    if panama_bindings.is_dir():
        shutil.rmtree(panama_bindings)

def gen_panama_bindings(jextract_bin: str = 'jextract'):
    clean_panama_bindings()

    bind_pkg = 'com.group_finity.mascotnative.panama.bindings'
    interface_base = Path('src/main/native/panama/interfaces')

    jextract(f'{bind_pkg}.render', interface_base / 'NativeRenderer.h', jextract_bin=jextract_bin)
    jextract(f'{bind_pkg}.environment', interface_base / 'NativeEnvironment.h', jextract_bin=jextract_bin)

def build_macjni(config: BuildConfig):
    cmake_generate(Path('src/main/native/macjni'), NATIVE_BUILD_DIR / 'build-macjni', NATIVE_LIB_DIR)
    cmake_build(NATIVE_BUILD_DIR / 'build-macjni', config)

def build_panama(config: BuildConfig):
    cmake_generate(Path('src/main/native/panama'), NATIVE_BUILD_DIR / 'build-panama', NATIVE_LIB_DIR)
    cmake_build(NATIVE_BUILD_DIR / 'build-panama', config)

# deletes everything including the native directories and bindings
def full_clean():
    subprocess.run(['mvn', 'clean'], check=True)
    if BUILD_DIR.is_dir():
        shutil.rmtree(BUILD_DIR)

    clean_panama_bindings()

def _main():
    import argparse

    is_mac = platform.system() == 'Darwin'

    parser = argparse.ArgumentParser(description='Builds shimeji with native directories')

    parser.add_argument('--clean', default=False, action='store_true', help='Delete everything an run a fresh build')
    parser.add_argument('--no-build', default=False, action='store_true', help="Doesn't run the build. (use for clean only conf))")
    parser.add_argument('--release', default=False, action='store_true', help='Enable release profile for native')
    parser.add_argument('--skip-tests', default=False, action='store_true', help='Skip running tests')
    parser.add_argument('--no-panama', default=False, action='store_true', help='Dont build panama backends')
    parser.add_argument('--jextract', required=False, help='Jextract binary path')

    if is_mac:
        parser.add_argument('--no-macjni', default=False, action='store_true', help='Dont build macjni native lib')

    args = parser.parse_args()

    if args.clean:
        print('[Running Full Clean]')
        full_clean()

    if args.no_build:
        return

    jextract_binary = 'jextract' if args.jextract is None else args.jextract
    config: BuildConfig = 'Release' if args.release else 'Debug'

    PRFX = '\n' + ('─'*50)

    # actual build
    if not args.no_panama:
        print(PRFX + '\n[Generating Panama Bindings]')
        gen_panama_bindings(jextract_binary)

        print(PRFX + '\n[Building Panama Libraries]')
        build_panama(config)

    print(PRFX + '\n[Building Java]')
    build_java(args.no_panama, args.skip_tests) # before jni build (header generation)

    if is_mac and not args.no_macjni:
        print(PRFX + '\n[Buildings Mac JNI Libraries]')
        build_macjni(config)

    print(PRFX + '\n[Creating Install Dir]')
    copy_ext_to_install(NATIVE_LIB_DIR / config, BUILD_DIR / PROJECT_NAME)
    print(f"Install created at: {BUILD_DIR / PROJECT_NAME}")


if __name__ == '__main__':
    start_time = time.time()
    _main()
    print('\n\nFINISHED')
    print(f"Time: {(time.time() - start_time):0.2f} s")
