
#import <Foundation/Foundation.h>
#import <jni.h>

@interface ShimejiEnvironment : NSObject

- (jobject)getUpdatedIeRep;

- (void)setTopLeftOfCurrentIeToX:(jint)x Y:(jint)y;

- (void)restoreMovedIes;

+ (jobjectArray)getCurentScreenReps;

@end
