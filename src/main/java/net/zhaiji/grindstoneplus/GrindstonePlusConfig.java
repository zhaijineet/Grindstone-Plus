package net.zhaiji.grindstoneplus;

import net.minecraftforge.common.ForgeConfigSpec;

public class GrindstonePlusConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<CostType> COST_TYPE_ENUM_VALUE = BUILDER
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

    public static final ForgeConfigSpec.IntValue FIXED_COST_VALUE = BUILDER
        .comment("cost type is FIXED_COST, apply this fixed cost")
        .defineInRange(
            "fixed_cost",
            2,
            1,
            Integer.MAX_VALUE
        );

    public static final ForgeConfigSpec.BooleanValue TRANSFER_CURSES = BUILDER
        .comment("allow transferring cursed enchantments")
        .define(
            "transfer_curses",
            false
        );

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public enum CostType {
        NO_COST,
        COUNT_COST,
        FIXED_COST,
        ANVIL_COST
    }
}
