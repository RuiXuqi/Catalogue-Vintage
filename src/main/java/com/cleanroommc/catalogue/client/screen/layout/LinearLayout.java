package com.cleanroommc.catalogue.client.screen.layout;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 简化的布局系统，专门用于 DropdownMenu
 */
@SideOnly(Side.CLIENT)
public class LinearLayout implements LayoutElement {
    private final Orientation orientation;
    private final List<LayoutElement> children = new ArrayList<>();
    private int x;
    private int y;
    private int width;
    private int height;

    public LinearLayout(Orientation orientation) {
        this.orientation = orientation;
    }

    public void arrangeElements() {
        if (children.isEmpty()) return;

        int totalPrimary = 0;
        int maxSecondary = 0;

        // 计算总长度和最大宽度/高度
        for (LayoutElement child : children) {
            if (orientation == Orientation.HORIZONTAL) {
                totalPrimary += child.getWidth();
                maxSecondary = Math.max(maxSecondary, child.getHeight());
            } else {
                totalPrimary += child.getHeight();
                maxSecondary = Math.max(maxSecondary, child.getWidth());
            }
        }

        // 设置位置
        int currentPos = 0;
        for (LayoutElement child : children) {
            if (orientation == Orientation.HORIZONTAL) {
                child.setX(x + currentPos);
                child.setY(y);
                currentPos += child.getWidth();
            } else {
                child.setX(x);
                child.setY(y + currentPos);
                currentPos += child.getHeight();
            }
        }

        // 更新容器尺寸
        if (orientation == Orientation.HORIZONTAL) {
            this.width = totalPrimary;
            this.height = maxSecondary;
        } else {
            this.width = maxSecondary;
            this.height = totalPrimary;
        }
    }

    public <T extends LayoutElement> T addChild(T child) {
        children.add(child);
        return child;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<LayoutElement> consumer) {
        for (LayoutElement child : children) {
            child.visitWidgets(consumer);
        }
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
