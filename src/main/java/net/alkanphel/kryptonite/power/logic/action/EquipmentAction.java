package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.operation.ItemOperation;
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

public class EquipmentAction extends Action {

    public static final MapCodec<EquipmentAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EquipmentSlotGroup.CODEC.fieldOf("equipment_slot").forGetter(ab -> ab.equipmentSlot),
            ItemOperation.CODEC.fieldOf("item_operations").forGetter(ab -> ab.itemOperation)
    ).apply(instance, EquipmentAction::new));

    public final EquipmentSlotGroup equipmentSlot;
    public final ItemOperation itemOperation;

    public EquipmentAction(EquipmentSlotGroup equipmentSlot, ItemOperation itemOperation) {
        this.equipmentSlot = equipmentSlot;
        this.itemOperation = itemOperation;
    }

    @Override
    protected boolean execute(DataContext context) {
        if (!(context.getEntity() instanceof LivingEntity livingEntity)) return false;

        boolean result = false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (equipmentSlot.test(slot)) {
                result |= itemOperation.run(context.getLevel(), SlotAccess.forEquipmentSlot(livingEntity, slot));
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
                    .setDescription("Performs item operations on an item stack in the specified equipment slot.")
                    .add("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "The equipment slot to perform the item operations on.")
                    .add("item_operations", ItemOperation.documentation(provider), "Item operation(s) performed on the item stack in the specified equipment slot.")
                    .addExampleObject(new EquipmentAction(EquipmentSlotGroup.MAINHAND, ItemOperation.ofConsume(new StaticValue(1))));
        }
    }

}