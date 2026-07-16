package net.alkanphel.kryptonite.power.logic.action.bi;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public class SetInLoveBiAction extends BiAction {

    public static final MapCodec<SetInLoveBiAction> CODEC = MapCodec.unit(new SetInLoveBiAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, SetInLoveBiAction> STREAM_CODEC = StreamCodec.unit(new SetInLoveBiAction());

    public SetInLoveBiAction() {}

    @Override
    public boolean run(BiActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();

        if (target instanceof Animal animalTarget && actor instanceof Player playerActor) {
            animalTarget.setInLove(playerActor);
            return true;
        }

        return false;
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.SET_IN_LOVE.get();
    }

    public static class Serializer extends BiActionSerializer<SetInLoveBiAction> {

        @Override
        public MapCodec<SetInLoveBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, SetInLoveBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set In Love")
                    .setDescription("Sets the target entity into 'love mode', where they will seek out other animals to breed with. Note that this only works on animals that can breed, such as Cows or Pigs.")
                    .addExampleObject(new SetInLoveBiAction());
        }
    }

}