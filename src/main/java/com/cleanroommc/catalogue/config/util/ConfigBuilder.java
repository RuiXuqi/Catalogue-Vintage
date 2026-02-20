package com.cleanroommc.catalogue.config.util;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

// TODO: 更好的数值校验，Forge 自带的不能处理好很多东西
//@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ConfigBuilder {
    private static final Map<Configuration, Map<String, Object>> CONFIG_DEFAULT_VALUES = new HashMap<>();
    private final Map<String, Object> defaultValues;
    private final Configuration config;
    private final boolean writeProp;
    private final LinkedList<String> langKeyPath = new LinkedList<>();
    private IFormatter langKeyFormatter = IFormatter.LOWER_CASE;
    private String langKeyPrefix = "";
    private ConfigCategory currentCategory;

    public static ConfigBuilder startReadingFromFile(Configuration config) {
        config.load();
        return new ConfigBuilder(config, false);
    }

    /**
     * 从 Property 读取值，并写入 currentValue。
     * 这个方法可在 {@link cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent} 调用，
     * 此时 Property 已经写入了新值，currentValue 和配置文件还没有更新。
     */
    public static ConfigBuilder startReadingFromProp(Configuration config) {
        return new ConfigBuilder(config, false);
    }

    /**
     * 将 currentValue 值写入 Property 和配置文件。
     */
    public static ConfigBuilder startSaving(Configuration config) {
        return new ConfigBuilder(config, true);
    }

    /**
     * @param writeProp 是否要将 currentValue 写入 Property。
     */
    private ConfigBuilder(Configuration config, boolean writeProp) {
        this.defaultValues = Optional.ofNullable(CONFIG_DEFAULT_VALUES.get(config)).orElse(new HashMap<>());
        this.config = config;
        this.writeProp = writeProp;
    }

    public void finishBuilding() {
        CONFIG_DEFAULT_VALUES.put(this.config, this.defaultValues);
        if (this.config.hasChanged()) this.config.save();
    }

    public void setLangKeyFormatter(IFormatter formatter) {
        this.langKeyFormatter = formatter;
    }

    public String formatLangKey(String str) {
        return this.langKeyFormatter.format(str);
    }

    /**
     * 设置一个 langKey 的前缀。如 config.modid。
     *
     * @param langKeyPrefix 不需要加“.”。
     */
    public void setLangKeyPrefix(@Nullable String langKeyPrefix) {
        this.langKeyPrefix = StringUtils.isBlank(langKeyPrefix) ? "" : this.formatLangKey(langKeyPrefix);
    }

    public void pushLangKey(String langKey) {
        this.langKeyPath.add(this.formatLangKey(langKey));
    }

    /**
     * 弹出一级 langKey。
     *
     * @return 被弹出的 langKey
     */
    @Nullable
    public String popLangKey() {
        return this.langKeyPath.pollLast();
    }

    /**
     * 获取当前的路径 langKey。
     * 如 config.modid.path。
     */
    public String getCurrentLangKey() {
        if (this.langKeyPath.isEmpty()) return this.langKeyPrefix;
        String path = String.join(".", this.langKeyPath);
        return this.langKeyPrefix.isEmpty() ? path : this.langKeyPrefix + "." + path;
    }

    /**
     * 获取当前路径下的一个子 langKey。
     * 如 {@link #getCurrentLangKey()} 获取到 config.modid.path，那么这个方法就会返回 config.modid.path.name。
     */
    public String getCurrentSubLangKey(String name) {
        String current = this.getCurrentLangKey();
        name = this.formatLangKey(name);
        return current.isEmpty() ? name : current + "." + name;
    }

    public void pushCategory(String category, @Nullable String comment, @Nullable String langKey) {
        this.currentCategory = this.config.getCategory(this.currentCategory != null ? this.currentCategory.getQualifiedName() + Configuration.CATEGORY_SPLITTER + category : category);
        if (StringUtils.isNoneBlank(comment)) this.currentCategory.setComment(comment);
        if (StringUtils.isNoneBlank(langKey)) {
            this.pushLangKey(category);
            this.currentCategory.setLanguageKey(this.getCurrentLangKey());
        }
    }

    /**
     * 推送一级目录的同时使用其名字推送一级 langKey。
     */
    public void pushCategory(String category, @Nullable String comment) {
        this.pushCategory(category, comment, category);
    }

    /**
     * 推送一级目录的同时使用其名字推送一级 langKey。
     */
    public void pushCategory(String category) {
        this.pushCategory(category, null);
    }

    /**
     * 弹出一级目录的同时弹出一级 langKey。
     *
     * @return 被弹出的目录
     */
    @Nullable
    public ConfigCategory popCategory() {
        if (this.currentCategory == null) return null;
        ConfigCategory pop = this.currentCategory;
        this.currentCategory = this.currentCategory.parent;
        this.popLangKey();
        return pop;
    }

    @Nullable
    public ConfigCategory popCategoryWithoutLangKey() {
        if (this.currentCategory == null) return null;
        ConfigCategory pop = this.currentCategory;
        this.currentCategory = this.currentCategory.parent;
        return pop;
    }

    @Nullable
    public ConfigCategory getCurrentCategory() {
        return this.currentCategory;
    }

    /*
    配置项
     */

    private String getCurrentCategoryForValue() {
        ConfigCategory current = this.getCurrentCategory();
        if (current == null) throw new RuntimeException("No category!");
        return current.getQualifiedName();
    }

    @SuppressWarnings("unchecked")
    private <T> T checkDefault(String name, T currentValue) {
        String key = this.getCurrentCategoryForValue() + "." + name;
        return (T) this.defaultValues.computeIfAbsent(key, k -> currentValue);
    }

    // String

    /**
     * @param langKey 完整的 langKey，不会被当前推送影响。
     */
    public Property getProp(String name, String currentValue, @Nullable String comment, @Nullable String langKey) {
        Property prop = this.config.get(this.getCurrentCategoryForValue(), name, this.checkDefault(name, currentValue), comment);
        if (StringUtils.isNoneBlank(langKey)) prop.setLanguageKey(langKey);
        if (this.writeProp) prop.set(currentValue);
        return prop;
    }

    public Property getProp(String name, String currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment, this.getCurrentSubLangKey(name));
    }

    public Property getProp(String name, String currentValue) {
        return this.getProp(name, currentValue, null);
    }

    public String get(String name, String currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment).getString();
    }

    public String get(String name, String currentValue) {
        return this.get(name, currentValue, null);
    }

    // String[]

    /**
     * @param langKey 完整的 langKey，不会被当前推送影响。
     */
    public Property getProp(String name, String[] currentValues, @Nullable String comment, @Nullable String langKey) {
        Property prop = this.config.get(this.getCurrentCategoryForValue(), name, this.checkDefault(name, currentValues), comment);
        if (StringUtils.isNoneBlank(langKey)) prop.setLanguageKey(langKey);
        if (this.writeProp) prop.set(currentValues);
        return prop;
    }

    public Property getProp(String name, String[] currentValues, @Nullable String comment) {
        return this.getProp(name, currentValues, comment, this.getCurrentSubLangKey(name));
    }

    public Property getProp(String name, String[] currentValues) {
        return this.getProp(name, currentValues, null);
    }

    public String[] get(String name, String[] currentValues, @Nullable String comment) {
        return this.getProp(name, currentValues, comment).getStringList();
    }

    public String[] get(String name, String[] currentValues) {
        return this.get(name, currentValues, null);
    }

    // boolean

    /**
     * @param langKey 完整的 langKey，不会被当前推送影响。
     */
    public Property getProp(String name, boolean currentValue, @Nullable String comment, @Nullable String langKey) {
        Property prop = this.config.get(this.getCurrentCategoryForValue(), name, this.checkDefault(name, currentValue), comment);
        if (StringUtils.isNoneBlank(langKey)) prop.setLanguageKey(langKey);
        if (this.writeProp) prop.set(currentValue);
        return prop;
    }

    public Property getProp(String name, boolean currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment, this.getCurrentSubLangKey(name));
    }

    public Property getProp(String name, boolean currentValue) {
        return this.getProp(name, currentValue, null);
    }

    public boolean get(String name, boolean currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment).getBoolean();
    }

    public boolean get(String name, boolean currentValue) {
        return this.get(name, currentValue, null);
    }

    // int

    /**
     * @param langKey 完整的 langKey，不会被当前推送影响。
     */
    public Property getProp(String name, int currentValue, @Nullable String comment, @Nullable String langKey) {
        Property prop = this.config.get(this.getCurrentCategoryForValue(), name, this.checkDefault(name, currentValue), comment);
        if (StringUtils.isNoneBlank(langKey)) prop.setLanguageKey(langKey);
        if (this.writeProp) prop.set(currentValue);
        return prop;
    }

    public Property getProp(String name, int currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment, this.getCurrentSubLangKey(name));
    }

    public Property getProp(String name, int currentValue) {
        return this.getProp(name, currentValue, null);
    }

    public int get(String name, int currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment).getInt();
    }

    public int get(String name, int currentValue) {
        return this.get(name, currentValue, null);
    }

    // double

    /**
     * @param langKey 完整的 langKey，不会被当前推送影响。
     */
    public Property getProp(String name, double currentValue, @Nullable String comment, @Nullable String langKey) {
        Property prop = this.config.get(this.getCurrentCategoryForValue(), name, this.checkDefault(name, currentValue), comment);
        if (StringUtils.isNoneBlank(langKey)) prop.setLanguageKey(langKey);
        if (this.writeProp) prop.set(currentValue);
        return prop;
    }

    public Property getProp(String name, double currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment, this.getCurrentSubLangKey(name));
    }

    public Property getProp(String name, double currentValue) {
        return this.getProp(name, currentValue, null);
    }

    public double get(String name, double currentValue, @Nullable String comment) {
        return this.getProp(name, currentValue, comment).getDouble();
    }

    public double get(String name, double currentValue) {
        return this.get(name, currentValue, null);
    }
}
