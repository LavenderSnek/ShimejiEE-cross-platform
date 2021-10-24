package com.group_finity.mascotnative.mac.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Carbon extends Library {

    Carbon INSTANCE = Native.load("Carbon", Carbon.class);

    NativeLibrary nl = NativeLibrary.getProcess();
    Pointer kCurrentUser = nl.getGlobalVariableAddress("kCFPreferencesCurrentUser").getPointer(0);
    Pointer kAnyHost = nl.getGlobalVariableAddress("kCFPreferencesAnyHost").getPointer(0);

    long kAXErrorSuccess = 0;
    long kAXValueCGPointType = 1;
    long kAXValueCGSizeType = 2;
    long kCFNumberInt32Type = 3;

    long GetFrontProcess(ProcessSerialNumber psn);

    long GetProcessPID(final ProcessSerialNumber psn, LongByReference pidp);

    long AXUIElementCopyAttributeValue(AXUIElementRef element, CoreFoundation.CFStringRef attr, PointerByReference value);

    int AXUIElementSetAttributeValue(AXUIElementRef element, CoreFoundation.CFStringRef attr, CoreFoundation.CFTypeRef value);

    AXUIElementRef AXUIElementCreateApplication(long pid);

    boolean AXValueGetValue(AXValueRef value, long type, Pointer valuep);

    AXValueRef AXValueCreate(long type, Pointer valuep);

    CoreFoundation.CFTypeRef CFPreferencesCopyValue(CoreFoundation.CFStringRef key, CoreFoundation.CFStringRef app, Pointer user, Pointer host);

    boolean CFPreferencesAppSynchronize(CoreFoundation.CFStringRef app);

    boolean CFNumberGetValue(CoreFoundation.CFTypeRef number, long type, ByReference ref);

    CoreFoundation.CFStringRef CFStringCreateWithCharacters(Pointer alloc, char[] source, long length);

    boolean CFStringGetCString(CoreFoundation.CFTypeRef strRef, Pointer buffer, long length, int encoding);

    int CFStringGetSystemEncoding();

    long CFArrayGetCount(CoreFoundation.CFArrayRef array);

    Pointer CFArrayGetValueAtIndex(CoreFoundation.CFArrayRef array, long i);

    void CFRetain(CoreFoundation.CFTypeRef any);

    void CFRelease(CoreFoundation.CFTypeRef any);

    void CFShow(CoreFoundation.CFTypeRef any);

}
