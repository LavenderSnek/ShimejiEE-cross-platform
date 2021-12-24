
#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface JniMenuItem : NSMenuItem

/**
 @param javaRep A strong global ref to an instance of MacJniMenuRep.
 */
- (instancetype)initWithTitle:(NSString*)title javaRep:(jobject)javaRep;

@end
