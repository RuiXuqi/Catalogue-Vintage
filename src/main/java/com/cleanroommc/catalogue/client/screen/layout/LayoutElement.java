package com.cleanroommc.catalogue.client.screen.layout;

import com.cleanroommc.catalogue.client.screen.widget.DropdownMenu;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * 布局元素接口
 */
@SideOnly(Side.CLIENT)
public interface LayoutElement {
    void setX(int x);

    void setY(int y);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    default DropdownMenu.ScreenRectangle getRectangle() {
        return new DropdownMenu.ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    void visitWidgets(Consumer<LayoutElement> consumer);
}
