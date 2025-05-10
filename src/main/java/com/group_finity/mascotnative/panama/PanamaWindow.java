package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascot.window.contextmenu.MenuItemRep;
import com.group_finity.mascot.window.contextmenu.MenuRep;
import com.group_finity.mascotnative.panama.bindings.render.*;
import com.group_finity.mascotnative.panama.bindings.render.Menu;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

class PanamaWindow implements TranslucentWindow {
    private final Arena arena = Arena.ofShared();
    private final MemorySegment renderer;

    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;
    private final MemorySegment menuCallbacks;

    private NativeImage image = null;
    private Rectangle bounds = new Rectangle();
    private boolean visible = false;

    private Arena menuArena = Arena.ofShared();

    private static void addSubItems(Arena arena, MemorySegment menu, MenuItemRep[] items) {
        for (var item : items) {
            if (item instanceof MenuRep submenu) {
                var sm = NativeRenderer_h.menu_create_submenu(arena, menu, arena.allocateFrom(submenu.getTitle()));
                addSubItems(arena, sm, submenu.getSubItems());
                continue;
            }

            if (item.isSeparator()) {
                NativeRenderer_h.menu_add_separator(menu);
            } else if (!item.isEnabled()) {
                NativeRenderer_h.menu_add_disabled(menu, arena.allocateFrom(item.getTitle()));
            } else {
                NativeRenderer_h.menu_add_button(menu, arena.allocateFrom(item.getTitle()), MenuCallback.allocate(() -> item.getAction().run(), arena));
            }
        }
    }

    // this _might_ be a memory leak, im not sure
    private MemorySegment createMenu() {
        menuArena.close(); // hopefully this fixes it?
        menuArena = Arena.ofShared();

        var m = NativeRenderer_h.menu_create(menuArena, menuArena.allocateFrom(""), menuCallbacks);

        if (Menu.data(m) == MemorySegment.NULL) {
            return m;
        }

        var rep = eventHandler.getContextMenuRep();
        addSubItems(menuArena, m, rep.getSubItems());

        return m;
    }

    PanamaWindow() {
        var down = MouseHandler.allocate((rel_x, rel_y) -> eventHandler.onDragBegin(new TranslucentWindowEvent(new Point(rel_x, rel_y))), arena);
        var up = MouseHandler.allocate((rel_x, rel_y) -> eventHandler.onDragEnd(new TranslucentWindowEvent(new Point(rel_x, rel_y))), arena);
        var menu_prod = MenuProducer.allocate(this::createMenu, arena);

        var cbs = RendererCallbacks.allocate(arena);
        RendererCallbacks.on_left_mousedown(cbs, down);
        RendererCallbacks.on_left_mouseup(cbs, up);
        RendererCallbacks.create_menu(cbs, menu_prod);

        this.renderer = NativeRenderer_h.renderer_create(arena, cbs);

        menuCallbacks = MenuCallbacks.allocate(arena);
        MenuCallbacks.on_open(menuCallbacks, MenuCallback.allocate(() -> eventHandler.getContextMenuRep().getOnOpenAction().run(), arena));
        MenuCallbacks.on_close(menuCallbacks, MenuCallback.allocate(() -> eventHandler.getContextMenuRep().getOnCloseAction().run(), arena));
    }

    @Override
    public void setEventHandler(TranslucentWindowEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void setImage(NativeImage image) {
        if (image != null) {
            this.image = image;
        }
    }

    @Override
    public void updateImage() {
        if (image != null) {
            NativeRenderer_h.renderer_update(visible, renderer, ((PanamaImage)image).image, bounds.x, bounds.y);
        }
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Rectangle r) {
        this.bounds = r;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean b) {
        this.visible = b;
    }

    @Override
    public void dispose() {
        NativeRenderer_h.renderer_dispose(renderer);
        menuArena.close();
        arena.close();
    }
}
