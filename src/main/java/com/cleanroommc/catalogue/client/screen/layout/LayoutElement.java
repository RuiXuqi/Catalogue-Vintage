package com.cleanroommc.catalogue.client.screen.layout;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public interface LayoutElement {
    void setX(int x);

    void setY(int y);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    default ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    void visitWidgets(Consumer<LayoutElement> consumer);
}
