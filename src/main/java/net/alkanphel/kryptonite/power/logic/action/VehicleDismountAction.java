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

public class VehicleDismountAction extends Action {

    public static final MapCodec<VehicleDismountAction> CODEC = MapCodec.unit(new VehicleDismountAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleDismountAction> STREAM_CODEC = StreamCodec.unit(new VehicleDismountAction());

    public VehicleDismountAction() {}

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        entity.stopRiding();
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.VEHICLE_DISMOUNT.get();
    }

    public static class Serializer extends ActionSerializer<VehicleDismountAction> {

        @Override
        public MapCodec<VehicleDismountAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, VehicleDismountAction> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Dismount")
                    .setDescription("Dismounts the entity from their vehicle.")
                    .addExampleObject(new VehicleDismountAction());
        }
    }

}