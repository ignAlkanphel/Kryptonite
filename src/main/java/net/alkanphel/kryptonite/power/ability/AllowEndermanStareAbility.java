package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class AllowEndermanStareAbility extends Ability {

    public static final MapCodec<AllowEndermanStareAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(ab -> ab.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, AllowEndermanStareAbility::new));

    public final List<BiCondition> biEntityConditions;

    public AllowEndermanStareAbility(List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityConditions = biEntityConditions;
    }

    public boolean appliesTo(Entity actor, Entity target) {
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, actor, target)) {
            return false;
        }

        return true;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ALLOW_ENDERMAN_STARE.get();
    }

    public static class Serializer extends AbilitySerializer<AllowEndermanStareAbility> {

        @Override
        public MapCodec<AllowEndermanStareAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, AllowEndermanStareAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows looking into the eyes of an enderman without angering it. In the context of this ability, the \"actor\" is the ability holder & \"target\" the enderman.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actor is only allowed to stare if the target fulfills these conditions.")
                    .addExampleObject(new AllowEndermanStareAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}