package net.alkanphel.kryptonite.power.logic.action.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializers;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class DamageItemAction extends ItemAction {

    public static final MapCodec<DamageItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("amount", new StaticValue(1)).forGetter(a -> a.amount),
            Value.CODEC.optionalFieldOf("ignore_unbreaking", new StaticValue(false)).forGetter(a -> a.ignoreUnbreaking)
    ).apply(instance, DamageItemAction::new));

    private final Value amount;
    private final Value ignoreUnbreaking;

    public DamageItemAction(Value amount, Value ignoreUnbreaking) {
        this.amount = amount;
        this.ignoreUnbreaking = ignoreUnbreaking;
    }

    @Override
    protected boolean run(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null) return false;

        ItemStack stack = slot.get();
        if (stack.isEmpty()) return false;

        int amount = Math.max(0, this.amount.getAsInt(null));
        boolean ignoreUnbreaking = this.ignoreUnbreaking.getAsBoolean(null);

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
                    .addOptional("amount", TYPE_VALUE, "Amount of damage to apply to the item stack.", 1)
                    .addOptional("ignore_unbreaking", TYPE_BOOLEAN, "If true, it will ignore the Unbreaking enchantment.", false)
                    .addExampleObject(new DamageItemAction(new StaticValue(7), new StaticValue(true)));
        }
    }

}