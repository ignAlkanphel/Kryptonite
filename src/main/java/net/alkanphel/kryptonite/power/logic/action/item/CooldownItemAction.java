package net.alkanphel.kryptonite.power.logic.action.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemActionContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class CooldownItemAction extends ItemAction {

    public static final MapCodec<CooldownItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("ticks", new StaticValue(20)).forGetter(a -> a.ticks)
    ).apply(instance, CooldownItemAction::new));

    private final Value ticks;

    public CooldownItemAction(Value ticks) {
        this.ticks = ticks;
    }

    @Override
    protected boolean run(ItemActionContext context) {
        var slot = context.slotAccess();
        if (slot == null) return false;

        ItemStack stack = slot.get();
        if (context.level().isClientSide() || stack.isEmpty()) return false;

        if (((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity() instanceof Player player) {
            player.getCooldowns().addCooldown(stack, Math.max(0, this.ticks.getAsInt(null)));
            return true;
        }

        return false;
    }

    @Override
    public ItemActionSerializer<?> getSerializer() {
        return ItemActionSerializers.COOLDOWN.get();
    }

    public static class Serializer extends ItemActionSerializer<CooldownItemAction> {

        @Override
        public MapCodec<CooldownItemAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemAction, CooldownItemAction> builder, HolderLookup.Provider provider) {
            builder.setName("Cooldown")
                    .setDescription("Puts the item stack on cooldown for the holding player.")
                    .addOptional("ticks", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Duration of the cooldown (in ticks).", 20)
                    .addExampleObject(new CooldownItemAction(new StaticValue(20)));
        }
    }

}