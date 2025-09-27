package com.cleanroommc.catalogue.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

/**
 * Author: MrCrayfish
 */
public class ClientHelper {
    /**
     * Creates a scissor test using minecraft screen coordinates instead of pixel coordinates.
     * @param screenX
     * @param screenY
     * @param boxWidth
     * @param boxHeight
     */
    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        int scale = scaledRes.getScaleFactor();

        int x = screenX * scale;
        int y = mc.displayHeight - (screenY * scale + boxHeight * scale);
        int width = Math.max(0, boxWidth * scale);
        int height = Math.max(0, boxHeight * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    public static boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static boolean isPlayingGame() {
        return Minecraft.getMinecraft().player != null;
    }

    /**
     * Method for linear interpolation of floats
     * @param pDelta A value usually between 0 and 1 that indicates the percentage of the lerp. (0 will give the start
     * value and 1 will give the end value)
     * @param pStart Start value for the lerp
     * @param pEnd End value for the lerp
     */
    public static float lerp(float pDelta, float pStart, float pEnd) {
        return pStart + pDelta * (pEnd - pStart);
    }

    /**
     * Draw nine slice with independent horizontal and vertical border sizes.
     *
     * @param x           X pos
     * @param y           Y pos
     * @param width       The width to draw
     * @param height      The height to draw
     * @param borderSize  Borders size
     * @param textureSize Texture size
     */
    public static void drawNineSlice(int x, int y, int width, int height, int borderSize, int textureSize) {
        drawNineSlice(x, y, width, height, borderSize, borderSize, borderSize, borderSize, textureSize, textureSize);
    }

    /**
     * Draw nine slice with independent horizontal and vertical border sizes.
     *
     * @param x             X pos
     * @param y             Y pos
     * @param width         The width to draw
     * @param height        The height to draw
     * @param borderWidth   Left & right border size
     * @param borderHeight  Top & bottom border size
     * @param textureWidth  The width of the texture
     * @param textureHeight The height of the texture
     */
    public static void drawNineSlice(int x, int y, int width, int height, int borderWidth, int borderHeight, int textureWidth, int textureHeight) {
        drawNineSlice(x, y, width, height, borderWidth, borderWidth, borderHeight, borderHeight, textureWidth, textureHeight);
    }

    /**
     * Draw nine slice with independent horizontal and vertical border sizes.
     *
     * @param x             X pos
     * @param y             Y pos
     * @param width         The width to draw
     * @param height        The height to draw
     * @param borderLeft    Left border size
     * @param borderRight   Right border size
     * @param borderTop     Top border size
     * @param borderBottom  Bottom border size
     * @param textureWidth  The width of the texture
     * @param textureHeight The height of the texture
     */
    public static void drawNineSlice(int x, int y, int width, int height,
                                     int borderLeft, int borderRight, int borderTop, int borderBottom,
                                     int textureWidth, int textureHeight) {

        int minWidth = borderLeft + borderRight;
        int minHeight = borderTop + borderBottom;
        if (width < minWidth) width = minWidth;
        if (height < minHeight) height = minHeight;

        int right = x + width;
        int bottom = y + height;
        int innerWidth = width - borderLeft - borderRight;
        int innerHeight = height - borderTop - borderBottom;

        int texRight = textureWidth - borderRight;
        int texBottom = textureHeight - borderBottom;

        // Four corners
        // Up-left
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, borderLeft, borderTop, textureWidth, textureHeight);
        // Up-right
        drawModalRectWithCustomSizedTexture(right - borderRight, y, texRight, 0, borderRight, borderTop, textureWidth, textureHeight);
        // Bottom-left
        drawModalRectWithCustomSizedTexture(x, bottom - borderBottom, 0, texBottom, borderLeft, borderBottom, textureWidth, textureHeight);
        // Bottom-right
        drawModalRectWithCustomSizedTexture(right - borderRight, bottom - borderBottom, texRight, texBottom, borderRight, borderBottom, textureWidth, textureHeight);

        // Top border
        if (innerWidth > 0 && borderTop > 0) {
            for (int i = 0; i < innerWidth; i += borderLeft) {
                int segmentWidth = Math.min(borderLeft, innerWidth - i);
                drawModalRectWithCustomSizedTexture(x + borderLeft + i, y,
                        borderLeft, 0, segmentWidth, borderTop, textureWidth, textureHeight);
            }
        }

        // Bottom border
        if (innerWidth > 0 && borderBottom > 0) {
            for (int i = 0; i < innerWidth; i += borderLeft) {
                int segmentWidth = Math.min(borderLeft, innerWidth - i);
                drawModalRectWithCustomSizedTexture(x + borderLeft + i, bottom - borderBottom,
                        borderLeft, texBottom, segmentWidth, borderBottom, textureWidth, textureHeight);
            }
        }

        // Left border
        if (innerHeight > 0 && borderLeft > 0) {
            for (int i = 0; i < innerHeight; i += borderTop) { // 使用borderTop作为步进
                int segmentHeight = Math.min(borderTop, innerHeight - i);
                drawModalRectWithCustomSizedTexture(x, y + borderTop + i,
                        0, borderTop, borderLeft, segmentHeight, textureWidth, textureHeight);
            }
        }

        // Right border
        if (innerHeight > 0 && borderRight > 0) {
            for (int i = 0; i < innerHeight; i += borderTop) {
                int segmentHeight = Math.min(borderTop, innerHeight - i);
                drawModalRectWithCustomSizedTexture(right - borderRight, y + borderTop + i,
                        texRight, borderTop, borderRight, segmentHeight, textureWidth, textureHeight);
            }
        }

        // Center area
        if (innerWidth > 0 && innerHeight > 0) {
            for (int i = 0; i < innerWidth; i += borderLeft) {
                for (int j = 0; j < innerHeight; j += borderTop) {
                    int segWidth = Math.min(borderLeft, innerWidth - i);
                    int segHeight = Math.min(borderTop, innerHeight - j);
                    drawModalRectWithCustomSizedTexture(x + borderLeft + i, y + borderTop + j,
                            borderLeft, borderTop, segWidth, segHeight, textureWidth, textureHeight);
                }
            }
        }
    }
}
