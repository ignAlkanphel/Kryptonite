package net.alkanphel.kryptonite.power.logic.action.bi;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public class TameBiAction extends BiAction {

    public static final MapCodec<TameBiAction> CODEC = MapCodec.unit(new TameBiAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, TameBiAction> STREAM_CODEC = StreamCodec.unit(new TameBiAction());

    public TameBiAction() {}

    @Override
    public boolean run(BiActionContext context) {
        if (!(context.actor() instanceof Player playerActor)) {
            return false;
        }

        boolean success = false;

        switch (context.target()) {
            case TamableAnimal tameableTarget when !tameableTarget.isTame() -> {
                tameableTarget.tame(playerActor);
                success = true;
            }
            case AbstractHorse horseLikeTarget when !horseLikeTarget.isTamed() -> {
                horseLikeTarget.tameWithName(playerActor);
                success = true;
            }
            default -> {}
        }

        return success;
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.TAME.get();
    }

    public static class Serializer extends BiActionSerializer<TameBiAction> {

        @Override
        public MapCodec<TameBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, TameBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Tame")
                    .setDescription("Tames the target entity with the actor entity as the owner. Only works on tameable entities such as wolves or cats.")
                    .addExampleObject(new TameBiAction());
        }
    }

}