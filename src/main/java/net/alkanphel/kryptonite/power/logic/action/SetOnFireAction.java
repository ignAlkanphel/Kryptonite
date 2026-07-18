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

public class SetOnFireAction extends Action {

    public static final MapCodec<SetOnFireAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("duration").forGetter(a -> a.duration)
    ).apply(instance, SetOnFireAction::new));

    public final Value duration;

    public SetOnFireAction(Value duration) {
        this.duration = duration;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        entity.setRemainingFireTicks(Math.max(0, duration.getAsInt(context)));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.SET_ON_FIRE.get();
    }

    public static class Serializer extends ActionSerializer<SetOnFireAction> {

        @Override
        public MapCodec<SetOnFireAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetOnFireAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set On Fire")
                    .setDescription("Sets the entity on fire for the specified amount of time in ticks.")
                    .addOptional("duration", TYPE_VALUE, "How long the entity will burn.")
                    .addExampleObject(new SetOnFireAction(new StaticValue(5)));
        }
    }

}