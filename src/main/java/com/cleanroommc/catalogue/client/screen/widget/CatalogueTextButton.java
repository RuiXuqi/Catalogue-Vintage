package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.client.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class CatalogueTextButton extends GuiButton {
    protected static final WidgetSprites SPRITES = new WidgetSprites(Utils.withDefaultNamespace("widget/button"), Utils.withDefaultNamespace("widget/button_disabled"), Utils.withDefaultNamespace("widget/button_highlighted"));

    public CatalogueTextButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        mc.getTextureManager().bindTexture(SPRITES.get(this.enabled, this.field_146123_n));
        ClientHelper.blitNineSlicedSprite(new ClientHelper.NineSlice(200, 20, 3), this.xPosition, this.yPosition, this.width, this.height);

        this.mouseDragged(mc, mouseX, mouseY);
        this.renderString(mc.fontRenderer, this.getFGColor());
    }

    public void renderString(FontRenderer font, int color) {
        this.renderScrollingString(font, 2, color);
    }

    protected void renderScrollingString(FontRenderer font, int width, int color) {
        int i = this.xPosition + width;
        int j = this.xPosition + this.width - width;
        this.renderScrollingString(font, this.displayString, i, this.yPosition, j, this.yPosition + this.height, color);
    }

    public void renderScrollingString(FontRenderer font, String text, int minX, int minY, int maxX, int maxY, int color) {
        this.renderScrollingString(font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    public void renderScrollingString(FontRenderer font, String text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int i = font.getStringWidth(text);
        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;
        if (i > k) {
            int l = i - k;
            double d0 = (double) System.currentTimeMillis() / (double) 1000.0F;
            double d1 = Math.max((double) l * (double) 0.5F, 3.0F);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / (double) 2.0F + (double) 0.5F;
            double d3 = Utils.lerp(d2, 0.0F, l);
            ClientHelper.scissor(minX, minY, maxX - minX, maxY - minY);
            this.drawString(font, text, minX - (int) d3, j, color);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            int i1 = MathHelper.clamp_int(centerX, minX + i / 2, maxX - i / 2);
            this.drawCenteredString(font, text, i1, j, color);
        }
    }

    protected int getFGColor() {
        if (this.packedFGColour != 0) {
            return this.packedFGColour;
        } else if (!this.enabled) {
            return 0xA0A0A0;
        } else if (this.field_146123_n) {
            return 0xFFFFA0;
        } else {
            return 0xE0E0E0;
        }
    }

    /**
     * Whether the mouse cursor is currently over the button.
     *
     * @deprecated Use {@link #isMouseOver()}.
     */
    @Deprecated
    @Override
    public boolean func_146115_a() {
        return this.isMouseOver();
    }

    /**
     * Whether the mouse cursor is currently over the button.
     */
    public boolean isMouseOver() {
        return super.func_146115_a();
    }

    /**
     * @deprecated Use {@link #playPressSound(SoundHandler)}.
     */
    @Deprecated
    @Override
    public void func_146113_a(SoundHandler soundHandlerIn) {
        this.playPressSound(soundHandlerIn);
    }

    public void playPressSound(SoundHandler soundHandlerIn) {
        super.func_146113_a(soundHandlerIn);
    }
}
