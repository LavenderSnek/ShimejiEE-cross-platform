package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;

import java.awt.Rectangle;

class MacJniEnvironment extends BaseNativeEnvironment {

    private static final Rectangle INVISIBLE_RECT = new Rectangle(1, 1, -10_000, -10_000);

    @Override
    protected void updateIe(Area ieToUpdate) {
        ieToUpdate.setVisible(false);
        ieToUpdate.set(INVISIBLE_RECT);
    }

}
