package com.mrcrayfish.catalogue.client.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class CatalogueTextField extends GuiTextField {
    // GuiTextField with suggestions.

    private final FontRenderer fontRenderer;
    private String suggestion = "";

    public CatalogueTextField(int id, FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(id, fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        int textX = this.getEnableBackgroundDrawing() ? this.x + 4 : this.x;
        int textY = this.getEnableBackgroundDrawing() ? this.y + (this.height - 8) / 2 : this.y;
        if (this.getText().isEmpty() && !this.isFocused()) {
            this.fontRenderer.drawStringWithShadow(suggestion, textX, textY, 0x808080);
        }
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return this.suggestion;
    }

}
