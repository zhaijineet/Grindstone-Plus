package net.zhaiji.grindstoneplus.datagen;

import net.minecraft.data.PackOutput;
import net.zhaiji.grindstoneplus.GrindstonePlus;
import org.jetbrains.annotations.Nullable;

public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {
    public static final String EN_US = "en_us";
    public static final String ZH_CN = "zh_cn";

    private final String locale;

    public LanguageProvider(PackOutput output, String locale) {
        super(output, GrindstonePlus.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case EN_US -> English();
            case ZH_CN -> Chinese();
        }
    }

    private void English() {
        addConfig("title", "Grindstone Plus Configuration");
        addConfig("cost_type", "Cost Type", "How experience cost is calculated when extracting enchantments");
        addConfig("cost_type.no_cost", "No Cost");
        addConfig("cost_type.count_cost", "Cost per Enchantment");
        addConfig("cost_type.fixed_cost", "Fixed Cost");
        addConfig("cost_type.anvil_cost", "Vanilla Anvil Cost");
        addConfig("fixed_cost", "Fixed Cost", "Experience cost when cost type is FIXED_COST");
        addConfig("transfer_curses", "Transfer Curses", "Allow transferring cursed enchantments");
    }

    private void Chinese() {
        addConfig("title", "Grindstone Plus 配置");
        addConfig("cost_type", "消耗类型", "提取附魔时经验消耗的计算方式");
        addConfig("cost_type.no_cost", "无消耗");
        addConfig("cost_type.count_cost", "按附魔数量消耗");
        addConfig("cost_type.fixed_cost", "固定消耗");
        addConfig("cost_type.anvil_cost", "原版铁砧消耗");
        addConfig("fixed_cost", "固定消耗", "当消耗类型为固定消耗时收取的经验值");
        addConfig("transfer_curses", "转移诅咒附魔", "允许转移诅咒附魔");
    }

    private void addConfig(String key, String name, @Nullable String tooltip) {
        add(GrindstonePlus.MOD_ID + ".configuration." + key, name);
        if (tooltip != null) {
            add(GrindstonePlus.MOD_ID + ".configuration." + key + ".tooltip", tooltip);
        }
    }

    private void addConfig(String key, String name) {
        addConfig(key, name, null);
    }
}
