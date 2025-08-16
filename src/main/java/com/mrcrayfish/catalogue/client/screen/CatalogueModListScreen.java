package com.mrcrayfish.catalogue.client.screen;

import com.google.common.collect.Lists;
import com.mrcrayfish.catalogue.Catalogue;
import com.mrcrayfish.catalogue.client.ScreenUtil;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueCheckBoxButton;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueIconButton;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueTextField;
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

    public CatalogueModListScreen() {
        super();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.searchTextField = new CatalogueTextField(0, this.fontRenderer, 11, 25, 148, 20);
        this.searchTextField.setFocused(true);
        this.searchTextField.setCanLoseFocus(true);
        this.searchTextField.setText("");
        this.searchTextField.setEnableBackgroundDrawing(true);
        this.searchTextField.setSuggestion(I18n.format("catalogue.gui.search"));

        this.modList = new ModList();
        this.modList.registerScrollButtons(1, 2);
        this.modList.filterAndUpdateList("");

        this.buttonList.add(new GuiButton(3, 10, modList.bottom + 8, 127, 20, I18n.format("gui.back")));
        this.modFolderButton = this.addButton(new CatalogueIconButton(4, 140, modList.bottom + 8, 0, 0));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.activeTooltip = null;
        this.drawDefaultBackground();
        this.drawModList(mouseX, mouseY, partialTicks);
//        this.drawModInfo(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);


        Pair<ResourceLocation, Size2i> pair = LOGO_CACHE.get("catalogue");
        if(pair != null && pair.getLeft() != null) {
            ResourceLocation textureId = pair.getLeft();
            Size2i size = pair.getRight();
            mc.getTextureManager().bindTexture(textureId);
            this.drawTexturedModalRect(10, 9, 0, 0, 10, 10);
        }

        if(this.modFolderButton.isMouseOver()) {
            this.setActiveTooltip(I18n.format("catalogue.gui.open_mods_folder"));
        }

        if(this.activeTooltip != null) {
            this.drawHoveringText(this.activeTooltip, mouseX, mouseY + this.tooltipYOffset);
            this.tooltipYOffset = 0;
        }

    }

    @Override
    protected void keyTyped(char typedChar, int key) throws IOException {
        if (this.searchTextField.textboxKeyTyped(typedChar, key)) {
            this.modList.filterAndUpdateList(this.searchTextField.getText());
            return;
        }

        super.keyTyped(typedChar, key);
    }

    private class ModList extends GuiListExtended {
        private List<ModEntry> entries = Lists.<ModEntry>newArrayList();
        private int selectedIndex = -1;

        public ModList() {
            super(CatalogueModListScreen.this.mc, 150, CatalogueModListScreen.this.height, 46, CatalogueModListScreen.this.height - 35, 26);
            this.left = 10;
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
            //CatalogueModListScreen.this.setSelectedModInfo(entry.info);
        }

        public void filterAndUpdateList(String text) {
            entries.clear();
            this.entries = Loader.instance().getActiveModList().stream()
                    .filter(info -> info.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH)))
                    .map(info -> new ModEntry(info, this))
                    .sorted(Comparator.comparing(entry -> entry.info.getName()))
                    .collect(Collectors.toList());

            this.selectedIndex = -1;
        }

        public CatalogueModListScreen.ModEntry getEntryFromInfo(ModContainer info) {
            for (CatalogueModListScreen.ModEntry entry : entries) {
                if (entry.info == info) {
                    return entry;
                }
            }
            return null;
        }

        public void selectMod(int selectedIndex) {
            this.selectedIndex = selectedIndex;
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.entries.get(index);
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

    // 定义 Entry
    private class ModEntry implements GuiListExtended.IGuiListEntry {
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
            drawString(fontRenderer, TextFormatting.GRAY + this.info.getVersion(), left + 24, top + 12, 0xFFFFFF);

            CatalogueModListScreen.this.loadAndCacheIcon(this.info);

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
            if (result.status != ForgeVersion.Status.UP_TO_DATE) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = result.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                drawTexturedModalRect(left + rowWidth - 8 - 10, top + 6, result.status.getSheetOffset() * 8, vOffset, 8, 8);
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
            int width = this.list.width - (this.list.getMaxScroll() > 0 ? 30 : 24);
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

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.modList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        this.searchTextField.mouseClicked(x, y, button);
        if (button == 1 && x >= this.searchTextField.x && x < this.searchTextField.x + this.searchTextField.width && y >= this.searchTextField.y && y < this.searchTextField.y + this.searchTextField.height) {
            this.searchTextField.setText("");
            this.modList.filterAndUpdateList("");
        }
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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
        this.drawTexturedModalRect(this.modList.left + this.modList.width - 24, 10, 24, 0, 8, 8);

        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        drawString(this.fontRenderer, TextFormatting.BOLD + I18n.format("catalogue.gui.title"), 70, 10, 0xFFFFFF);
        this.searchTextField.drawTextBox();

        if(ScreenUtil.isMouseWithin(this.modList.left + this.modList.width - 14, 7, 14, 14, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("fml.menu.mods.filter_updates"));
            this.tooltipYOffset = 10;
        }
    }

    // 加载logo
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

    // 加载图标
    private void loadAndCacheIcon(ModContainer info) {
//        if(ICON_CACHE.containsKey(info.getModId()))
//            return;
//
//        // Fills an empty icon as icon may not be present
//        ICON_CACHE.put(info.getModId(), Pair.of(null, new Size2i(0, 0)));
//
//        // Attempts to load the real icon
//        ModContainer modInfo = info;
//        if(modInfo.getCustomModProperties().containsKey("catalogueImageIcon")) {
//            String s = modInfo.getCustomModProperties().get("catalogueImageIcon");
//
//            if(s.isEmpty())
//                return;
//
//            if(s.contains("/") || s.contains("\\"))
//            {
//                Catalogue.LOGGER.warn("Skipped loading Catalogue icon file from {}. The file name '{}' contained illegal characters '/' or '\\'", info.getDisplayName(), s);
//                return;
//            }
//
//            ModFileResourcePack resourcePack = ResourcePackLoader.getResourcePackFor(info.getModId()).orElse(ResourcePackLoader.getResourcePackFor("forge").orElseThrow(() -> new RuntimeException("Can't find forge, WHAT!")));
//            try(InputStream is = resourcePack.getRootResource(s); NativeImage icon = NativeImage.read(is)) {
//                TextureManager textureManager = this.getMinecraft().getTextureManager();
//                ICON_CACHE.put(info.getModId(), Pair.of(textureManager.register("catalogueicon", this.createLogoTexture(icon, false)), new Size2i(icon.getWidth(), icon.getHeight())));
//                return;
//            } catch(IOException ignored) {}
//        }
//
//        // Attempts to use the logo file if it's a square
//        modInfo.getLogoFile().ifPresent(s ->
//        {
//            if(s.isEmpty())
//                return;
//
//            if(s.contains("/") || s.contains("\\"))
//            {
//                Catalogue.LOGGER.warn("Skipped loading logo file from {}. The file name '{}' contained illegal characters '/' or '\\'", info.getDisplayName(), s);
//                return;
//            }
//
//            ModFileResourcePack resourcePack = ResourcePackLoader.getResourcePackFor(info.getModId()).orElse(ResourcePackLoader.getResourcePackFor("forge").orElseThrow(() -> new RuntimeException("Can't find forge, WHAT!")));
//            try(InputStream is = resourcePack.getRootResource(s); NativeImage logo = NativeImage.read(is))
//            {
//                if(logo.getWidth() == logo.getHeight())
//                {
//                    TextureManager textureManager = this.getMinecraft().getTextureManager();
//                    String modId = info.getModId();
//
//                    /* The first selected mod will have it's logo cached before the icon, so we
//                     * can just use the logo instead of loading the image again. */
//                    if(LOGO_CACHE.containsKey(modId))
//                    {
//                        if(LOGO_CACHE.get(modId).getLeft() != null)
//                        {
//                            ICON_CACHE.put(modId, LOGO_CACHE.get(modId));
//                            return;
//                        }
//                    }
//
//                    /* Since the icon will be same as the logo, we can cache into both icon and logo cache */
//                    DynamicTexture texture = this.createLogoTexture(logo, modInfo.getLogoBlur());
//                    Size2i size = new Size2i(logo.getWidth(), logo.getHeight());
//                    ResourceLocation textureId = textureManager.register("catalogueicon", texture);
//                    ICON_CACHE.put(modId, Pair.of(textureId, size));
//                    LOGO_CACHE.put(modId, Pair.of(textureId, size));
//                }
//            }
//            catch(IOException ignored) {}
//        });
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
//        this.configButton.active = ConfigGuiHandler.getGuiFactoryFor((ModInfo) selectedModInfo).isPresent();
//        this.websiteButton.active = ((ModInfo) selectedModInfo).getConfigElement("displayURL").isPresent();
//        this.issueButton.active = ((ModInfo) selectedModInfo).getOwningFile().getConfigElement("issueTrackerURL").isPresent();
//        int contentLeft = this.modList.getRight() + 12 + 10;
//        int contentWidth = this.width - contentLeft - 10;
//        int labelCount = this.getLabelCount(selectedModInfo);
//        this.descriptionList.updateSize(contentWidth, this.height - 135 - 10 - labelCount * 15, 130, this.height - 10 - labelCount * 15);
//        this.descriptionList.setLeftPos(contentLeft);
//        this.descriptionList.setTextFromInfo(selectedModInfo);
//        this.descriptionList.setScrollAmount(0);
    }

    private static class Size2i {
        public final int width, height;

        public Size2i(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
