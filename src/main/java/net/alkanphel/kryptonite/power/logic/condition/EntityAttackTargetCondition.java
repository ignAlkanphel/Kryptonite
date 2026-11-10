package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Targeting;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Objects;

public record EntityAttackTargetCondition() implements Condition {

    public static final MapCodec<EntityAttackTargetCondition> CODEC = MapCodec.unit(new EntityAttackTargetCondition());

    @Override
    public boolean test(DataContext context) {
        Entity source = context.getSource();
        Entity target = context.getTarget();

        return (source instanceof Targeting targetingSource && Objects.equals(target, targetingSource.getTarget())) ||
                (source instanceof NeutralMob neutralSource && Objects.equals(target, neutralSource.getTarget()));
    }

    @Override
    public ConditionSerializer<EntityAttackTargetCondition> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_ATTACK_TARGET.get();
    }

    public static class Serializer extends ConditionSerializer<EntityAttackTargetCondition> {

        @Override
        public MapCodec<EntityAttackTargetCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityAttackTargetCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity Attack Target")
                    .setDescription("Checks if the source entity's current attack target is the target entity.")
                    .addExampleObject(new EntityAttackTargetCondition());
        }
    }

}