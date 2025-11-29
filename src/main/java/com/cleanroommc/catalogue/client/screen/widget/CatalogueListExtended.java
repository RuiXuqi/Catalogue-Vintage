package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.client.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CatalogueListExtended<E extends CatalogueListExtended.IListEntry> extends GuiListExtended {
    public boolean visible = true;
    private boolean scrollBarVisible;

    public CatalogueListExtended(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    // Values renamed by deepseek. Comments are handwrite.
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        this.bindAmountScrolled();
        int maxScroll = this.getMaxScroll();
        this.scrollBarVisible = maxScroll > 0 && this.getContentHeight() != 0;

        ClientHelper.scissor(this.left, this.top, this.width, this.bottom - this.top);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;

        // Shadowed dirt background. Scroll with the entries.
        this.drawContainerBackground(tessellator);

        // Customized header. Empty by default
        if (this.hasListHeader) {
            this.drawListHeader(this.getListLeft(), this.getListTop(), tessellator);
        }

        this.renderListItems(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Scroll Bar
        if (this.scrollBarVisible) {
            this.drawScrollBar(maxScroll);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawScrollBar(int maxScroll) {
        Tessellator tessellator = Tessellator.instance;

        int scrollBarLeft = this.getScrollBarX();
        int scrollBarRight = scrollBarLeft + 6;

        int scrollThumbHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
        scrollThumbHeight = MathHelper.clamp_int(scrollThumbHeight, 32, this.bottom - this.top - 8);
        int scrollThumbTop = (int) this.amountScrolled * (this.bottom - this.top - scrollThumbHeight) / maxScroll + this.top;
        scrollThumbTop = Math.max(scrollThumbTop, this.top);

        // Background
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV(scrollBarLeft, this.bottom, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight, this.bottom, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight, this.top, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(scrollBarLeft, this.top, 0.0D, 0.0D, 0.0D);
        tessellator.draw();

        // Main
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(8421504, 255);
        tessellator.addVertexWithUV(scrollBarLeft, scrollThumbTop + scrollThumbHeight, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight, scrollThumbTop + scrollThumbHeight, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight, scrollThumbTop, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(scrollBarLeft, scrollThumbTop, 0.0D, 0.0D, 0.0D);
        tessellator.draw();

        // Border
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(12632256, 255);
        tessellator.addVertexWithUV(scrollBarLeft, scrollThumbTop + scrollThumbHeight - 1, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight - 1, scrollThumbTop + scrollThumbHeight - 1, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(scrollBarRight - 1, scrollThumbTop, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(scrollBarLeft, scrollThumbTop, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
    }

    protected void renderListItems(int mouseX, int mouseY, float partialTicks) {
        for (int index = 0; index < this.getSize(); ++index) {
            int rowLeft = this.getListLeft();
            int rowRight = this.getListRight();
            int rowTop = this.getRowTop(index);
            int rowBottom = this.getRowBottom(index) - 4;

            if (rowTop + this.slotHeight >= this.top && rowTop <= this.bottom) {
                this.renderItem(index, rowLeft, rowTop, rowRight, rowBottom, mouseX, mouseY, partialTicks);
            }
        }
    }

    protected void renderItem(int slotIndex, int rowLeft, int rowTop, int rowRight, int rowBottom, int mouseX, int mouseY, float partialTicks) {
        this.drawSlot(slotIndex, rowLeft, rowTop, rowBottom - rowTop, Tessellator.instance, mouseX, mouseY);
    }

    @Deprecated
    @Override
    protected void drawSelectionBox(int contentLeft, int contentTop, int mouseXIn, int mouseYIn) {
    }

    // Backport from 1.12.2. Providing a better scroll experience.
    public void handleMouseInput() {
        if (this.isMouseYWithinSlotBounds(this.mouseY)) {

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
                int listLeft = this.getListLeft();
                int listRight = this.getListRight();

                int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                int slotIndex = relativeY / this.slotHeight;

                if (slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                    this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                    this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }

            if (Mouse.isButtonDown(0) && this.getEnabled()) {
                if (this.initialClickY == -1) {
                    boolean clickedOnList = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                        int listLeft = this.getListLeft();
                        int listRight = this.getListRight();
                        int relativeY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
                        int slotIndex = relativeY / this.slotHeight;

                        if (slotIndex < this.getSize() && this.mouseX >= listLeft && this.mouseX <= listRight && slotIndex >= 0 && relativeY >= 0) {
                            boolean isDoubleClick = slotIndex == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(slotIndex, isDoubleClick, this.mouseX, this.mouseY);
                            this.selectedElement = slotIndex;
                            this.lastClicked = Minecraft.getSystemTime();
                        } else if (this.mouseX >= listLeft && this.mouseX <= listRight && relativeY < 0) {
                            this.clickedHeader(this.mouseX - listLeft, this.mouseY - this.top + (int) this.amountScrolled - 4);
                            clickedOnList = false;
                        }

                        int scrollBarLeft = this.getScrollBarX();
                        int scrollBarRight = scrollBarLeft + 6;

                        if (this.mouseX >= scrollBarLeft && this.mouseX <= scrollBarRight) {
                            this.scrollMultiplier = -1.0F;

                            int maxScroll = Math.max(1, this.getMaxScroll());

                            int viewHeight = this.bottom - this.top;
                            int scrollBarHeight = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());

                            scrollBarHeight = MathHelper.clamp_int(scrollBarHeight, 32, viewHeight - 8);

                            this.scrollMultiplier /= (float) (viewHeight - scrollBarHeight) / (float) maxScroll;
                        } else {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (clickedOnList) {
                            this.initialClickY = this.mouseY;
                        } else {
                            this.initialClickY = -2;
                        }
                    } else {
                        this.initialClickY = -2;
                    }
                } else if (this.initialClickY >= 0) {
                    this.amountScrolled -= (this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            } else {
                this.initialClickY = -1;
            }

            int dWheel = Mouse.getEventDWheel();

            if (dWheel != 0) {
                dWheel = dWheel > 0 ? -1 : 1;
                this.amountScrolled += (float) (dWheel * this.slotHeight / 2);
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (this.isMouseYWithinSlotBounds(mouseY)) {
            int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
            if (slotIndex >= 0) {
                int j = this.left + this.getListLeft();
                int k = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
                int relativeX = mouseX - j;
                int relativeY = mouseY - k;
                if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY)) {
                    this.setEnabled(false);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent) {
        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            int j = this.left + this.getListLeft();
            int k = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
            int relativeX = mouseX - j;
            int relativeY = mouseY - k;
            this.getListEntry(slotIndex).mouseReleased(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
        }
        this.setEnabled(true);
        return false;
    }

    public int getSlotIndexFromScreenCoords(int mouseX, int mouseY) {
        int i = this.getListWidth() / 2;
        int j = this.left + this.width / 2;
        int k = j - i;
        int l = j + i;
        int i1 = MathHelper.floor_float(mouseY - this.top) - this.headerPadding + this.getAmountScrolled() - 4;
        int j1 = i1 / this.slotHeight;
        return mouseX < this.getScrollBarX() && mouseX >= k && mouseX <= l && j1 >= 0 && i1 >= 0 && j1 < this.getSize() ? j1 : -1;
    }

    // Fix hover check bug in 1.7.10
    @Override
    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, Tessellator tessellator, int mouseXIn, int mouseYIn) {
        this.getListEntry(slotIndex).drawEntry(slotIndex, xPos, yPos, this.getListWidth(), heightIn, tessellator, mouseXIn, mouseYIn, this.isMouseYWithinSlotBounds(mouseYIn) && this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == slotIndex);
    }

    public void setClampedAmountScrolled(float scroll) {
        this.amountScrolled = MathHelper.clamp_float(scroll, 0.0F, this.getMaxScroll());
    }

    public void setAmountScrolled(float scroll) {
        this.setClampedAmountScrolled(scroll);
    }

    public void clampAmountScrolled() {
        this.setClampedAmountScrolled(this.getAmountScrolled());
    }

    public void setWidth(int width) {
        this.width = width;
        this.right = this.left + this.width;
    }

    public void setHeight(int height) {
        this.height = height;
        this.bottom = this.top + height;
    }

    protected boolean isScrollBarVisible() {
        return this.scrollBarVisible;
    }

    protected int getListLeft() {
        return this.width / 2 - this.getListWidth() / 2 + 2;
    }

    protected int getListRight() {
        return this.getListLeft() + this.getListWidth();
    }

    protected int getListTop() {
        return this.top + 4 - (int) this.amountScrolled;
    }

    protected int getRowTop(int slotIndex) {
        return this.top + 4 - (int) this.amountScrolled + slotIndex * this.slotHeight + this.headerPadding;
    }

    protected int getRowBottom(int slotIndex) {
        return this.getRowTop(slotIndex) + this.slotHeight;
    }

    /*
    Some helpers.
     */

    private final List<E> children = new ArrayList<>();

    public final List<E> children() {
        return this.children;
    }

    @Nonnull
    @Override
    public E getListEntry(int slotIndex) {
        return this.children().get(slotIndex);
    }

    @Override
    protected int getSize() {
        return this.children().size();
    }

    public void centerScrollOn(E pEntry) {
        this.setAmountScrolled((float) (this.children().indexOf(pEntry) * this.slotHeight + this.slotHeight / 2 - (this.bottom - this.top) / 2));
    }

    public void addEntry(E pEntry) {
        this.children().add(pEntry);
    }

    public void clearEntries() {
        this.children().clear();
    }

    public void replaceEntries(Collection<E> entries) {
        this.clearEntries();
        this.children().addAll(entries);
    }

    public void removeEntries(@Nonnull List<E> entries) {
        entries.forEach(this::removeEntry);
    }

    public void removeEntry(E entry) {
        this.children.remove(entry);
    }

    public void clearEntriesExcept(E entry) {
        this.children.removeIf((e) -> e != entry);
    }

    public interface IListEntry extends IGuiListEntry {

        /**
         * Called when the mouse is clicked within this entry.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         * @return true means that something within this entry was clicked and the list should not be dragged.
         */
        @Override
        default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
            return true;
        }

        /**
         * Called when the mouse button is released.
         *
         * @param mouseX    the current mouse x position
         * @param mouseY    the current mouse y position
         * @param relativeX the current x position of the mouse relative to the top-left corner of the entry
         * @param relativeY the current y position of the mouse relative to the top-left corner of the entry
         */
        @Override
        default void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseButton, int relativeX, int relativeY) {
        }
    }

    /*
    Simply rename some srg-named methods.
     */

    /**
     * @deprecated Use {@link #isMouseYWithinSlotBounds(int)}.
     */
    @Deprecated
    @Override
    public boolean func_148141_e(int mouseY) {
        return this.isMouseYWithinSlotBounds(mouseY);
    }

    public boolean isMouseYWithinSlotBounds(int mouseY) {
        return super.func_148141_e(mouseY);
    }

    /**
     * @deprecated Use {@link #getSlotIndexFromScreenCoords(int, int)}.
     */
    @Deprecated
    @Override
    public int func_148124_c(int mouseX, int mouseY) {
        return this.getSlotIndexFromScreenCoords(mouseX, mouseY);
    }

    /**
     * @deprecated Use {@link #mouseReleased(int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean func_148181_b(int x, int y, int mouseEvent) {
        return this.mouseReleased(x, y, mouseEvent);
    }

    /**
     * @deprecated Use {@link #mouseClicked(int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean func_148179_a(int mouseX, int mouseY, int mouseEvent) {
        return this.mouseClicked(mouseX, mouseY, mouseEvent);
    }

    /**
     * @deprecated Use {@link #setEnabled(boolean)}.
     */
    @Deprecated
    @Override
    public void func_148143_b(boolean enabledIn) {
        this.setEnabled(enabledIn);
    }

    public void setEnabled(boolean enabledIn) {
        super.func_148143_b(enabledIn);
    }

    /**
     * @deprecated Use {@link #getEnabled()}.
     */
    @Deprecated
    @Override
    public boolean func_148125_i() {
        return this.getEnabled();
    }

    public boolean getEnabled() {
        return super.func_148125_i();
    }

    /**
     * @deprecated Use {@link #getMaxScroll()}.
     */
    @Deprecated
    @Override
    public int func_148135_f() {
        return this.getMaxScroll();
    }

    public int getMaxScroll() {
        return super.func_148135_f();
    }

    /**
     * @deprecated Use {@link #clickedHeader(int, int)}.
     */
    @Deprecated
    @Override
    protected void func_148132_a(int mouseX, int mouseY) {
        this.clickedHeader(mouseX, mouseY);
    }

    protected void clickedHeader(int mouseX, int mouseY) {
        super.func_148132_a(mouseX, mouseY);
    }
}
