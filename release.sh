
python3 jre_dl.py

mv build/ShimejiEE-mac-jre/jre/silicon/*/* build/ShimejiEE-mac-jre/jre/silicon
rmdir build/ShimejiEE-mac-jre/jre/silicon/*/

mv build/ShimejiEE-mac-jre/jre/intel/*/* build/ShimejiEE-mac-jre/jre/intel
rmdir build/ShimejiEE-mac-jre/jre/intel/*/

rm build/ShimejiEE/launcher
chmod +x build/ShimejiEE-mac-jre/launcher

ditto -c -k --sequesterRsrc build/ShimejiEE-mac-jre build/release/ShimejiEE-mac-jre.zip
ditto -c -k build/ShimejiEE build/release/ShimejiEE-no-jre.zip
