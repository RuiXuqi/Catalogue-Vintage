package com.mrcrayfish.catalogue.client.screen;

import com.mrcrayfish.catalogue.client.ScreenUtil;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueCheckBoxButton;
import com.mrcrayfish.catalogue.client.screen.widget.CatalogueIconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.List;

// Code by deepseek. DO NOT USE DIRECTLY.
public class CatalogueModListScreenTemp extends GuiScreen {
    private static final Comparator<ModEntry> SORT = Comparator.comparing(o -> o.getInfo().getName());
    private static final ResourceLocation MISSING_BANNER = new ResourceLocation("catalogue", "textures/gui/missing_banner.png");
    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation("forge", "textures/gui/version_check_icons.png");
    private static final Map<String, Pair<ResourceLocation, Size2i>> LOGO_CACHE = new HashMap<>();
    private static final Map<String, Pair<ResourceLocation, Size2i>> ICON_CACHE = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_CACHE = new HashMap<>();

    private GuiTextField searchTextField;
    private ModList modList;
    private ModContainer selectedMod;
    private GuiButton modFolderButton;
    private GuiButton configButton;
    private GuiButton websiteButton;
    private GuiButton issueButton;
    private CatalogueCheckBoxButton updatesButton;
    private StringList descriptionList;
    private List<String> activeTooltip;
    private int tooltipYOffset;

    public CatalogueModListScreenTemp() {
        super();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.searchTextField = new GuiTextField(0, this.fontRenderer, 11, 25, 148, 20);
        this.searchTextField.setFocused(true);
        this.searchTextField.setText("");
        this.searchTextField.setEnableBackgroundDrawing(true);

        this.modList = new ModList(this, this.mc);
        this.modList.registerScrollButtons(7, 8);

        this.buttonList.add(new GuiButton(10, (this.width - 200) / 2, this.height - 28, 200, 20, I18n.format("gui.done")));
        this.modFolderButton = this.addButton(new CatalogueIconButton(11, this.width - 24, this.height - 24, 0, 0));

        int padding = 10;
        int contentLeft = this.width - 180;
        int contentWidth = 170;
        int buttonWidth = (contentWidth - padding) / 3;

        this.configButton = this.addButton(new CatalogueIconButton(12, contentLeft, 105, 10, 0, buttonWidth, I18n.format("fml.menu.mods.config")));
        this.configButton.visible = false;

        this.websiteButton = this.addButton(new CatalogueIconButton(13, contentLeft + buttonWidth + 5, 105, 20, 0, buttonWidth, I18n.format("catalogue.gui.website")));
        this.websiteButton.visible = false;

        this.issueButton = this.addButton(new CatalogueIconButton(14, contentLeft + buttonWidth * 2 + 10, 105, 30, 0, buttonWidth, I18n.format("catalogue.gui.issue")));
        this.issueButton.visible = false;

        this.descriptionList = new StringList(this.mc, contentWidth, this.height - 135 - 55, 130, contentLeft);
        this.updatesButton = this.addButton(new CatalogueCheckBoxButton(15, this.width - 30, 7, false));

        this.modList.filterAndUpdateList(this.searchTextField.getText());

        if(this.selectedMod != null) {
            this.setSelectedModInfo(this.selectedMod);
            this.updateSelectedModList();
            ModEntry entry = this.modList.getEntryFromInfo(this.selectedMod);
            if(entry != null) {
                this.modList.scrollToIndex(this.modList.getIndexForEntry(entry));
            }
        }
        this.updateSearchField(this.searchTextField.getText());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.activeTooltip = null;

        // 绘制标题
        String title = I18n.format("catalogue.gui.title");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 10, 0xFFFFFF);

        // 绘制模组列表
        this.drawModList(mouseX, mouseY, partialTicks);

        // 绘制模组信息
        this.drawModInfo(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // 绘制搜索框
        this.searchTextField.drawTextBox();

        // 绘制工具提示
        if(this.activeTooltip != null) {
            this.drawHoveringText(this.activeTooltip, mouseX, mouseY + this.tooltipYOffset);
            this.tooltipYOffset = 0;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.modList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }

        if (this.searchTextField.textboxKeyTyped(typedChar, keyCode)) {
            this.modList.filterAndUpdateList(this.searchTextField.getText());
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 10) {
            // 完成按钮
            this.mc.displayGuiScreen(null);
        }
        else if (button.id == 11) {
            // 打开模组文件夹
            //Util.getOSType().openFile(Loader.instance().getConfigDir().getParentFile());
        }
        else if (button.id == 12) {
            // 配置按钮
            if(this.selectedMod != null) {
                // 打开配置界面
                try {
                    IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(selectedMod);
                    GuiScreen newScreen = guiFactory.createConfigGui(this);
                    this.mc.displayGuiScreen(newScreen);
                } catch (Exception e) {
                    FMLLog.log.error("There was a critical issue trying to build the config GUI for {}", selectedMod.getModId(), e);
                }
            }
        }
        else if (button.id == 13) {
            // 网站按钮
            if(this.selectedMod != null) {
                this.openLink(getModProperty("displayURL", this.selectedMod));
            }
        }
        else if (button.id == 14) {
            // 问题报告按钮
            if(this.selectedMod != null) {
                this.openLink(getModProperty("issueTrackerURL", this.selectedMod));
            }
        }
        else if (button.id == 15) {
            // 更新过滤按钮
            if (button instanceof CatalogueCheckBoxButton) {
                CatalogueCheckBoxButton checkBox = (CatalogueCheckBoxButton) button;
                checkBox.setSelected(!checkBox.selected());
                this.modList.filterAndUpdateList(this.searchTextField.getText());
                this.updateSelectedModList();
            }
        }
    }

    private void drawModList(int mouseX, int mouseY, float partialTicks) {
        this.modList.drawScreen(mouseX, mouseY, partialTicks);

        // 绘制更新按钮提示
        if (ScreenUtil.isMouseWithin(this.width - 30, 7, 14, 14, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("catalogue.gui.filter_updates"));
        }
    }

    private void drawModInfo(int mouseX, int mouseY, float partialTicks) {
        int contentLeft = this.width - 180;
        int contentWidth = 170;

        // 绘制分隔线
        drawVerticalLine(contentLeft - 5, 30, this.height - 30, 0xFFAAAAAA);

        // 绘制模组信息区域背景
        drawRect(contentLeft, 30, this.width, this.height - 30, 0x66000000);

        // 绘制选中的模组信息
        if (this.selectedMod != null) {
            // 绘制模组名称
            String modName = this.selectedMod.getName();
            this.fontRenderer.drawStringWithShadow(modName, contentLeft + 10, 40, 0xFFFFFF);

            // 绘制模组版本
            String version = I18n.format("catalogue.gui.version", this.selectedMod.getDisplayVersion());
            this.fontRenderer.drawString(version, contentLeft + 10, 60, 0xCCCCCC);

            // 绘制模组ID
            String modId = I18n.format("catalogue.gui.modid", this.selectedMod.getModId());
            this.fontRenderer.drawString(modId, contentLeft + 10, 75, 0xAAAAAA);

            // 绘制描述
            this.descriptionList.drawScreen(mouseX, mouseY, partialTicks);
        } else {
            // 没有选择模组时的提示
            String noSelection = I18n.format("catalogue.gui.no_selection");
            this.drawCenteredString(this.fontRenderer, noSelection, contentLeft + contentWidth / 2, this.height / 2 - 5, 0xAAAAAA);
        }
    }

    private void openLink(String url) {
        if (url != null && !url.isEmpty()) {
            //net.minecraft.util.Util.getOSType().openURI(url);
        }
    }

    private String getModProperty(String key, ModContainer mod) {
        if (mod == null) return "";
        ModMetadata meta = mod.getMetadata();
        if (meta == null) return "";

        switch (key) {
            case "displayURL": return meta.url;
            case "issueTrackerURL": return meta.updateUrl;
            default: return "";
        }
    }

    private void updateSearchField(String value) {
        if (value.isEmpty()) {
            this.searchTextField.setText(I18n.format("fml.menu.mods.search"));
        }
    }

    private void updateSelectedModList() {
        if (this.selectedMod != null) {
            ModEntry entry = this.modList.getEntryFromInfo(this.selectedMod);
            if (entry != null) {
                this.modList.selectedIndex = this.modList.getIndexForEntry(entry);
            }
        }
    }

    private void setActiveTooltip(String content) {
        this.activeTooltip = this.fontRenderer.listFormattedStringToWidth(content, 200);
        this.tooltipYOffset = 0;
    }

    private void setSelectedModInfo(ModContainer mod) {
        this.selectedMod = mod;
        this.configButton.visible = mod != null;
        this.websiteButton.visible = mod != null;
        this.issueButton.visible = mod != null;

        if (mod != null) {
            // 更新配置按钮状态
            //this.configButton.enabled = FMLClientHandler.instance().hasConfigGui(mod);

            // 更新描述列表
            this.descriptionList.setTextFromInfo(mod);

            // 更新网站按钮状态
            this.websiteButton.enabled = !getModProperty("displayURL", mod).isEmpty();

            // 更新问题按钮状态
            this.issueButton.enabled = !getModProperty("issueTrackerURL", mod).isEmpty();
        }
    }

    private class ModList extends GuiListExtended {
        private final List<ModEntry> entries = new ArrayList<>();
        private final Minecraft mc;
        private final CatalogueModListScreenTemp parent;
        private int selectedIndex = -1;

        public ModList(CatalogueModListScreenTemp parent, Minecraft mc) {
            super(mc, parent.width - 220, parent.height, 30, parent.height - 35, 26);
            this.mc = mc;
            this.parent = parent;
        }

        public void filterAndUpdateList(String text) {
            entries.clear();
            List<ModContainer> mods = new ArrayList<>(Loader.instance().getActiveModList());
            mods.sort(Comparator.comparing(ModContainer::getName));

            for (ModContainer mod : mods) {
                if (mod.getName().toLowerCase().contains(text.toLowerCase())) {
                    entries.add(new ModEntry(mod, this));
                }
            }
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return entries.get(index);
        }

        @Override
        protected int getSize() {
            return entries.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            ModEntry entry = entries.get(slotIndex);
            parent.setSelectedModInfo(entry.info);
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        public ModEntry getEntryFromInfo(ModContainer info) {
            for (ModEntry entry : entries) {
                if (entry.info == info) {
                    return entry;
                }
            }
            return null;
        }

        public int getIndexForEntry(ModEntry entry) {
            return entries.indexOf(entry);
        }

        public void scrollToIndex(int index) {
            int maxScroll = Math.max(0, this.getSize() * this.slotHeight + this.headerPadding - (this.bottom - this.top - 4));
            this.amountScrolled = Math.min(index * this.slotHeight, maxScroll);
        }
    }

    private class ModEntry implements GuiListExtended.IGuiListEntry {
        private final ModContainer info;
        private final ModList list;

        public ModEntry(ModContainer info, ModList list) {
            this.info = info;
            this.list = list;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            // 绘制选中背景
            if (isSelected) {
                drawRect(x, y, x + listWidth, y + slotHeight, 0x33FFFFFF);
            }

            // 绘制模组名称
            String name = this.info.getName();
            if (fontRenderer.getStringWidth(name) > listWidth - 30) {
                name = fontRenderer.trimStringToWidth(name, listWidth - 35) + "...";
            }

            fontRenderer.drawStringWithShadow(name, x + 24, y + 2, 0xFFFFFF);
            fontRenderer.drawString(this.info.getDisplayVersion(), x + 24, y + 12, 0xCCCCCC);

            // 绘制图标
            ItemStack icon = getItemIcon();
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(icon, x + 4, y + 2);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();

            // 检查更新状态
            ForgeVersion.CheckResult result = ForgeVersion.getResult(this.info);
            if (result.status != ForgeVersion.Status.UP_TO_DATE) {
                mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = result.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                drawTexturedModalRect(x + listWidth - 20, y + 6, result.status.getSheetOffset() * 8, vOffset, 8, 8);
            }
        }

        private ItemStack getItemIcon() {
            String modId = info.getModId();
            if (ITEM_CACHE.containsKey(modId)) {
                return ITEM_CACHE.get(modId);
            }

            // 尝试获取自定义图标
            ModMetadata meta = info.getMetadata();
            if (meta != null) {
                // 检查metadata中是否有自定义图标
                if (meta.logoFile != null && !meta.logoFile.isEmpty()) {
                    // 这里可以加载自定义图标，但简化处理
                }
            }

            // 默认图标
            ItemStack icon = new ItemStack(Items.BOOK);

            // 特殊模组图标
            if ("minecraft".equals(modId)) {
                icon = new ItemStack(Blocks.GRASS);
            } else if ("forge".equals(modId)) {
                icon = new ItemStack(Blocks.ANVIL);
            }

            ITEM_CACHE.put(modId, icon);
            return icon;
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            list.elementClicked(slotIndex, false, mouseX, mouseY);
            return true;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }

        public ModContainer getInfo() {
            return info;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks){}
    }

    private class StringList extends GuiScrollingList {
        private final List<String> lines = new ArrayList<>();
        private final int top;
        private final int left;

        public StringList(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, top + height, left, 10);
            this.top = top;
            this.left = left;
        }

        public void setTextFromInfo(ModContainer info) {
            lines.clear();
            if (info != null && info.getMetadata() != null) {
                String description = info.getMetadata().description;
                if (description != null) {
                    lines.addAll(fontRenderer.listFormattedStringToWidth(description, listWidth - 10));
                }
            }
        }

        @Override
        protected int getSize() {
            return lines.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
        }

        @Override
        protected boolean isSelected(int index) {
            return false;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            String line = lines.get(slotIdx);
            fontRenderer.drawString(line, left + 5, slotTop + 2, 0xFFFFFF);
        }
    }

    private static class Size2i {
        public final int width, height;

        public Size2i(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}