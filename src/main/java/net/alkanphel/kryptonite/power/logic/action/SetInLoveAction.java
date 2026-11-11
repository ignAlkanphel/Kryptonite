package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class SetInLoveAction extends Action {

    public static final MapCodec<SetInLoveAction> CODEC = MapCodec.unit(new SetInLoveAction());

    public SetInLoveAction() {}

    @Override
    protected boolean execute(DataContext context) {
        if (context.getTarget() instanceof Animal animalTarget && context.getSource() instanceof Player playerSource) {
            animalTarget.setInLove(playerSource);
            return true;
        }

        return false;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.SET_IN_LOVE.get();
    }

    public static class Serializer extends ActionSerializer<SetInLoveAction> {

        @Override
        public MapCodec<SetInLoveAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetInLoveAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set In Love")
                    .setDescription("Sets the target breedable entity in love, where they will seek out other entities to breed with. For this action, the \"source\" is the player & the \"target\" is the animal.")
                    .addExampleObject(new SetInLoveAction());
        }
    }

}