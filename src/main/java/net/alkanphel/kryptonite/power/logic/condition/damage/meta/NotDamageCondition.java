package net.alkanphel.kryptonite.power.logic.condition.damage.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;
import java.util.Optional;

public record NotDamageCondition(List<DamageCondition> damageConditions) implements DamageCondition {

    public static final MapCodec<NotDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions").forGetter(c -> Optional.of(c.damageConditions())),
            DamageCondition.LIST_CODEC.optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (damageConditions, conditions) ->
            new NotDamageCondition(damageConditions.orElseGet(() -> conditions.orElse(List.of())))
    ));

    @Override
    public boolean test(DamageConditionContext context) {
        for (DamageCondition damageCondition : this.damageConditions) {
            if (damageCondition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DamageConditionSerializer<NotDamageCondition> getSerializer() {
        return DamageConditionSerializers.NOT.get();
    }

    public static class Serializer extends DamageConditionSerializer<NotDamageCondition> {

        @Override
        public MapCodec<NotDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, NotDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NOT")
                    .setDescription("Allows you to group multiple damage conditions into one using the NOT logic. None of the given damage conditions must be true for this one to be true aswell. The namespace alias \"palladium:not\" is supported.")
                    .add("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "List of damage conditions. This field supports aliases: \"damage_conditions\" & \"conditions\"");
        }
    }

}