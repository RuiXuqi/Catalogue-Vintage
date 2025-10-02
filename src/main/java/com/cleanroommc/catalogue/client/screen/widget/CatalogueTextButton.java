package com.cleanroommc.catalogue.client.screen.widget;

import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.client.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class CatalogueTextButton extends GuiButton {
    protected static final WidgetSprites SPRITES = new WidgetSprites(Utils.withDefaultNamespace("widget/button"), Utils.withDefaultNamespace("widget/button_disabled"), Utils.withDefaultNamespace("widget/button_highlighted"));

    public CatalogueTextButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture(SPRITES.get(this.enabled, this.hovered));
        ClientHelper.blitNineSlicedSprite(new ClientHelper.NineSlice(200, 20, 3), this.x, this.y, this.width, this.height);

        this.mouseDragged(mc, mouseX, mouseY);
        this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, this.getFGColor());
    }

    protected int getFGColor() {
        if (packedFGColour != 0) {
            return packedFGColour;
        } else if (!this.enabled) {
            return 10526880;
        } else if (this.hovered) {
            return 16777120;
        } else {
            return 14737632;
        }
    }
}
