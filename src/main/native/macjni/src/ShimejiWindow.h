
#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface ShimejiWindow : NSWindow

/**
 @param javaRep an instance of a MacJniShimejiWindow object with a string global reference.
 */
-(instancetype)initWithJavaRep:(jobject)javaRep;

-(void)setImage:(NSImage *)image;

@end
