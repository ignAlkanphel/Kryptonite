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

public record OrDamageCondition(List<DamageCondition> conditions) implements DamageCondition {

    public static final MapCodec<OrDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageCondition.LIST_CODEC.fieldOf("conditions").forGetter(OrDamageCondition::conditions)
    ).apply(instance, OrDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        for (DamageCondition condition : this.conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DamageConditionSerializer<OrDamageCondition> getSerializer() {
        return DamageConditionSerializers.OR.get();
    }

    public static class Serializer extends DamageConditionSerializer<OrDamageCondition> {

        @Override
        public MapCodec<OrDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, OrDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("OR")
                    .setDescription("Allows you to group multiple damage conditions into one using the OR logic. At least one of the given damage conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "List of damage conditions");
        }
    }

}