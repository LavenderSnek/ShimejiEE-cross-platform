package com.group_finity.mascotnative.win;

/**
 * The fix is so simple but it has taken us all 5 years. *facepalm*
 *
 * @author TigerHix
 */
public enum OsArchitecture {

    x86(20), x86_64(24);

    private final int bitmapSize;

    OsArchitecture(int bitmapSize) {
        this.bitmapSize = bitmapSize;
    }

    public int getBitmapSize() {
        return bitmapSize;
    }

}
