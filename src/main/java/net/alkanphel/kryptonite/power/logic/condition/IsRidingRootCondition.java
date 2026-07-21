package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public record IsRidingRootCondition(List<BiCondition> biEntityConditions) implements Condition {

    public static final MapCodec<IsRidingRootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(c -> c.biEntityConditions)
    ).apply(instance, IsRidingRootCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        Entity rootVehicle = entity.getRootVehicle();

        return biEntityConditions.isEmpty() || BiCondition.checkConditions(biEntityConditions, entity, rootVehicle);
    }

    @Override
    public ConditionSerializer<IsRidingRootCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_RIDING_ROOT.get();
    }

    public static class Serializer extends ConditionSerializer<IsRidingRootCondition> {

        @Override
        public MapCodec<IsRidingRootCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsRidingRootCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Riding Root")
                    .setDescription("Checks whether the \"actor\" entity is indirectly riding the \"target\" entity. In the context of this condition, the \"actor\" is the passenger & the entity that fulfilled the condition, while the \"target\" is the entity that is being indirectly ridden (at the start of the riding chain).")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, these conditions must be fulfilled by either or both the \"actor\" & and \"target\" entities.")
                    .addExampleObject(new IsRidingRootCondition(List.of()))
                    .addExampleObject(new IsRidingRootCondition(List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("pig"))))))))));
        }
    }

}