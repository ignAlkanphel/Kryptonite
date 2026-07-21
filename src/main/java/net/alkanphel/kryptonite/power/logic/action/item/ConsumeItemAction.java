package net.alkanphel.kryptonite.power.logic.action.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class ConsumeItemAction extends ItemAction {

    public static final MapCodec<ConsumeItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("amount", new StaticValue(1)).forGetter(a -> a.amount)
    ).apply(instance, ConsumeItemAction::new));

    private final Value amount;

    public ConsumeItemAction(Value amount) {
        this.amount = amount;
    }

    @Override
    protected boolean run(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null || slot.get().isEmpty()) return false;

        slot.get().shrink(Math.max(0, amount.getAsInt(null)));
        return true;
    }

    @Override
    public ItemActionSerializer<?> getSerializer() {
        return ItemActionSerializers.CONSUME.get();
    }

    public static class Serializer extends ItemActionSerializer<ConsumeItemAction> {

        @Override
        public MapCodec<ConsumeItemAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemAction, ConsumeItemAction> builder, HolderLookup.Provider provider) {
            builder.setName("Consume")
                    .setDescription("Removes the specified amount of items from the item stack.")
                    .addOptional("amount", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of items to remove.", new StaticValue(1))
                    .addExampleObject(new ConsumeItemAction(new StaticValue(7)));

        }
    }

}