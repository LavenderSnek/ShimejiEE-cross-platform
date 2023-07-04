
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
    
    parser = argparse.ArgumentParser(description='Runs shimeji with the given settings.')
    parser.add_argument('dirType', choices={'pf', 'img'}, help='dirType: program folder or img dir')
    parser.add_argument('dirPath', help='Path to pf/img dir')
    parser.add_argument('--mono', default=False, action='store_true', help='Mono imageset mode: searches for imageset files directly in img dir')
    parser.add_argument('--native', required=False, help='Native implementation package name')
    parser.add_argument('--scale', type=float, default=1.0, help='Global scaling')
    parser.add_argument('--lanc', default=False, action='store_true', help='Logical anchors: images with same name can have copies with multiple anchors')
    parser.add_argument('--asym', default=False, action='store_true', help='Asymmetry name scheme: do not ignore -r suffix for asymmetry')
    parser.add_argument('--pix', default=False, action='store_true', help='Pixel art scaling: use nearest neighbour scaling')

    args = parser.parse_args()

    dsel = 'ProgramFolder' if args.dirType == 'pf' else 'ProgramFolder.img'
    javaCmd.append(f"-D{P_PREFS}.{dsel}={args.dirPath}")
    javaCmd.append(f"-D{P_PREFS}.ProgramFolder.mono={args.mono}")
    
    if args.native is not None:  
        javaCmd.append(f"-Dcom.group_finity.mascotnative={args.native}")
    
    javaCmd.append(f"-D{P_PREFS}.Scaling={args.scale}")
    javaCmd.append(f"-D{P_PREFS}.LogicalAnchors={args.lanc}")
    javaCmd.append(f"-D{P_PREFS}.AsymmetryNameScheme={args.asym}")
    javaCmd.append(f"-D{P_PREFS}.PixelArtScaling={args.pix}")

    javaCmd.extend(["-jar", SHIME_JAR,])

    subprocess.call(javaCmd)
