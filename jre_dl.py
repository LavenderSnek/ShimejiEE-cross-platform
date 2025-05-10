import requests
import os
import subprocess
import tempfile
import sys

# Bash script that happened to need json

def mac_jre_url(arch):
    return f"https://api.adoptium.net/v3/assets/latest/23/hotspot?architecture={arch}&image_type=jre&os=mac&vendor=eclipse"

def dl_jre(url, extract_out):
    os.makedirs(extract_out, exist_ok=True)
    response = requests.get(url)
    data = response.json()
    download_url = data[0]['binary']['package']['link']

    with tempfile.NamedTemporaryFile() as temp_tar:
        subprocess.run(['curl', '-L', '-o', temp_tar.name, download_url], check=True)
        subprocess.run(['tar', '-xzf', temp_tar.name, '-C', extract_out], check=True)


if __name__ == '__main__':
    if not os.path.isdir('build/ShimejiEE'):
        print("build/ShimejiEE does not exist. Run build script first.")
        exit(1)

    subprocess.run(['cp', '-a', 'build/ShimejiEE', 'build/ShimejiEE-mac-jre'], check=True)

    dl_jre(mac_jre_url('aarch64'), 'build/ShimejiEE-mac-jre/jre/silicon')
    dl_jre(mac_jre_url('x64'), 'build/ShimejiEE-mac-jre/jre/intel')


