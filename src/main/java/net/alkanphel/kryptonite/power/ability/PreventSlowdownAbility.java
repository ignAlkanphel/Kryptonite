package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PreventSlowdownAbility extends Ability {

    public static final MapCodec<PreventSlowdownAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.listOf().optionalFieldOf("modes", List.of()).forGetter(a -> a.mode),
            ModeBlocks.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(a -> a.modeBlocks),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventSlowdownAbility::new));

    public final List<Mode> mode;
    public final List<ModeBlocks> modeBlocks;

    public PreventSlowdownAbility(List<Mode> mode, List<ModeBlocks> modeBlocks, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.mode = mode;
        this.modeBlocks = modeBlocks;
    }

    public boolean modePrevents(Mode modeEnum) {
        return mode.contains(modeEnum);
    }

    public boolean modeBlocksPrevents(ModeBlocks blockEnum) {
        return mode.contains(Mode.BLOCK) && modeBlocks.contains(blockEnum);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get();
    }

    public static class Serializer extends AbilitySerializer<PreventSlowdownAbility> {

        @Override
        public MapCodec<PreventSlowdownAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventSlowdownAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the player that has this ability from moving slower because of various things.")
                    .addOptional("modes", SettingType.enumList(Mode.values()), "What is prevented from slowing you down.")
                    .addOptional("blocks", SettingType.enumList(ModeBlocks.values()), "The blocks to prevent from slowing you down if \"mode\" contains \"block\".")
                    .addExampleObject(new PreventSlowdownAbility(List.of(Mode.ITEM), List.of(ModeBlocks.COBWEB), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum Mode implements StringRepresentable {
        BLOCK("block"),
        ITEM("item"),
        WATER("water"),
        CROUCHING("crouching"),
        HUNGER("hunger");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public enum ModeBlocks implements StringRepresentable {
        HONEY_BLOCK("honey_block"),
        SOUL_SAND("soul_sand"),
        SLIME_BLOCK("slime_block"),
        COBWEB("cobweb"),
        SWEET_BERRY_BUSH("sweet_berry_bush");

        public static final Codec<ModeBlocks> CODEC = StringRepresentable.fromEnum(ModeBlocks::values);
        private final String name;

        ModeBlocks(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}