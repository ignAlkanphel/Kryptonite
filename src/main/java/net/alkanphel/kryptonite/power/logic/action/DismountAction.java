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

public class DismountAction extends Action {

    public static final MapCodec<DismountAction> CODEC = MapCodec.unit(new DismountAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, DismountAction> STREAM_CODEC = StreamCodec.unit(new DismountAction());

    public DismountAction() {}

    @Override
    protected boolean execute(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        entity.stopRiding();
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.DISMOUNT.get();
    }

    public static class Serializer extends ActionSerializer<DismountAction> {

        @Override
        public MapCodec<DismountAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, DismountAction> builder, HolderLookup.Provider provider) {
            builder.setName("Dismount")
                    .setDescription("Dismounts the entity from their vehicle.")
                    .addExampleObject(new DismountAction());
        }
    }

}