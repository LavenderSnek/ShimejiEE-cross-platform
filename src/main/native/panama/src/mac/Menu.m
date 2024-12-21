#include "Renderer.h"

@interface CbMenuDelegate : NSObject <NSMenuDelegate> {
    struct MenuCallbacks _callbacks;
}
- (instancetype)initWithCallbacks:(struct MenuCallbacks)callbacks;
@end

@implementation CbMenuDelegate
- (instancetype)initWithCallbacks:(struct MenuCallbacks)callbacks {
    self = [super init];
    if (self) {
        _callbacks = callbacks;
    }
    return self;
}

- (void)menuWillOpen:(NSMenu *)menu {
    if (_callbacks.on_open) {
        _callbacks.on_open();
    }
}

- (void)menuDidClose:(NSMenu *)menu {
    if (_callbacks.on_close) {
        _callbacks.on_close();
    }
}
@end


@interface CbMenuItem : NSMenuItem {
    MenuCallback _onClick;
}
- (instancetype)initWithTitle:(NSString *)title callback:(MenuCallback)callback;
@end

@implementation CbMenuItem
- (instancetype)initWithTitle:(NSString *)title callback:(MenuCallback)callback {
    self = [super initWithTitle:title action:@selector(onClickAction) keyEquivalent:@""];
    if (self) {
        _onClick = callback; // Store the callback in the instance variable
    }
    return self;
}

- (void)onClickAction {
    _onClick();
}
@end

struct Menu menu_create(char *title, struct MenuCallbacks callbacks) {
    __block struct Menu menu;

    @autoreleasepool {
        NSString *nst = [[NSString alloc] initWithUTF8String:title];

        [Util runOnMainSync:^{
            NSMenu* m = [[NSMenu alloc] initWithTitle:nst];

            id <NSMenuDelegate> delegate = [[[CbMenuDelegate alloc] initWithCallbacks:callbacks] autorelease];
            [m setDelegate:delegate];

            menu.data = m;
        }];
    }

    return menu;
}

struct Menu menu_create_submenu(struct Menu parent, char *title) {
    __block struct Menu menu;


    @autoreleasepool {
        NSString *nst = [[NSString alloc] initWithUTF8String:title];

        [Util runOnMainSync:^{
            NSMenuItem *item = [[[NSMenuItem alloc] initWithTitle:nst
                                                           action:NULL
                                                    keyEquivalent:@""] autorelease];

            NSMenu *submenu = [[[NSMenu alloc] initWithTitle:nst] autorelease];

            [item setSubmenu:submenu];
            [(NSMenu*)parent.data addItem:item];

            menu.data = submenu;
        }];
    }

    // only lasts as long as the parent is alive
    return menu;
}

void menu_add_button(struct Menu menu, char *title, MenuCallback on_click) {
    @autoreleasepool {
        NSString *nst = [[NSString alloc] initWithUTF8String:title];

        [Util runOnMainSync:^{
            NSMenuItem *item = [[[CbMenuItem alloc] initWithTitle:nst
                                                         callback:on_click] autorelease];
            [(NSMenu*)menu.data addItem:item];
        }];
    }
}

void menu_add_disabled(struct Menu menu, char *title) {
    @autoreleasepool {
        NSString *nst = [[NSString alloc] initWithUTF8String:title];

        [Util runOnMainSync:^{
            NSMenuItem *item = [[[NSMenuItem alloc] initWithTitle:nst
                                                           action:NULL
                                                    keyEquivalent:@""] autorelease];

            [item setEnabled:NO];
            [(NSMenu*)menu.data addItem:item];
        }];
    }
}

void menu_add_separator(struct Menu menu) {
    [Util runOnMainSync:^{
        [(NSMenu*)menu.data addItem:NSMenuItem.separatorItem];
    }];
}
