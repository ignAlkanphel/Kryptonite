package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class TameAction extends Action {

    public static final MapCodec<TameAction> CODEC = MapCodec.unit(new TameAction());

    public TameAction() {}

    @Override
    protected boolean execute(DataContext context) {
        if (!(context.getSource() instanceof Player source)) {
            return false;
        }

        Entity target = context.getTarget();
        if (target == null) return false;

        return switch (target) {
            case TamableAnimal tameableTarget when !tameableTarget.isTame() -> {
                tameableTarget.tame(source);
                yield true;
            }
            case AbstractHorse horseLikeTarget when !horseLikeTarget.isTamed() -> {
                horseLikeTarget.tameWithName(source);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.TAME.get();
    }

    public static class Serializer extends ActionSerializer<TameAction> {

        @Override
        public MapCodec<TameAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, TameAction> builder, HolderLookup.Provider provider) {
            builder.setName("Tame")
                    .setDescription("Tames the target tamable entity with the source entity as the owner. For this action, the \"source\" is the player & the \"target\" is the animal being tamed.")
                    .addExampleObject(new TameAction());
        }
    }

}