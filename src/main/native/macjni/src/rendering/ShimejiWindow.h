
#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface ShimejiWindow : NSWindow

/**
 @param jvr A strong global ref to an instance of MacJniShimejiWindow.
 */
-(instancetype)initWithJavaRep:(jobject)jvr;

-(void)setImage:(NSImage *)image;

@end
