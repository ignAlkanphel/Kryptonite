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

public class ActionItemAction extends ItemAction {

    public static final MapCodec<ActionItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.action)
    ).apply(instance, ActionItemAction::new));

    private final List<Action> action;

    public ActionItemAction(List<Action> action) {
        this.action = action;
    }

    @Override
    protected boolean execute(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null) return false;

        ItemStack stack = slot.get();
        if (stack.isEmpty()) return false;

        Entity holder = ((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity();
        if (holder == null) return false;

        Action.runList(action, DataContext.forEntity(holder));
        return true;
    }

    @Override
    public ItemActionSerializer<?> getSerializer() {
        return ItemActionSerializers.ACTION.get();
    }

    public static class Serializer extends ItemActionSerializer<ActionItemAction> {

        @Override
        public MapCodec<ActionItemAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemAction, ActionItemAction> builder, HolderLookup.Provider provider) {
            builder.setName("Action")
                    .setDescription("Performs actions on the entity holding this item stack.")
                    .add("actions", TYPE_ACTION_LIST, "Action(s) performed on the holding entity. Does nothing if the item has no holder.")
                    .addExampleObject(new ActionItemAction(List.of()));
        }
    }

}