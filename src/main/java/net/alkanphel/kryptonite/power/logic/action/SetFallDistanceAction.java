package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class SetFallDistanceAction extends Action {

    public static final MapCodec<SetFallDistanceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("fall_distance").forGetter(a -> a.fallDistance)
    ).apply(instance, SetFallDistanceAction::new));

    public final Value fallDistance;

    public SetFallDistanceAction(Value fallDistance) {
        this.fallDistance = fallDistance;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        entity.fallDistance = fallDistance.getAsDouble(context);
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.SET_FALL_DISTANCE.get();
    }

    public static class Serializer extends ActionSerializer<SetFallDistanceAction> {

        @Override
        public MapCodec<SetFallDistanceAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetFallDistanceAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Fall Distance")
                    .setDescription("Sets the fall distance of the entity.")
                    .addOptional("fall_distance", TYPE_VALUE, "The value the fall distance is set to.")
                    .addExampleObject(new SetFallDistanceAction(new StaticValue(0)));
        }
    }

}