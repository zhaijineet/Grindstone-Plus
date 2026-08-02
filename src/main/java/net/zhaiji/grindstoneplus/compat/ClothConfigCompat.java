package net.zhaiji.grindstoneplus.compat;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.zhaiji.grindstoneplus.GrindstonePlus;
import net.zhaiji.grindstoneplus.GrindstonePlusConfig;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ClothConfigCompat {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> createConfigScreen(parent)));
    }

    private static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(GrindstonePlus.MOD_ID + ".configuration.title"))
                .setSavingRunnable(ClothConfigCompat::saveAllLoadedConfigs);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        for (ModConfig modConfig : getModConfigs()) {
            ConfigCategory category = builder.getOrCreateCategory(getCategoryTitle(modConfig));
            Component lockReason = getLockReason(modConfig, minecraft);
            if (lockReason != null) {
                // 锁定分支绝不读/写 spec 值（未加载时访问会抛异常）
                category.addEntry(entryBuilder.startTextDescription(lockReason).build());
                continue;
            }
            // 配置项均定义在 SERVER spec，仅该类型生成条目
            if (modConfig.getType() == ModConfig.Type.SERVER) {
                buildEntries(category, entryBuilder);
            }
        }
        return builder.build();
    }

    private static List<ModConfig> getModConfigs() {
        return ConfigTracker.INSTANCE.configSets().values().stream()
                .flatMap(Set::stream)
                .filter(config -> config.getModId().equals(GrindstonePlus.MOD_ID))
                .sorted(Comparator.comparingInt(config -> config.getType().ordinal()))
                .toList();
    }

    private static Component getCategoryTitle(ModConfig modConfig) {
        String typeName = modConfig.getType().name().toLowerCase(Locale.ROOT);
        String translationKey = GrindstonePlus.MOD_ID + ".configuration.section." + typeName;
        return I18n.exists(translationKey) ? Component.translatable(translationKey) : Component.literal(typeName);
    }

    private static Component getLockReason(ModConfig modConfig, Minecraft minecraft) {
        if (!((ForgeConfigSpec) modConfig.getSpec()).isLoaded()) {
            return Component.translatable(GrindstonePlus.MOD_ID + ".configuration.server_config_not_loaded");
        }
        if (modConfig.getType() == ModConfig.Type.SERVER) {
            if (minecraft.getCurrentServer() != null && !minecraft.isSingleplayer()) {
                return Component.translatable(GrindstonePlus.MOD_ID + ".configuration.server_config_while_online");
            }
            if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer().isPublished()) {
                return Component.translatable(GrindstonePlus.MOD_ID + ".configuration.server_config_while_lan");
            }
        }
        return null;
    }

    private static void saveAllLoadedConfigs() {
        for (ModConfig modConfig : getModConfigs()) {
            ForgeConfigSpec spec = (ForgeConfigSpec) modConfig.getSpec();
            if (spec.isLoaded()) {
                spec.save();
            }
        }
    }

    private static void buildEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        category.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable(GrindstonePlus.MOD_ID + ".configuration.cost_type"),
                        GrindstonePlusConfig.CostType.class,
                        GrindstonePlusConfig.COST_TYPE_ENUM_VALUE.get())
                .setDefaultValue(GrindstonePlusConfig.COST_TYPE_ENUM_VALUE.getDefault())
                .setEnumNameProvider(costType -> Component.translatable(
                        GrindstonePlus.MOD_ID + ".configuration.cost_type." + costType.name().toLowerCase(Locale.ROOT)))
                .setTooltip(Component.translatable(GrindstonePlus.MOD_ID + ".configuration.cost_type.tooltip"))
                .setSaveConsumer(GrindstonePlusConfig.COST_TYPE_ENUM_VALUE::set)
                .build());

        category.addEntry(entryBuilder.startIntField(
                        Component.translatable(GrindstonePlus.MOD_ID + ".configuration.fixed_cost"),
                        GrindstonePlusConfig.FIXED_COST_VALUE.get())
                .setDefaultValue(GrindstonePlusConfig.FIXED_COST_VALUE.getDefault())
                .setMin(1)
                .setMax(Integer.MAX_VALUE)
                .setTooltip(Component.translatable(GrindstonePlus.MOD_ID + ".configuration.fixed_cost.tooltip"))
                .setSaveConsumer(GrindstonePlusConfig.FIXED_COST_VALUE::set)
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable(GrindstonePlus.MOD_ID + ".configuration.transfer_curses"),
                        GrindstonePlusConfig.TRANSFER_CURSES.get())
                .setDefaultValue(GrindstonePlusConfig.TRANSFER_CURSES.getDefault())
                .setTooltip(Component.translatable(GrindstonePlus.MOD_ID + ".configuration.transfer_curses.tooltip"))
                .setSaveConsumer(GrindstonePlusConfig.TRANSFER_CURSES::set)
                .build());
    }
}
