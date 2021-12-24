
#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface JniMenuListener : NSObject<NSMenuDelegate>

/**
 @param javaRep A strong global ref to an instance of MacJniMenu.
 */
- (instancetype)initWithJavaRep:(jobject)javaRep;

@end
