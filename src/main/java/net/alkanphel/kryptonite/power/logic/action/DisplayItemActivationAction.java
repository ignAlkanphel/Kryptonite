package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class DisplayItemActivationAction extends Action {

    public static final MapCodec<DisplayItemActivationAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Item.CODEC.fieldOf("item").forGetter(a -> a.item)
    ).apply(instance, DisplayItemActivationAction::new));

    private final Holder<Item> item;

    public DisplayItemActivationAction(Holder<Item> item) {
        this.item = item;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (!(entity instanceof Player player) || !player.level().isClientSide()) return false;

        Kryptonite.PROXY.displayItemActivation(new ItemStack(item.value()));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.DISPLAY_ITEM_ACTIVATION.get();
    }

    public static class Serializer extends ActionSerializer<DisplayItemActivationAction> {

        @Override
        public MapCodec<DisplayItemActivationAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, DisplayItemActivationAction> builder, HolderLookup.Provider provider) {
            builder.setName("Display Item Activation")
                    .setDescription("Triggers an item to animate on the screen like when a Totem of Undying is used.")
                    .add("item", TYPE_ITEM_STACK, "The item stack to use for the animation.")
                    .addExampleObject(new DisplayItemActivationAction(Items.BOOK.builtInRegistryHolder()));
        }
    }

}