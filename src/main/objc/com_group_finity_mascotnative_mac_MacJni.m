#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>
#import <jni.h>

#include "com_group_finity_mascotnative_mac_MacJni.h"

// this whole thing has no error handling. If it breaks, it breaks

#pragma mark - Convenience 1

jfieldID getFieldIDFor
 (JNIEnv *env, jobject objContainingField, char fieldName[], char fieldSignature[]) {
    jclass containingCls = (*env)->GetObjectClass(env, objContainingField);
    jfieldID fieldID = (*env)->GetFieldID(env, containingCls, fieldName, fieldSignature);
    return fieldID;
}

jmethodID getMethodIDFor
 (JNIEnv *env, jobject objContainingMethod, char methodName[], char returnSignature[]) {
    jclass containingCls = (*env)->GetObjectClass(env, objContainingMethod);
    jmethodID methodID = (*env)->GetMethodID(env, containingCls, methodName, returnSignature);
    return methodID;
}


#pragma mark - Convenience 2

jobject getObjectField
 (JNIEnv *env, jobject objContainingField, char fieldName[], char fieldSignature[]) {
    jfieldID fieldID = getFieldIDFor(env, objContainingField, fieldName, fieldSignature);
    jobject fieldVal = (*env)->GetObjectField(env, objContainingField, fieldID);
    return fieldVal;
}

jlong getLongField
 (JNIEnv *env, jobject objContainingField, char fieldName[]) {
    jfieldID fieldID = getFieldIDFor(env, objContainingField, fieldName, "J");
    jlong fieldVal = (*env)->GetLongField(env, objContainingField, fieldID);
    return fieldVal;
}


jobject CallObjectMethod
 (JNIEnv *env, jobject objContainingMethod, char methodName[], char returnSignature[]) {
    jmethodID methodID = getMethodIDFor(env, objContainingMethod, methodName, returnSignature);
    jobject methodRetVal = (*env)->CallObjectMethod(env, objContainingMethod, methodID);
    return methodRetVal;
}


#pragma mark - Actual Code

jlong getNativeWindowPointer
 (JNIEnv *env, jobject javaWindow) {

    jobject windowPeer = getObjectField(env, javaWindow, "peer", "Ljava/awt/peer/ComponentPeer;");

    jobject platformWindow = CallObjectMethod(env, windowPeer, "getPlatformWindow", "()Lsun/lwawt/PlatformWindow;");

    return getLongField(env, platformWindow, "ptr");
}

JNIEXPORT void JNICALL
Java_com_group_1finity_mascotnative_mac_MacJni_nativeSetNSWindowLevel
  (JNIEnv *env, jclass cls, jobject javaWindow, jint requestedWindowLevel) {

    long winPtr = getNativeWindowPointer(env, javaWindow);

    NSWindow *win = (__bridge NSWindow*) winPtr;

    dispatch_async(dispatch_get_main_queue(), ^{
      win.level = requestedWindowLevel;
    });

}
