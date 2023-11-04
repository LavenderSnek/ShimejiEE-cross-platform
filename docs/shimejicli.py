
import argparse
import os
import subprocess
import sys


P_PREFS = "com.group_finity.mascot.prefs"

if __name__ == "__main__":
    # path to shimejiEE jar
    SHIME_JAR = os.path.join(os.path.dirname(sys.argv[0]), "../target/ShimejiEE/ShimejiEE.jar")
    SHIME_JAR = os.path.abspath(SHIME_JAR)

    # could be placed later to allow --help, but this feels like a better alert
    if not os.path.isfile(SHIME_JAR):
        print(f"jar path does not exist (edit cli script to change): {SHIME_JAR}\n", file=sys.stderr)
        exit()

    print(f"jar path (edit cli script to change): {SHIME_JAR}\n", file=sys.stderr)

    javaCmd = [ "java",
        "-Dsun.java2d.metal=true"
    ]

    parser = argparse.ArgumentParser(description='Runs shimeji with the given settings. Its not really stable, but good enough for quick run configs.')

    parser.add_argument('dirType', choices={'pf', 'img'}, help='dirType: program folder or img dir')
    parser.add_argument('dirPath', help='Path to pf/img dir')
    parser.add_argument('--mono', default=False, action='store_true', help='Mono imageset mode: searches for sprite files directly in img dir')

    parser.add_argument('--select', '-s', required=False, help='Select image set', action='extend', nargs='*')

    parser.add_argument('--native', required=False, help='Native implementation package name')

    parser.add_argument('--scale', type=float, default=1.0, help='Global scaling')
    parser.add_argument('--lanc', default=False, action='store_true', help='Logical anchors: images with same name can have copies with multiple anchors')
    parser.add_argument('--asym', default=False, action='store_true', help='Asymmetry name scheme: do not ignore -r suffix for asymmetry')
    parser.add_argument('--pix', default=False, action='store_true', help='Pixel art scaling: use nearest neighbour scaling')
    parser.add_argument('--soundfix', default=False, action='store_true', help='Fix Relative Global Sound: Searches in local sound folder if xml uses relative paths to global folder')

    # works well enough
    parser.add_argument('--no-breed', default=False, action='store_true', help='Disallow shimeji to spawn other shimeji')
    parser.add_argument('--no-transient', default=False, action='store_true', help='Disallow Transient shimeji in addition to breed')
    parser.add_argument('--no-transform', default=False, action='store_true', help='Disable Transform actions')
    parser.add_argument('--no-throw', default=False, action='store_true', help='Disable window throwing')
    parser.add_argument('--no-sound', default=False, action='store_true', help='Disable sound')
    parser.add_argument('--no-bvtr', default=False, action='store_true', help='Disable behaviour name translation')
    parser.add_argument('--chooser', default=False, action='store_true', help='Show image set chooser on startup')
    parser.add_argument('--ignore-imp', default=False, action='store_true', help='Ignore imageset.properties in inner conf folder')

    args = parser.parse_args()

    pfOrImg = 'ProgramFolder' if args.dirType == 'pf' else 'ProgramFolder.img'
    javaCmd.append(f"-D{P_PREFS}.{pfOrImg}={args.dirPath}")
    javaCmd.append(f"-D{P_PREFS}.ProgramFolder.mono={args.mono}")

    if args.native is not None:
        javaCmd.append(f"-Dcom.group_finity.mascotnative={args.native}")

    if args.select is not None and len(args.select) != 0:
        import urllib.parse
        selStr = ""
        for s in args.select:
            urllib.parse.quote_plus(s) + '/'
        javaCmd.append(f"-D{P_PREFS}.ActiveShimeji={selStr}")

    javaCmd.append(f"-D{P_PREFS}.Scaling={args.scale}")
    javaCmd.append(f"-D{P_PREFS}.LogicalAnchors={args.lanc}")
    javaCmd.append(f"-D{P_PREFS}.AsymmetryNameScheme={args.asym}")
    javaCmd.append(f"-D{P_PREFS}.PixelArtScaling={args.pix}")
    javaCmd.append(f"-D{P_PREFS}.FixRelativeGlobalSound={args.soundfix}")

    javaCmd.append(f"-D{P_PREFS}.Breeding={not args.no_breed}")
    javaCmd.append(f"-D{P_PREFS}.Transients={not args.no_transient}")
    javaCmd.append(f"-D{P_PREFS}.Transformation={not args.no_transform}")
    javaCmd.append(f"-D{P_PREFS}.Throwing={not args.no_throw}")
    javaCmd.append(f"-D{P_PREFS}.Sounds={not args.no_sound}")
    javaCmd.append(f"-D{P_PREFS}.TranslateBehaviorNames={not args.no_bvtr}")
    javaCmd.append(f"-D{P_PREFS}.AlwaysShowShimejiChooser={args.chooser}")
    javaCmd.append(f"-D{P_PREFS}.IgnoreImagesetProperties={args.ignore_imp}")

    javaCmd.extend(["-jar", SHIME_JAR])

    subprocess.call(javaCmd)
