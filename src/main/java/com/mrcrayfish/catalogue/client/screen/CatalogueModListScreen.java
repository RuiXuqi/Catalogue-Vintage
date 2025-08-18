package com.mrcrayfish.catalogue.client.screen;

import com.google.common.collect.Lists;
import com.mrcrayfish.catalogue.Catalogue;
import com.mrcrayfish.catalogue.client.ScreenUtil;
import com.mrcrayfish.catalogue.client.ScreenUtil.Size2i;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueCheckBoxButton;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueIconButton;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueListExtended;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogueModListScreen extends GuiScreen {
    private static final ResourceLocation MISSING_BANNER = new ResourceLocation("catalogue", "textures/gui/missing_banner.png");
    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");
    private static final Map<String, Pair<ResourceLocation, Size2i>> LOGO_CACHE = new HashMap<>();
    private static final Map<String, Pair<ResourceLocation, Size2i>> ICON_CACHE = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_CACHE = new HashMap<>();
    
    private CatalogueTextField searchTextField;
    private ModList modList;
    private ModContainer selectedModInfo;
    private CatalogueIconButton modFolderButton;
    private CatalogueIconButton configButton;
    private CatalogueIconButton websiteButton;
    private CatalogueIconButton issueButton;
    private CatalogueCheckBoxButton updatesButton;

    private List<String> activeTooltip;
    private int tooltipYOffset;
    private StringList descriptionList;

    private long lastClickTime;

    public CatalogueModListScreen() {
        super();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.searchTextField = new CatalogueTextField(0, this.fontRenderer, 11, 25, 148, 20);
        this.searchTextField.setCanLoseFocus(true);
        this.searchTextField.setEnableBackgroundDrawing(true);

        this.modList = new ModList();
        this.modList.setSlotXBoundsFromLeft(10);
        this.modList.setDrawTopAndBottom(false);

        this.buttonList.add(new GuiButton(1, 10, modList.bottom + 8, 127, 20, I18n.format("gui.back")));
        this.modFolderButton = this.addButton(new CatalogueIconButton(2, 140, modList.bottom + 8, 0, 0));

        int padding = 10;
        int contentLeft = this.modList.right + 12 + padding;
        int contentWidth = this.width - contentLeft - padding;
        int buttonWidth = (contentWidth - padding) / 3;

        this.configButton = this.addButton(new CatalogueIconButton(3, contentLeft, 105, 10, 0, buttonWidth, I18n.format("catalogue.gui.config")));
        this.configButton.visible = false;

        this.websiteButton = this.addButton(new CatalogueIconButton(4, contentLeft + buttonWidth + 5, 105, 20, 0, buttonWidth, I18n.format("catalogue.gui.website")));
        this.websiteButton.visible = false;

        this.issueButton = this.addButton(new CatalogueIconButton(5, contentLeft + buttonWidth + buttonWidth + 10, 105, 30, 0, buttonWidth, I18n.format("catalogue.gui.issue")));
        this.issueButton.visible = false;

        this.descriptionList = new StringList(contentWidth, this.height - 135 - 55, contentLeft, 130);
        this.descriptionList.setDrawTopAndBottom(false);
        this.descriptionList.setDrawBackground(false);

        this.updatesButton = this.addButton(new CatalogueCheckBoxButton(6, this.modList.right - 14, 7, false));

        this.modList.filterAndUpdateList(this.searchTextField.getText());

        // Resizing window causes all widgets to be recreated, therefore need to update selected info
        if(this.selectedModInfo != null) {
            this.setSelectedModInfo(this.selectedModInfo);
            this.updateSelectedModList();
            // WIP. Should scroll to the entry instead of selecting it
            //ModEntry entry = this.modList.getEntryFromInfo(this.selectedModInfo);
            //if(entry != null) {
            //    this.modList.selectMod(entry);
            //}
        }
        this.updateSearchField(this.searchTextField.getText());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.drawDefaultBackground();
        this.drawModList(mouseX, mouseY, partialTicks);
        this.drawModInfo(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);

        ModContainer info = Loader.instance().getIndexedModList().get("catalogue");
        if (info != null) this.loadAndCacheLogo(info);
        Pair<ResourceLocation, Size2i> pair = LOGO_CACHE.get("catalogue");
        if (pair != null && pair.getLeft() != null) {
            ResourceLocation textureId = pair.getLeft();
            Size2i size = pair.getRight();
            mc.getTextureManager().bindTexture(textureId);
            ScreenUtil.blit(10, 9, 10, 10, 0.0F, 0.0F, size.width, size.height, size.width, size.height);
        }

        if (ScreenUtil.isMouseWithin(10, 9, 10, 10, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("catalogue.gui.info"));
            this.tooltipYOffset = 10;
        }

        if (this.modFolderButton.isMouseOver()) {
            this.setActiveTooltip(I18n.format("fml.button.open.mods.folder"));
        }

        if (this.activeTooltip != null) {
            this.drawHoveringText(this.activeTooltip, mouseX, mouseY + this.tooltipYOffset);
            this.tooltipYOffset = 0;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int key) throws IOException {
        if (this.searchTextField.textboxKeyTyped(typedChar, key)) {
            String s = this.searchTextField.getText();
            this.updateSearchField(s);
            this.modList.filterAndUpdateList(s);
            return;
        }

        super.keyTyped(typedChar, key);
    }

    private class ModList extends CatalogueListExtended {
        private List<ModEntry> entries = Lists.<ModEntry>newArrayList();
        private int selectedIndex = -1;

        public ModList() {
            super(CatalogueModListScreen.this.mc, 150, CatalogueModListScreen.this.height, 46, CatalogueModListScreen.this.height - 35, 26);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ScreenUtil.scissor(this.left, this.top, this.width, this.height);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            CatalogueModListScreen.ModEntry entry = entries.get(slotIndex);
            CatalogueModListScreen.this.setSelectedModInfo(entry.info);
        }

        public void filterAndUpdateList(String text) {
            List<ModEntry> entries = Loader.instance().getActiveModList().stream()
                .filter(info -> info.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH)))
                .filter(info -> !updatesButton.selected() || ForgeVersion.getResult(info).status.shouldDraw())
                .map(info -> new ModEntry(info, this))
                .sorted(Comparator.comparing(entry -> entry.info.getName()))
                .collect(Collectors.toList());
            this.entries = entries;
            this.selectMod(this.getEntryFromInfo(selectedModInfo));
            this.setAmountScrolled(0);
        }

        public ModEntry getEntryFromInfo(ModContainer info) {
            return this.entries.stream().filter(entry -> entry.info == info).findFirst().orElse(null);
        }

        public void selectMod(int selectedIndex) {
            this.selectedIndex = selectedIndex;
        }

        public void selectMod(IGuiListEntry entry) {
            this.selectMod(this.getEntryIndex(entry));
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.entries.get(index);
        }

        public int getEntryIndex(IGuiListEntry entry) {
            return this.entries.indexOf(entry);
        }

        @Override
        public int getListWidth() {
            return this.width; // Why it is 220 by default???
        }

        @Override
        protected int getSize() {
            return this.entries.size();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected int getScrollBarX() {
            return this.left + this.width - 6;
        }
    }

    private class ModEntry implements CatalogueListExtended.IGuiListEntry {
        private final ModContainer info;
        private final ModList list;

        public ModEntry(ModContainer info, ModList list) {
            this.info = info;
            this.list = list;
        }

        @Override
        public void drawEntry(int index, int left, int top, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            left -= 2; // Move 2px, the borders.
            // Draws mod name and version
            drawString(fontRenderer, this.getFormattedModName(), left + 24, top + 2, 0xFFFFFF);
            drawString(fontRenderer, TextFormatting.GRAY + this.info.getDisplayVersion(), left + 24, top + 12, 0xFFFFFF);

            //WIP
            //CatalogueModListScreen.this.loadAndCacheIcon(this.info);

            // Draw icon
            if(ICON_CACHE.containsKey(this.info.getModId()) && ICON_CACHE.get(this.info.getModId()).getLeft() != null) {
                ResourceLocation logoResource = TextureMap.LOCATION_MISSING_TEXTURE;
                Size2i size = new Size2i(16, 16);

                Pair<ResourceLocation, Size2i> logoInfo = ICON_CACHE.get(this.info.getModId());
                if(logoInfo != null && logoInfo.getLeft() != null) {
                    logoResource = logoInfo.getLeft();
                    size = logoInfo.getRight();
                }

                mc.getTextureManager().bindTexture(logoResource);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                drawModalRectWithCustomSizedTexture(left + 4, top + 2, 0, 0, 16, 16, size.width, size.height);
                GlStateManager.disableBlend();
            } else {
                try {
                    RenderHelper.enableGUIStandardItemLighting();
                    CatalogueModListScreen.this.mc.getRenderItem().renderItemIntoGUI(this.getItemIcon(), left + 4, top + 2);
                    RenderHelper.disableStandardItemLighting();
                } catch(Exception e) {
                    ITEM_CACHE.put(this.info.getModId(), new ItemStack(Blocks.GRASS));
                }
            }

            // Draws an icon if there is an update for the mod
            ForgeVersion.CheckResult result = ForgeVersion.getResult(this.info);
            if(result.status.shouldDraw()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = result.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                ScreenUtil.blit(left + rowWidth - 8 - 10, top + 6, result.status.getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            this.list.selectMod(slotIndex);
            return true;
        }

        private ItemStack getItemIcon() {
            if(ITEM_CACHE.containsKey(this.info.getModId())) {
                return ITEM_CACHE.get(this.info.getModId());
            }

            // Put grass as default item icon
            ITEM_CACHE.put(this.info.getModId(), new ItemStack(Blocks.GRASS));

            // Special case for Forge to set item icon to anvil
            if(this.info.getModId().equals("forge")) {
                ItemStack anvil = new ItemStack(Blocks.ANVIL);
                ITEM_CACHE.put("forge", anvil);
                return anvil;
            }

            // Not working
            // Gets the raw item icon resource string
            String itemIcon = this.info.getCustomModProperties().get("catalogueItemIcon");

            if(!itemIcon.isEmpty()) {
                try {
                    String[] parts = itemIcon.split(":");
                    Item item = Item.getByNameOrId(parts[0]);
                    int meta = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                    ItemStack itemStack = new ItemStack(item, 1, meta);
                    ITEM_CACHE.put(this.info.getModId(), itemStack);
                    return itemStack;
                } catch(Exception ignored) {}
            }

            // If the mod doesn't specify an item to use, Catalogue will attempt to get an item from the mod
            Optional<ItemStack> optional = ForgeRegistries.ITEMS.getValuesCollection().stream().filter(item -> item.getRegistryName().getNamespace().equals(this.info.getModId())).map(ItemStack::new).findFirst();
            if(optional.isPresent()) {
                ItemStack item = optional.get();
                if(!item.isEmpty())
                {
                    ITEM_CACHE.put(this.info.getModId(), item);
                    return item;
                }
            }

            return new ItemStack(Blocks.GRASS);
        }

        private String getFormattedModName() {
            String name = this.info.getName();
            int width = this.list.getListWidth() - (this.list.getMaxScroll() > 0 ? 30 : 24);
            if(CatalogueModListScreen.this.fontRenderer.getStringWidth(name) > width) {
                name = CatalogueModListScreen.this.fontRenderer.trimStringToWidth(name, width - 10) + "...";
            }
            if(this.info.getModId().equals("forge") || this.info.getModId().equals("minecraft")) {
                return TextFormatting.DARK_GRAY + name;
            }
            return name;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}
    }

    private class StringList extends CatalogueListExtended {
        private List<StringEntry> entries = Lists.<StringEntry>newArrayList();

        public StringList(int width, int height, int left, int top) {
            super(CatalogueModListScreen.this.mc, width, CatalogueModListScreen.this.height, top, top + height, 10);
            this.setSlotXBoundsFromLeft(left);
        }

        public void setTextFromInfo(ModContainer info) {
            this.entries.clear();
            String description = info.getMetadata().description.trim();
            List<String> lines = CatalogueModListScreen.this.fontRenderer.listFormattedStringToWidth(description, this.getListWidth());

            for (String line : lines) {
                String cleanLine = line.replace("\n", "").replace("\r", "").trim();
                this.entries.add(new StringEntry(cleanLine));
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ScreenUtil.scissor(this.left, this.top, this.width, this.bottom - this.top);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        protected int getScrollBarX() {
            return this.left + this.width - 7;
        }

        @Override
        protected int getSize() {
            return this.entries.size();
        }

        @Override
        public int getListWidth() {
            return this.width - 10;
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.entries.get(index);
        }
    }

    private class StringEntry implements CatalogueListExtended.IGuiListEntry {
        private String line;

        public StringEntry(String line) {
            this.line = line;
        }

        @Override
        public void drawEntry(int index, int left, int top, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            drawString(CatalogueModListScreen.this.fontRenderer, this.line, left, top, 0xFFFFFF);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return true;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.modList.handleMouseInput();
        this.descriptionList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        this.searchTextField.mouseClicked(x, y, button);
        if (button == 1 && x >= this.searchTextField.x && x < this.searchTextField.x + this.searchTextField.width && y >= this.searchTextField.y && y < this.searchTextField.y + this.searchTextField.height) {
            this.searchTextField.setText("");
            this.updateSearchField("");
            this.modList.filterAndUpdateList("");
            return;
        }
        String text = this.searchTextField.getText();
        long currentTine = Minecraft.getSystemTime();
        if (button == 0 && !text.isEmpty() && currentTine - this.lastClickTime < 250L) {
            text += this.searchTextField.getSuggestion();
            this.searchTextField.setText(text);
            this.updateSearchField(text);
            this.modList.filterAndUpdateList(text);
        }
        this.lastClickTime = currentTine;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.searchTextField.updateCursorCounter();
    }

    // 工具方法

    /**
     * Draws everything considered left of the screen; title, search bar and mod list.
     *
     * @param mouseX       the current mouse x position
     * @param mouseY       the current mouse y position
     * @param partialTicks the partial ticks
     */
    private void drawModList(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
        ScreenUtil.blit(this.modList.right - 24, 10, 24, 0, 8, 8, 64, 16);
        GlStateManager.disableBlend();

        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        drawString(this.fontRenderer, TextFormatting.BOLD + I18n.format("catalogue.gui.title"), 70, 10, 0xFFFFFF);
        this.searchTextField.drawTextBox();

        if(ScreenUtil.isMouseWithin(this.modList.right - 14, 7, 14, 14, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("catalogue.gui.filter_updates"));
            this.tooltipYOffset = 10;
        }
    }

    /**
     * Draws everything considered right of the screen; logo, mod title, description and more.
     *
     * @param mouseX       the current mouse x position
     * @param mouseY       the current mouse y position
     * @param partialTicks the partial ticks
     */
    private void drawModInfo(int mouseX, int mouseY, float partialTicks) {
        this.drawVerticalLine(this.modList.right + 11, -1, this.height, 0xFF707070);
        drawRect(this.modList.right + 12, 0, this.width, this.height, 0x66000000);
        this.descriptionList.drawScreen(mouseX, mouseY, partialTicks);

        int contentLeft = this.modList.right + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;

        if(this.selectedModInfo != null) {
            // Draw mod logo
            this.drawLogo(contentWidth, contentLeft, 10, this.width - (this.modList.right + 12 + 10) - 10, 50);

            // Draw mod name
            GlStateManager.pushMatrix();
            GlStateManager.translate(contentLeft, 70, 0);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            drawString(this.fontRenderer, this.selectedModInfo.getName(), 0, 0, 0xFFFFFF);
            GlStateManager.popMatrix();

            // Draw version
            String modId = TextFormatting.DARK_GRAY + I18n.format("catalogue.gui.modid", this.selectedModInfo.getModId());
            int modIdWidth = this.fontRenderer.getStringWidth(modId);
            drawString(this.fontRenderer, modId, contentLeft + contentWidth - modIdWidth, 92, 0xFFFFFF);

//            // Set tooltip for secure mod features forge has
//            if(ScreenUtil.isMouseWithin(contentLeft + contentWidth - modIdWidth, 92, modIdWidth, this.font.lineHeight, mouseX, mouseY)) {
//                if(FMLEnvironment.secureJarsEnabled) {
//                    this.setActiveTooltip(ForgeI18n.parseMessage("fml.menu.mods.info.signature", ((ModInfo) this.selectedModInfo).getOwningFile().getCodeSigningFingerprint().orElse(ForgeI18n.parseMessage("fml.menu.mods.info.signature.unsigned"))));
//                    this.setActiveTooltip(ForgeI18n.parseMessage("fml.menu.mods.info.trust", ((ModInfo) this.selectedModInfo).getOwningFile().getTrustData().orElse(ForgeI18n.parseMessage("fml.menu.mods.info.trust.noauthority"))));
//                } else {
//                    this.setActiveTooltip(ForgeI18n.parseMessage("fml.menu.mods.info.securejardisabled"));
//                }
//            }

            // Draw version
            this.drawStringWithLabel("catalogue.gui.version", this.selectedModInfo.getVersion(), contentLeft, 92, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);

            // Draws an icon if there is an update for the mod
            ForgeVersion.CheckResult result = ForgeVersion.getResult(this.selectedModInfo);
            if(result.status.shouldDraw() && result.url != null) {
                String version = I18n.format("catalogue.gui.version", this.selectedModInfo.getVersion());
                int versionWidth = this.fontRenderer.getStringWidth(version);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = result.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                ScreenUtil.blit(contentLeft + versionWidth + 5, 92, result.status.getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
                if(ScreenUtil.isMouseWithin(contentLeft + versionWidth + 5, 92, 8, 8, mouseX, mouseY)) {
                    this.setActiveTooltip(I18n.format("catalogue.gui.update_available", result.url));
                }
            }

            ModMetadata metadata = selectedModInfo.getMetadata();
            if (metadata != null && !metadata.autogenerated) {

                int labelOffset = this.height - 20;

//            // Draw license
//            String license = this.selectedModInfo.getMetadata().getLicense();
//            this.drawStringWithLabel("fml.menu.mods.info.license", license, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
//            labelOffset -= 15;

                // Draw credits
                String credits = metadata.credits;
                if(!credits.isEmpty()) {
                    this.drawStringWithLabel("catalogue.gui.credits", credits, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                    labelOffset -= 15;
                }

                // Draw authors
                String authors = metadata.getAuthorList();
                if(!authors.isEmpty()) {
                    this.drawStringWithLabel("catalogue.gui.authors", authors, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                }
            }
        } else {
            String message = TextFormatting.GRAY + I18n.format("catalogue.gui.no_selection");
            drawCenteredString(this.fontRenderer, message, contentLeft + contentWidth / 2, this.height / 2 - 5, 0xFFFFFF);
        }
    }

    /**
     * Draws a string and prepends a label. If the formed string and label is longer than the
     * specified max width, it will automatically be trimmed and allows the user to hover the
     * string with their mouse to read the full contents.
     *
     * @param format      a string to prepend to the content
     * @param text        the string to render
     * @param x           the x position
     * @param y           the y position
     * @param maxWidth    the maximum width the string can render
     * @param mouseX      the current mouse x position
     * @param mouseY      the current mouse u position
     */
    @SuppressWarnings("SameParameterValue")
    private void drawStringWithLabel(String format, String text, int x, int y, int maxWidth, int mouseX, int mouseY, TextFormatting labelColor, TextFormatting contentColor) {
        String formatted = I18n.format(format, text); // Attempting to keep Forge's lang since it's already support many languages
        String label = formatted.substring(0, formatted.indexOf(":") + 1);
        String content = formatted.substring(formatted.indexOf(":") + 1);
        if(this.fontRenderer.getStringWidth(formatted) > maxWidth) {
            content = this.fontRenderer.trimStringToWidth(content, maxWidth - this.fontRenderer.getStringWidth(label) - 7) + "...";
            String credits = labelColor + label;
            credits += contentColor + content;
            drawString(this.fontRenderer, credits, x, y, 0xFFFFFF);
            if(ScreenUtil.isMouseWithin(x, y, maxWidth, 9, mouseX, mouseY)) { // Sets the active tool tip if string is too long so users can still read it
                this.setActiveTooltip(text);
            }
        } else {
            drawString(this.fontRenderer, labelColor + label + contentColor + content, x, y, 0xFFFFFF);
        }
    }

    private void loadAndCacheLogo(ModContainer info) {
        if (LOGO_CACHE.containsKey(info.getModId())) {
            return;
        }

        LOGO_CACHE.put(info.getModId(), Pair.of(null, new Size2i(0, 0)));

        ModMetadata metadata = info.getMetadata();
        if (metadata != null && !metadata.logoFile.isEmpty()) {
            String logoFile = metadata.logoFile;
            TextureManager tm = mc.getTextureManager();

            try {
                BufferedImage logo = null;
                IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(info.getModId());

                if (pack != null) {
                    logo = pack.getPackImage();
                } else {
                    InputStream logoResource = getClass().getResourceAsStream(logoFile);
                    if (logoResource != null) {
                        logo = TextureUtil.readBufferedImage(logoResource);
                    }
                }

                if (logo != null) {
                    DynamicTexture texture = new DynamicTexture(logo);
                    ResourceLocation textureId = tm.getDynamicTextureLocation("modlogo", texture);
                    LOGO_CACHE.put(info.getModId(), Pair.of(textureId, new Size2i(logo.getWidth(), logo.getHeight())));
                }
            } catch (IOException e) {
                Catalogue.LOGGER.warn("Failed to load logo for mod {}", info.getModId(), e);
            }
        }
    }

    // Not working at all.
    private void loadAndCacheIcon(ModContainer info) {
        if(ICON_CACHE.containsKey(info.getModId()))
            return;

        // Fills an empty icon as icon may not be present
        ICON_CACHE.put(info.getModId(), Pair.of(null, new Size2i(0, 0)));

        // Attempts to load the real icon
        ModContainer modInfo = (ModContainer) info;
        if (modInfo.getCustomModProperties().containsKey("catalogueImageIcon")) {
            String s = (String) modInfo.getCustomModProperties().get("catalogueImageIcon");

            if (s.isEmpty()) return;

            if (s.contains("/") || s.contains("\\")) {
                Catalogue.LOGGER.warn("Skipped loading Catalogue icon file from {}. The file name '{}' contained illegal characters '/' or '\\'", info.getName(), s);
                //return;
            }

            IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(info.getModId());
            if (resourcePack == null) FMLClientHandler.instance().getResourcePackFor("forge");
            if (resourcePack == null) throw new RuntimeException("Can't find forge, WHAT!");
            try (InputStream is = resourcePack.getInputStream(new ResourceLocation(s))) {
                BufferedImage icon = TextureUtil.readBufferedImage(is);
                TextureManager textureManager = this.mc.getTextureManager();
                ICON_CACHE.put(info.getModId(), Pair.of(textureManager.getDynamicTextureLocation("catalogueicon", this.createLogoTexture(icon, false)), new Size2i(icon.getWidth(), icon.getHeight())));
                return;
            } catch(IOException ignored) {}
        }

        // Attempts to use the logo file if it's a square
        ModMetadata metadata = modInfo.getMetadata();
        if (metadata == null) return;
        String s = metadata.logoFile;
        if (s.isEmpty()) return;

        if (s.contains("/") || s.contains("\\")) {
            Catalogue.LOGGER.warn("Skipped loading logo file from {}. The file name '{}' contained illegal characters '/' or '\\'", info.getName(), s);
            //return;
        }

        IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(info.getModId());
        if (resourcePack == null) FMLClientHandler.instance().getResourcePackFor("forge");
        if (resourcePack == null) throw new RuntimeException("Can't find forge, WHAT!");
        try (InputStream is = resourcePack.getInputStream(new ResourceLocation(s))) {
            BufferedImage logo = TextureUtil.readBufferedImage(is);
            if (logo.getWidth() == logo.getHeight()) {
                TextureManager textureManager = this.mc.getTextureManager();
                String modId = info.getModId();

                /* The first selected mod will have it's logo cached before the icon, so we
                 * can just use the logo instead of loading the image again. */
                if (LOGO_CACHE.containsKey(modId)) {
                    if (LOGO_CACHE.get(modId).getLeft() != null) {
                        ICON_CACHE.put(modId, LOGO_CACHE.get(modId));
                        return;
                    }
                }

                /* Since the icon will be same as the logo, we can cache into both icon and logo cache */
                DynamicTexture texture = this.createLogoTexture(logo, true);
                Size2i size = new Size2i(logo.getWidth(), logo.getHeight());
                ResourceLocation textureId = textureManager.getDynamicTextureLocation("catalogueicon", texture);
                ICON_CACHE.put(modId, Pair.of(textureId, size));
                LOGO_CACHE.put(modId, Pair.of(textureId, size));
            }
        }
        catch(IOException ignored) {}
    }

    private DynamicTexture createLogoTexture(BufferedImage image, boolean smooth) {
        return new DynamicTexture(image) {
            @Override
            public void updateDynamicTexture() {
                TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, smooth, false);
            }
        };
    }

    @SuppressWarnings("SameParameterValue")
    private void drawLogo(int contentWidth, int x, int y, int maxWidth, int maxHeight) {
        if(this.selectedModInfo != null) {
            ResourceLocation logoResource = MISSING_BANNER;
            Size2i size = new Size2i(600, 120);

            if(LOGO_CACHE.containsKey(this.selectedModInfo.getModId())) {
                Pair<ResourceLocation, Size2i> logoInfo = LOGO_CACHE.get(this.selectedModInfo.getModId());
                if(logoInfo.getLeft() != null) {
                    logoResource = logoInfo.getLeft();
                    size = logoInfo.getRight();
                }
            }

            mc.getTextureManager().bindTexture(logoResource);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();

            int width = size.width;
            int height = size.height;
            if (size.width > maxWidth) {
                width = maxWidth;
                height = (width * size.height) / size.width;
            }
            if (height > maxHeight) {
                height = maxHeight;
                width = (height * size.width) / size.height;
            }

            x += (contentWidth - width) / 2;
            y += (maxHeight - height) / 2;

            ScreenUtil.blit(x, y, width, height, 0.0F, 0.0F, size.width, size.height, size.width, size.height);

            GlStateManager.disableBlend();
        }
    }

    private void setActiveTooltip(String content) {
        this.activeTooltip = this.fontRenderer.listFormattedStringToWidth(content, 200);
        this.tooltipYOffset = 0;
    }

    private void setSelectedModInfo(ModContainer selectedModInfo) {
        this.selectedModInfo = selectedModInfo;
        this.loadAndCacheLogo(selectedModInfo);
        this.configButton.visible = true;
        this.websiteButton.visible = true;
        this.issueButton.visible = true;

        this.configButton.enabled = false;
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(selectedModInfo);
        if (guiFactory != null) {
            this.configButton.enabled = guiFactory.hasConfigGui();
        }

        ModMetadata metadata = selectedModInfo.getMetadata();
        if (metadata != null && !metadata.autogenerated) {
            this.websiteButton.enabled = !metadata.url.isEmpty();
        }

        this.issueButton.enabled = false;
//        this.issueButton.active = ((ModInfo) selectedModInfo).getOwningFile().getConfigElement("issueTrackerURL").isPresent();

        int contentLeft = this.modList.right + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;
        int labelCount = this.getLabelCount(selectedModInfo);
        this.descriptionList.updateSize(contentWidth, this.height - 135 - 10 - labelCount * 15, 130, this.height - 10 - labelCount * 15);
        this.descriptionList.setSlotXBoundsFromLeft(contentLeft);
        this.descriptionList.setTextFromInfo(selectedModInfo);
        this.descriptionList.setAmountScrolled(0);
    }

    private int getLabelCount(ModContainer selectedModInfo) {
        int count = 1; //1 by default since license property will always exist

        ModMetadata metadata = selectedModInfo.getMetadata();
        if (metadata != null && !metadata.autogenerated) {
            if (!metadata.credits.isEmpty()) count++;
            if (!metadata.authorList.isEmpty()) count++;
        }

        return count;
    }

    private void updateSelectedModList() {
        ModEntry selectedEntry = this.modList.getEntryFromInfo(this.selectedModInfo);
        if(selectedEntry != null) {
            this.modList.selectMod(selectedEntry);
        }
    }

    private void updateSearchField(String value) {
        if(value.isEmpty()) {
            this.searchTextField.setSuggestion(I18n.format("catalogue.gui.search"));
        } else {
            Optional<ModContainer> optional = Loader.instance().getActiveModList().stream().filter(info -> {
                return info.getName().toLowerCase(Locale.ENGLISH).startsWith(value.toLowerCase(Locale.ENGLISH));
            }).min(Comparator.comparing(ModContainer::getName));
            if(optional.isPresent()) {
                int length = value.length();
                String displayName = optional.get().getName();
                this.searchTextField.setSuggestion(displayName.substring(length));
            } else {
                this.searchTextField.setSuggestion("");
            }
        }
    }


}
