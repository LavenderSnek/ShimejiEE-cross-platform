package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MacJniEnvironment extends BaseNativeEnvironment {

    private static final Rectangle INVISIBLE_RECT = new Rectangle(1, 1, -10_000, -10_000);

    public record IeRep(
            Rectangle bounds,
            String title
    ) { }

    public record ScreenRep(
            Rectangle bounds,
            Rectangle visibleBounds
    ) { }

    private static native long createNativeShimejiEnvironment();
    private static native IeRep getUpdatedIeOf(long shimejiEnvPtr);
    private static native void moveIeOf(long shimejiEnvPtr, int x, int y);
    private static native void restoreIesOf(long shimejiEnvPtr);
    private static native ScreenRep[] getCurrentScreens();

    private final long ptr;
    String currentIeTitle = null;

    private Set<Rectangle> fullScreenBounds = new HashSet<>(8);
    private Rectangle visibleBoundsUnionRect = new Rectangle();


    MacJniEnvironment() {
        this.ptr = createNativeShimejiEnvironment();
    }

    @Override
    protected List<Rectangle> getNewDisplayBoundsList() {
        ScreenRep[] screenReps = getCurrentScreens();

        HashSet<Rectangle> fsb = new HashSet<>();
        Rectangle vbUnion = new Rectangle();

        for (ScreenRep screenRep : screenReps) {
            final var vbRect = screenRep.visibleBounds().getBounds();
            fsb.add(vbRect);
            fsb.addAll(getAllApproxRectsOf(vbRect));

            vbUnion = vbUnion.union(vbRect);
        }

        fullScreenBounds = fsb;
        visibleBoundsUnionRect = vbUnion;

        return Arrays.stream(screenReps)
                .map(ScreenRep::bounds)
                .toList();
    }

    private boolean isValidIe(IeRep ie) {
        return ie != null
               && ie.bounds() != null
               && !fullScreenBounds.contains(ie.bounds())
               && !ie.bounds().contains(visibleBoundsUnionRect);
    }

    @Override
    public String getActiveIETitle() {
        return currentIeTitle;
    }

    @Override
    protected void updateIe(Area ieToUpdate) {
        IeRep ie = getUpdatedIeOf(ptr);

        boolean visible = isValidIe(ie);
        Rectangle bounds = visible ? ie.bounds() : INVISIBLE_RECT;

        ieToUpdate.setVisible(visible);
        ieToUpdate.set(bounds);
        currentIeTitle = visible ? ie.title() : null;
    }

    @Override
    public void moveActiveIE(Point point) {
        var newIeRect = getActiveIE().toRectangle();
        newIeRect.setLocation(point);

        if (newIeRect.intersects(visibleBoundsUnionRect)) {
            if (point.y < visibleBoundsUnionRect.y) {
                point.y = visibleBoundsUnionRect.y;
            }

            moveIeOf(ptr, point.x, point.y);
        }
    }

    @Override
    public void restoreIE() {
        // not implemented for now
    }

    static List<Rectangle> getAllApproxRectsOf(Rectangle rect) {
        List<Rectangle> rects = new ArrayList<>();
        // atrocious code; idek if this even improves it that much
        rects.add(new Rectangle(rect.x - 1, rect.y, rect.width, rect.height));
        rects.add(new Rectangle(rect.x + 1, rect.y, rect.width, rect.height));
        rects.add(new Rectangle(rect.x, rect.y - 1, rect.width, rect.height));
        rects.add(new Rectangle(rect.x, rect.y + 1, rect.width, rect.height));
        rects.add(new Rectangle(rect.x, rect.y, rect.width -1, rect.height));
        rects.add(new Rectangle(rect.x, rect.y, rect.width + 1, rect.height));
        rects.add(new Rectangle(rect.x, rect.y, rect.width, rect.height - 1));
        rects.add(new Rectangle(rect.x, rect.y, rect.width, rect.height + 1));
        return rects;
    }

}
