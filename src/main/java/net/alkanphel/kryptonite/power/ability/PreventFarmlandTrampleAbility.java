package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventFarmlandTrampleAbility extends Ability {

    public static final MapCodec<PreventFarmlandTrampleAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(ab -> ab.blockConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventFarmlandTrampleAbility::new));

    public final List<BlockCondition> blockConditions;

    public PreventFarmlandTrampleAbility(List<BlockCondition> blockConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockConditions = blockConditions;
    }

    public boolean doesPrevent(Level level, BlockPos pos) {
        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, level, pos);
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
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only prevents trampling if the block fulfills these conditions.")
                    .addExampleObject(new PreventFarmlandTrampleAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}