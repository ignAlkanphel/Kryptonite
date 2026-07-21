package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.ConsumeItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;

import java.util.List;

public class EquipmentAction extends Action {

    public static final MapCodec<EquipmentAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EquipmentSlotGroup.CODEC.fieldOf("equipment_slot").forGetter(ab -> ab.equipmentSlot),
            ItemAction.LIST_CODEC.fieldOf("item_actions").forGetter(ab -> ab.itemAction)
    ).apply(instance, EquipmentAction::new));

    public final EquipmentSlotGroup equipmentSlot;
    public final List<ItemAction> itemAction;

    public EquipmentAction(EquipmentSlotGroup equipmentSlot, List<ItemAction> itemAction) {
        this.equipmentSlot = equipmentSlot;
        this.itemAction = itemAction;
    }

    @Override
    public boolean run(DataContext context) {
        if (!(context.getEntity() instanceof LivingEntity livingEntity)) return false;

        boolean result = false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (equipmentSlot.test(slot)) {
                result |= ItemAction.runList(itemAction, context.getLevel(), SlotAccess.forEquipmentSlot(livingEntity, slot));
            }
        }

        return result;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.EQUIPMENT.get();
    }

    public static class Serializer extends ActionSerializer<EquipmentAction> {

        @Override
        public MapCodec<EquipmentAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, EquipmentAction> builder, HolderLookup.Provider provider) {
            builder.setName("Equipment")
                    .setDescription("Runs item actions on an item stack in the specified equipment slot.")
                    .add("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "The equipment slot to run the actions on.")
                    .add("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "The item actions to run on the item stack in the specified equipment slot.")
                    .addExampleObject(new EquipmentAction(EquipmentSlotGroup.MAINHAND, List.of(new ConsumeItemAction(new StaticValue(1)))));
        }
    }

}