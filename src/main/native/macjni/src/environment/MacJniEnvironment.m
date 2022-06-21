
#import "com_group_finity_mascotnative_macjni_MacJniEnvironment.h"
#import "ShimejiEnvironment.h"

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniEnvironment
 * Method:    createNativeShimejiEnvironment
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniEnvironment_createNativeShimejiEnvironment
(JNIEnv *env, jclass cls) {
    ShimejiEnvironment* shimejiEnv = [[ShimejiEnvironment alloc] init];
    return (jlong)shimejiEnv;
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniEnvironment
 * Method:    getUpdatedIeOf
 * Signature: (J)Lcom/group_finity/mascotnative/macjni/MacJniEnvironment/IeRep;
 */
JNIEXPORT jobject JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniEnvironment_getUpdatedIeOf
(JNIEnv *env, jclass cls, jlong shimejiEnvPtr) {
    return [(ShimejiEnvironment*)shimejiEnvPtr getUpdatedIeRep];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniEnvironment
 * Method:    moveIeOf
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniEnvironment_moveIeOf
(JNIEnv *env, jclass cls, jlong shimejiEnvPtr, jint topLeftX, jint topLeftY) {
    return [(ShimejiEnvironment*)shimejiEnvPtr setTopLeftOfCurrentIeToX:topLeftX
                                                                      Y:topLeftY];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniEnvironment
 * Method:    restoreIesOf
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniEnvironment_restoreIesOf
(JNIEnv *env, jclass cls, jlong shimejiEnvPtr) {
    [(ShimejiEnvironment*)shimejiEnvPtr restoreMovedIes];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniEnvironment
 * Method:    getCurrentScreens
 * Signature: ()[Lcom/group_finity/mascotnative/macjni/MacJniEnvironment/ScreenRep;
 */
JNIEXPORT jobjectArray JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniEnvironment_getCurrentScreens
(JNIEnv *env, jclass cls) {
    return [ShimejiEnvironment getCurentScreenReps];
}
