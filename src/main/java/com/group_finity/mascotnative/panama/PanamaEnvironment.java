package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;
import com.group_finity.mascotnative.panama.bindings.environment.NativeEnvironment_h;
import com.group_finity.mascotnative.panama.bindings.environment.NativeRect;

import java.awt.*;
import java.lang.foreign.Arena;

class PanamaEnvironment extends BaseNativeEnvironment {
    private static final Rectangle INVISIBLE_RECT = new Rectangle(1, 1, -10_000, -10_000);

    @Override
    protected void updateIe(Area ieToUpdate) {
        try (var a = Arena.ofConfined()) {
            var r = NativeEnvironment_h.get_active_ie_bounds(a);
            var nr = new Rectangle();

            nr.x = NativeRect.x(r);
            nr.y = NativeRect.y(r);
            nr.width = NativeRect.w(r);
            nr.height = NativeRect.h(r);

            if (nr.isEmpty()) {
                ieToUpdate.setVisible(false);
                ieToUpdate.set(INVISIBLE_RECT);
            } else {
                ieToUpdate.setVisible(true);
                ieToUpdate.set(nr);
            }
        } catch (Exception | Error e) {
            ieToUpdate.setVisible(false);
            ieToUpdate.set(INVISIBLE_RECT);
        }
    }

    @Override
    public void moveActiveIE(Point point) {
        if (!getActiveIE().isVisible()) {
            return;
        }

        // get safe bounds
        var sr = getScreen().toRectangle();
        sr.x += 100;
        sr.y += 100;
        sr.width -= 100;
        sr.height -= 100;

        var ie = getActiveIE().toRectangle();

        if (ie.intersects(sr)) {
            if (point.y < sr.y) {
                point.y = sr.y;
            }

            NativeEnvironment_h.move_ie_window(point.x, point.y);
        }
    }

    @Override
    public void restoreIE() {
        NativeEnvironment_h.restore_ie();
    }
}
