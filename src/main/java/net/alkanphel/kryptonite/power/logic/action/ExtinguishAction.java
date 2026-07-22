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

public class ExtinguishAction extends Action {

    public static final MapCodec<ExtinguishAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("no_effects", new StaticValue(false)).forGetter(a -> a.noEffects)
    ).apply(instance, ExtinguishAction::new));

    public final Value noEffects;

    public ExtinguishAction(Value noEffects) {
        this.noEffects = noEffects;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        if (noEffects.getAsBoolean(context)) {
            entity.clearFire();
        } else {
            entity.extinguishFire();
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.EXTINGUISH.get();
    }

    public static class Serializer extends ActionSerializer<ExtinguishAction> {

        @Override
        public MapCodec<ExtinguishAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, ExtinguishAction> builder, HolderLookup.Provider provider) {
            builder.setName("Extinguish")
                    .setDescription("Clears fire from the entity.")
                    .addOptional("no_effects", TYPE_VALUE, "If true, clears the fire without additional effects like sound.", false)
                    .addExampleObject(new ExtinguishAction(new StaticValue(false)));
        }
    }

}