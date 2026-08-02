package net.zhaiji.grindstoneplus.datagen;

import net.minecraft.data.PackOutput;
import net.zhaiji.grindstoneplus.GrindstonePlus;
import org.jetbrains.annotations.Nullable;

public class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
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
        addConfig("section.server", "Server Settings");
        addConfig("cost_type", "Cost Type", "How experience cost is calculated when extracting enchantments");
        addConfig("cost_type.no_cost", "No Cost");
        addConfig("cost_type.count_cost", "Cost per Enchantment");
        addConfig("cost_type.fixed_cost", "Fixed Cost");
        addConfig("cost_type.anvil_cost", "Vanilla Anvil Cost");
        addConfig("fixed_cost", "Fixed Cost", "Experience cost when cost type is FIXED_COST");
        addConfig("transfer_curses", "Transfer Curses", "Allow transferring cursed enchantments");
        addConfig("server_config_not_loaded", "Settings in here are only available while a world is loaded.");
        addConfig("server_config_while_online", "Settings in here are determined by the server and cannot be changed while online.");
        addConfig("server_config_while_lan", "Settings in here cannot be edited while your game is open to LAN. Please return to the main menu and load the world again.");
    }

    private void Chinese() {
        addConfig("title", "Grindstone Plus 配置");
        addConfig("section.server", "服务器设置");
        addConfig("cost_type", "消耗类型", "提取附魔时经验消耗的计算方式");
        addConfig("cost_type.no_cost", "无消耗");
        addConfig("cost_type.count_cost", "按附魔数量消耗");
        addConfig("cost_type.fixed_cost", "固定消耗");
        addConfig("cost_type.anvil_cost", "原版铁砧消耗");
        addConfig("fixed_cost", "固定消耗", "当消耗类型为固定消耗时收取的经验值");
        addConfig("transfer_curses", "转移诅咒附魔", "允许转移诅咒附魔");
        addConfig("server_config_not_loaded", "此处的配置仅在加载世界后可用。");
        addConfig("server_config_while_online", "此处的配置由服务器决定，联机时无法更改。");
        addConfig("server_config_while_lan", "游戏开放局域网时无法编辑此处的配置。请返回主菜单并重新加载世界。");
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
