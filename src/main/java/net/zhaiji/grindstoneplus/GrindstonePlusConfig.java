package net.zhaiji.grindstoneplus;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.Locale;

public class GrindstonePlusConfig {
    public enum CostType implements TranslatableEnum {
        NO_COST, COUNT_COST, FIXED_COST, ANVIL_COST;

        @Override
        public Component getTranslatedName() {
            return Component.translatable(
                    GrindstonePlus.MOD_ID + ".configuration.cost_type." + name().toLowerCase(Locale.ROOT)
            );
        }
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<CostType> COST_TYPE_ENUM_VALUE = BUILDER
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

    public static final ModConfigSpec.IntValue FIXED_COST_VALUE = BUILDER
            .comment("cost type is FIXED_COST, apply this fixed cost")
            .defineInRange(
                    "fixed_cost",
                    2,
                    1,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.BooleanValue TRANSFER_CURSES = BUILDER
            .comment("allow transferring cursed enchantments")
            .define(
                    "transfer_curses",
                    false
            );

    public static final ModConfigSpec SPEC = BUILDER.build();
}
