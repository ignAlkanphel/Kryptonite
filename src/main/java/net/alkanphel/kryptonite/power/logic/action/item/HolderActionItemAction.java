package net.alkanphel.kryptonite.power.logic.action.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;

import java.util.List;

public class HolderActionItemAction extends ItemAction {

    public static final MapCodec<HolderActionItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.actions)
    ).apply(instance, HolderActionItemAction::new));

    private final List<Action> actions;

    public HolderActionItemAction(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    protected boolean run(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null) return false;

        ItemStack stack = slot.get();
        if (stack.isEmpty()) return false;

        Entity holder = ((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity();
        if (holder == null) return false;

        Action.runList(actions, DataContext.forEntity(holder));
        return true;
    }

    @Override
    public ItemActionSerializer<?> getSerializer() {
        return ItemActionSerializers.HOLDER_ACTION.get();
    }

    public static class Serializer extends ItemActionSerializer<HolderActionItemAction> {

        @Override
        public MapCodec<HolderActionItemAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemAction, HolderActionItemAction> builder, HolderLookup.Provider provider) {
            builder.setName("Holder Action")
                    .setDescription("Runs actions on the entity holding this item stack. Does nothing if the item has no holder.")
                    .add("action", TYPE_ACTION_LIST, "The actions to run on the holding entity.")
                    .addExampleObject(new HolderActionItemAction(List.of()));
        }
    }

}