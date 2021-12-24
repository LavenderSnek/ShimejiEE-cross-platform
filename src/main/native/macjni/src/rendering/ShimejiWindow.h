
#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface ShimejiWindow : NSWindow

/**
 @param javaRep A strong global ref to an instance of MacJniShimejiWindow.
 */
-(instancetype)initWithJavaRep:(jobject)javaRep;

-(void)setImage:(NSImage *)image;

@end
