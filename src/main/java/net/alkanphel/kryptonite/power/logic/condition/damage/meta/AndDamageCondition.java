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

public record AndDamageCondition(List<DamageCondition> damageConditions) implements DamageCondition {

    public static final MapCodec<AndDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions").forGetter(c -> Optional.of(c.damageConditions())),
            DamageCondition.LIST_CODEC.optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (damageConditions, conditions) ->
            new AndDamageCondition(damageConditions.orElseGet(() -> conditions.orElse(List.of())))
    ));

    @Override
    public boolean test(DamageConditionContext context) {
        for (DamageCondition damageCondition : this.damageConditions) {
            if (!damageCondition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DamageConditionSerializer<AndDamageCondition> getSerializer() {
        return DamageConditionSerializers.AND.get();
    }

    public static class Serializer extends DamageConditionSerializer<AndDamageCondition> {

        @Override
        public MapCodec<AndDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, AndDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("AND")
                    .setDescription("Allows you to group multiple damage conditions into one using the AND logic. All of the given damage conditions must be true for this one to be true aswell. The namespace alias \"palladium:and\" is supported.")
                    .add("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "List of damage conditions. This field supports aliases: \"damage_conditions\" & \"conditions\"");
        }
    }

}