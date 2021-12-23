/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
#import "JdkCode.h"

// from: https://github.com/openjdk/jdk/blob/master/src/java.desktop/macosx/native/libawt_lwawt/awt/CImage.m
static void CImage_CopyArrayIntoNSImageRep
(jint *srcPixels, jint *dstPixels, int width, int height)
{
    int x, y;

    for (y = 0; y < height; y++) {
        for (x = 0; x < width; x++) {
            jint pix = srcPixels[x];
            jint a = (pix >> 24) & 0xff;
            jint r = (pix >> 16) & 0xff;
            jint g = (pix >>  8) & 0xff;
            jint b = (pix      ) & 0xff;
            dstPixels[x] = (b << 24) | (g << 16) | (r << 8) | a;
        }
        srcPixels += width;
        dstPixels += width;
    }
}

// from: https://github.com/openjdk/jdk/blob/master/src/java.desktop/macosx/native/libawt_lwawt/awt/CImage.m
NSBitmapImageRep* CImage_CreateImageRep
(JNIEnv *env, jintArray buffer, jint width, jint height) {
    NSBitmapImageRep* imageRep = [[[NSBitmapImageRep alloc] initWithBitmapDataPlanes:NULL
                                                                          pixelsWide:width
                                                                          pixelsHigh:height
                                                                       bitsPerSample:8
                                                                     samplesPerPixel:4
                                                                            hasAlpha:YES
                                                                            isPlanar:NO
                                                                      colorSpaceName:NSCalibratedRGBColorSpace
                                                                        bitmapFormat:NSBitmapFormatAlphaFirst
                                                                         bytesPerRow:width*4
                                                                        bitsPerPixel:32] autorelease];

    jint *imgData = (jint *)[imageRep bitmapData];
    if (imgData == NULL) return 0L;

    jint *src = (*env)->GetPrimitiveArrayCritical(env, buffer, NULL);
    if (src == NULL) return 0L;

    CImage_CopyArrayIntoNSImageRep(src, imgData, width, height);

    (*env)->ReleasePrimitiveArrayCritical(env, buffer, src, JNI_ABORT);

    return imageRep;
}

