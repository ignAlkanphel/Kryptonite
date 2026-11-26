package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventFarmlandTrampleAbility extends Ability {

    public static final MapCodec<PreventFarmlandTrampleAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(ab -> ab.blockFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventFarmlandTrampleAbility::new));

    public final BlockFilter blockFilter;

    public PreventFarmlandTrampleAbility(BlockFilter blockFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockFilter = blockFilter;
    }

    public boolean doesPrevent(Level level, BlockPos pos) {
        return blockFilter.test(level, pos);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_FARMLAND_TRAMPLE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventFarmlandTrampleAbility> {

        @Override
        public MapCodec<PreventFarmlandTrampleAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventFarmlandTrampleAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the trampling of farmland blocks.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filters(s) the farmland blocks must pass to prevent trampling.")
                    .addExampleObject(new PreventFarmlandTrampleAbility(BlockFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}