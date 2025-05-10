package net.zhaiji.grindstoneplus;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = GrindstonePlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GrindstonePlusConfig {
    public static CostType costType;
    public static int fixedCost;
    public static boolean transferCurses;

    public enum CostType {
        NO_COST, COUNT_COST, FIXED_COST, ANVIL_COST
    }

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder()
            .push("Config");

    private static final ForgeConfigSpec.EnumValue<CostType> COST_TYPE_ENUM_VALUE = BUILDER
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
                    CostType.COUNT_COST
            );

    private static final ForgeConfigSpec.IntValue FIXED_COST_VALUE = BUILDER
            .comment("cost type is FIXED_COST, apply this fixed cost")
            .defineInRange(
                    "fixed_cost",
                    2,
                    1,
                    Integer.MAX_VALUE
            );

    private static final ForgeConfigSpec.BooleanValue TRANSFER_CURSES = BUILDER
            .comment("allow transferring cursed enchantments")
            .define(
                    "transfer_curses",
                    false
            );

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        costType = COST_TYPE_ENUM_VALUE.get();
        fixedCost = FIXED_COST_VALUE.get();
        transferCurses = TRANSFER_CURSES.get();
    }
}
