package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.mutation.ItemMutation;
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
            ItemMutation.CODEC.fieldOf("item_mutations").forGetter(ab -> ab.itemMutation)
    ).apply(instance, EquipmentAction::new));

    public final EquipmentSlotGroup equipmentSlot;
    public final ItemMutation itemMutation;

    public EquipmentAction(EquipmentSlotGroup equipmentSlot, ItemMutation itemMutation) {
        this.equipmentSlot = equipmentSlot;
        this.itemMutation = itemMutation;
    }

    @Override
    protected boolean execute(DataContext context) {
        if (!(context.getEntity() instanceof LivingEntity livingEntity)) return false;

        boolean result = false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (equipmentSlot.test(slot)) {
                result |= itemMutation.run(context.getLevel(), SlotAccess.forEquipmentSlot(livingEntity, slot));
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
                    .setDescription("Performs item mutations on an item stack in the specified equipment slot.")
                    .add("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "The equipment slot to perform the item mutations on.")
                    .add("item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the item stack in the specified equipment slot.")
                    .addExampleObject(new EquipmentAction(EquipmentSlotGroup.MAINHAND, ItemMutation.ofConsume(new StaticValue(1))));
        }
    }

}