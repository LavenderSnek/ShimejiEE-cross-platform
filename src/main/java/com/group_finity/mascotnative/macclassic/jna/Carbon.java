package com.group_finity.mascotnative.macclassic.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Carbon extends Library {

    Carbon INSTANCE = Native.load("Carbon", Carbon.class);

    NativeLibrary nl = NativeLibrary.getProcess();

    long kAXErrorSuccess = 0;
    long kAXValueCGPointType = 1;
    long kAXValueCGSizeType = 2;
    long kCFNumberInt32Type = 3;

    long GetFrontProcess(ProcessSerialNumber psn);

    long GetProcessPID(final ProcessSerialNumber psn, LongByReference pidp);

    long AXUIElementCopyAttributeValue(AXUIElementRef element, CFStringRef attr, PointerByReference value);

    int AXUIElementSetAttributeValue(AXUIElementRef element, CFStringRef attr, CFTypeRef value);

    AXUIElementRef AXUIElementCreateApplication(long pid);

    boolean AXValueGetValue(AXValueRef value, long type, Pointer valuep);

    AXValueRef AXValueCreate(long type, Pointer valuep);

}
