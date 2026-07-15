package net.alkanphel.kryptonite.power.logic.condition.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record AttackerDamageCondition(Optional<Condition> condition) implements DamageCondition {

    public static final MapCodec<AttackerDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("conditions").forGetter(AttackerDamageCondition::condition)
    ).apply(instance, AttackerDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        Entity attacker = context.source().getEntity();
        return attacker != null && condition
                .map(condition -> condition.test(DataContext.forEntity(attacker)))
                .orElse(true);
    }

    @Override
    public DamageConditionSerializer<AttackerDamageCondition> getSerializer() {
        return DamageConditionSerializers.ATTACKER.get();
    }

    public static class Serializer extends DamageConditionSerializer<AttackerDamageCondition> {

        @Override
        public MapCodec<AttackerDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, AttackerDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attacker")
                    .setDescription("Checks whether the damage source is from an entity.")
                    .addOptional("conditions", TYPE_CONDITION_LIST, "If specified, the attacker entity must fulfill these conditions.")
                    .addExampleObject(new AttackerDamageCondition(Optional.empty()));
        }
    }

}