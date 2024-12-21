package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascotnative.panama.bindings.render.Menu;
import com.group_finity.mascotnative.panama.bindings.render.MenuProducer;
import com.group_finity.mascotnative.panama.bindings.render.MouseHandler;
import com.group_finity.mascotnative.panama.bindings.render.NativeRenderer_h;
import com.group_finity.mascotnative.panama.bindings.render.RendererCallbacks;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

class PanamaWindow implements TranslucentWindow {
    private final Arena arena = Arena.ofShared();
    private final MemorySegment renderer;

    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;

    private NativeImage image = null;
    private Rectangle bounds = new Rectangle();
    private boolean visible = false;

    PanamaWindow() {
        var down = MouseHandler.allocate((rel_x, rel_y) -> eventHandler.onDragBegin(new TranslucentWindowEvent(new Point(rel_x, rel_y))), arena);
        var up = MouseHandler.allocate((rel_x, rel_y) -> eventHandler.onDragEnd(new TranslucentWindowEvent(new Point(rel_x, rel_y))), arena);
        var menu = MenuProducer.allocate(() -> {
            var m = Menu.allocate(arena);
            Menu.data(m, MemorySegment.NULL);
            return m;
        }, arena);

        var cbs = RendererCallbacks.allocate(arena);
        RendererCallbacks.on_left_mousedown(cbs, down);
        RendererCallbacks.on_left_mouseup(cbs, up);
        RendererCallbacks.create_menu(cbs, menu);

        this.renderer = NativeRenderer_h.renderer_create(arena, cbs);
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
        arena.close();
    }
}
