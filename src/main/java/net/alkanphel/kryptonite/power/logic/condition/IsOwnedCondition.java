package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class IsOwnedCondition implements Condition {

    public static final IsOwnedCondition INSTANCE = new IsOwnedCondition();

    public static final MapCodec<IsOwnedCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsOwnedCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        return (entity instanceof OwnableEntity ownable && ownable.getOwner() != null)
                || (entity instanceof Projectile projectile && projectile.getOwner() != null)
                || (entity instanceof AreaEffectCloud cloud && cloud.getOwner() != null);
    }

    @Override
    public ConditionSerializer<IsOwnedCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_OWNED.get();
    }

    public static class Serializer extends ConditionSerializer<IsOwnedCondition> {

        @Override
        public MapCodec<IsOwnedCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsOwnedCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Owned")
                    .setDescription("Checks if the entity is owned.")
                    .addExampleObject(INSTANCE);
        }
    }

}