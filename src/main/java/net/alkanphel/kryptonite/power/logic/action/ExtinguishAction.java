package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class ExtinguishAction extends Action {

    public static final MapCodec<ExtinguishAction> CODEC = MapCodec.unit(new ExtinguishAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtinguishAction> STREAM_CODEC = StreamCodec.unit(new ExtinguishAction());

    public ExtinguishAction() {}

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        entity.extinguishFire();
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
                    .addExampleObject(new ExtinguishAction());
        }
    }

}