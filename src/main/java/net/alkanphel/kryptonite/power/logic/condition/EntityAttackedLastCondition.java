package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Attackable;

import java.util.Objects;

public record EntityAttackedLastCondition() implements Condition {

    public static final MapCodec<EntityAttackedLastCondition> CODEC = MapCodec.unit(new EntityAttackedLastCondition());

    @Override
    public boolean test(DataContext context) {
        var source = context.getSource();
        var target = context.getTarget();

        return source != null
                && target instanceof Attackable attackable
                && Objects.equals(source, attackable.getLastAttacker());
    }

    @Override
    public ConditionSerializer<EntityAttackedLastCondition> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_ATTACKED_LAST.get();
    }

    public static class Serializer extends ConditionSerializer<EntityAttackedLastCondition> {

        @Override
        public MapCodec<EntityAttackedLastCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityAttackedLastCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity Attacked Last")
                    .setDescription("Checks if the source entity is the target entity's last recorded attacker.")
                    .addExampleObject(new EntityAttackedLastCondition());
        }
    }

}