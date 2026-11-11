package net.alkanphel.kryptonite.power.logic.action.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializers;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

public class DamageItemAction extends ItemAction {

    public static final MapCodec<DamageItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.NUMBER_CODEC.optionalFieldOf("amount", new StaticValue(1)).forGetter(a -> a.amount),
            BooleanValueHolder.CODEC.optionalFieldOf("ignore_unbreaking", BooleanValueHolder.FALSE).forGetter(a -> a.ignoreUnbreaking)
    ).apply(instance, DamageItemAction::new));

    private final Value amount;
    private final BooleanValueHolder ignoreUnbreaking;

    public DamageItemAction(Value amount, BooleanValueHolder ignoreUnbreaking) {
        this.amount = amount;
        this.ignoreUnbreaking = ignoreUnbreaking;
    }

    @Override
    protected boolean execute(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null) return false;

        ItemStack stack = slot.get();
        if (stack.isEmpty()) return false;

        int amount = Math.max(0, this.amount.getAsInt(null));
        boolean ignoreUnbreaking = this.ignoreUnbreaking.get(null);

        if (ignoreUnbreaking) {
            if (amount >= stack.getMaxDamage()) {
                stack.shrink(1);
            }

            else {
                stack.setDamageValue(stack.getDamageValue() + amount);
            }
        }

        else {
            stack.hurtAndBreak(amount, context.level(), null, item -> {});
        }

        return true;
    }

    @Override
    public ItemActionSerializer<?> getSerializer() {
        return ItemActionSerializers.DAMAGE.get();
    }

    public static class Serializer extends ItemActionSerializer<DamageItemAction> {

        @Override
        public MapCodec<DamageItemAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemAction, DamageItemAction> builder, HolderLookup.Provider provider) {
            builder.setName("Damage")
                    .setDescription("Damages the item stack.")
                    .addOptional("amount", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of damage to apply to the item stack.", 1)
                    .addOptional("ignore_unbreaking", TYPE_BOOLEAN_VALUE, "If true, it will ignore the \"minecraft:unbreaking\" enchantment.", false)
                    .addExampleObject(new DamageItemAction(new StaticValue(7), BooleanValueHolder.TRUE));
        }
    }

}