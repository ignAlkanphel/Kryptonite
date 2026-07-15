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

public record AndDamageCondition(List<DamageCondition> conditions) implements DamageCondition {

    public static final MapCodec<AndDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageCondition.LIST_CODEC.fieldOf("conditions").forGetter(AndDamageCondition::conditions)
    ).apply(instance, AndDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        for (DamageCondition condition : this.conditions) {
            if (!condition.test(context)) {
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
                    .setDescription("Allows you to group multiple damage conditions into one using the AND logic. All of the given damage conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "List of damage conditions");
        }
    }

}