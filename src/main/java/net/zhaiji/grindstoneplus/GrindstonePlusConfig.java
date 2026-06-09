package net.zhaiji.grindstoneplus;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GrindstonePlusConfig {
    public static CostType costType;
    public static int fixedCost;
    public static boolean transferCurses;

    public enum CostType {
        NO_COST, COUNT_COST, FIXED_COST, ANVIL_COST
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder()
            .push("Config");

    private static final ModConfigSpec.EnumValue<CostType> COST_TYPE_ENUM_VALUE = BUILDER
            .comment(
                    """
                            NO_COST - No experience cost,
                            COUNT_COST - Experience cost based on number of enchantments,
                            FIXED_COST - Fixed experience cost,
                            ANVIL_COST - Experience cost like vanilla anvil
                            """
            )
            .defineEnum(
                    "cost_type",
                    CostType.ANVIL_COST
            );

    private static final ModConfigSpec.IntValue FIXED_COST_VALUE = BUILDER
            .comment("cost type is FIXED_COST, apply this fixed cost")
            .defineInRange(
                    "fixed_cost",
                    2,
                    1,
                    Integer.MAX_VALUE
            );

    private static final ModConfigSpec.BooleanValue TRANSFER_CURSES = BUILDER
            .comment("allow transferring cursed enchantments")
            .define(
                    "transfer_curses",
                    false
            );

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void handlerModConfigEvent(final ModConfigEvent event) {
        costType = COST_TYPE_ENUM_VALUE.get();
        fixedCost = FIXED_COST_VALUE.get();
        transferCurses = TRANSFER_CURSES.get();
    }
}
