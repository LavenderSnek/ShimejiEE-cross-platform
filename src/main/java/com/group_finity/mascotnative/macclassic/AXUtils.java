package com.group_finity.mascotnative.macclassic;

import com.group_finity.mascotnative.macclassic.jna.*;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.awt.Rectangle;

import static com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;

/**
 * This class contains native utilities for interacting with the macOS accessibility API.
 * <p>
 * The functions in this class follow the memory management naming convention
 * <a href="https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/MemoryMgmt/Articles/mmRules.html">
 * from this article</a>.
 */
public class AXUtils {

    private static final CFStringRef kAXPosition = toCFString("AXPosition");
    private static final CFStringRef kAXSize = toCFString("AXSize");
    private static final CFStringRef kAXFocusedWindow = toCFString("AXFocusedWindow");
    private static final CFStringRef kAXChildren = toCFString("AXChildren");

    private static final Carbon carbon = Carbon.INSTANCE;

    private static CFStringRef toCFString(String s) {
        return CFStringRef.createCFString(s);
    }

    private AXUtils() {
    }

    //======== utils =======//

    static Rectangle getRectOf(AXUIElementRef window) {
        CGPoint pos = getAXPositionOf(window);
        CGSize size = getAXSizeOf(window);
        return new Rectangle(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
    }

    static AXUIElementRef copyFocusedWindowOfPid(long targetPid) {
        AXUIElementRef application = carbon.AXUIElementCreateApplication(targetPid);
        AXUIElementRef focusedWindow = AXUtils.copyAXFocusedWindowOf(application);
        application.release();
        return focusedWindow;
    }

    static CFArrayRef copyChildWindowsOfPid(long targetPid) {
        AXUIElementRef application = carbon.AXUIElementCreateApplication(targetPid);
        CFArrayRef children = AXUtils.copyAXChildrenOf(application);
        application.release();
        return children;
    }

    static Rectangle getFocusedWindowRectOfPid(long targetPid) {
        if (targetPid == 0) {
            return null;
        }

        Rectangle rect = null;
        AXUIElementRef focusedWindow = AXUtils.copyFocusedWindowOfPid(targetPid);

        if (focusedWindow != null) {
            rect = AXUtils.getRectOf(focusedWindow);
            focusedWindow.release();
        }

        return rect;
    }

    static long getFrontmostAppsPID() {
        ProcessSerialNumber frontProcessPsn = new ProcessSerialNumber();
        LongByReference frontProcessPidp = new LongByReference();

        carbon.GetFrontProcess(frontProcessPsn);
        carbon.GetProcessPID(frontProcessPsn, frontProcessPidp);

        return frontProcessPidp.getValue();
    }

    //===== Getting properties =========//

    static CGPoint getAXPositionOf(AXUIElementRef window) {
        CGPoint position = new CGPoint();
        AXValueRef axVal = new AXValueRef();
        PointerByReference valPtr = new PointerByReference();

        carbon.AXUIElementCopyAttributeValue(window, kAXPosition, valPtr);
        axVal.setPointer(valPtr.getValue());

        carbon.AXValueGetValue(axVal, carbon.kAXValueCGPointType, position.getPointer());
        position.read();

        axVal.release();

        return position;
    }

    static void setAXPositionOf(AXUIElementRef window, double x, double y) {
        CGPoint position = new CGPoint(x, y);
        position.write();

        AXValueRef axVal = carbon.AXValueCreate(carbon.kAXValueCGPointType, position.getPointer());
        carbon.AXUIElementSetAttributeValue(window, kAXPosition, axVal);

        axVal.release();
    }

    static CGSize getAXSizeOf(AXUIElementRef elementRef) {
        CGSize size = new CGSize();
        AXValueRef axVal = new AXValueRef();
        PointerByReference valPtr = new PointerByReference();

        carbon.AXUIElementCopyAttributeValue(elementRef, kAXSize, valPtr);
        axVal.setPointer(valPtr.getValue());

        carbon.AXValueGetValue(axVal, carbon.kAXValueCGSizeType, size.getPointer());
        size.read();

        axVal.release();

        return size;
    }

    static CFArrayRef copyAXChildrenOf(AXUIElementRef elementRef) {
        PointerByReference axWindowsPtr = new PointerByReference();
        carbon.AXUIElementCopyAttributeValue(elementRef, kAXChildren, axWindowsPtr);

        if (axWindowsPtr.getValue() == Pointer.NULL) {
            return null;
        }

        return new CFArrayRef(axWindowsPtr.getValue());
    }

    static AXUIElementRef copyAXFocusedWindowOf(AXUIElementRef elementRef) {
        PointerByReference windowPtr = new PointerByReference();

        if (carbon.AXUIElementCopyAttributeValue(elementRef, kAXFocusedWindow, windowPtr) == carbon.kAXErrorSuccess) {
            AXUIElementRef window = new AXUIElementRef();
            window.setPointer(windowPtr.getValue());
            return window;
        }

        return null;
    }

}
