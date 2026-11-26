package net.alkanphel.kryptonite.power.logic.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.context.ItemOperationContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

import java.util.List;
import java.util.Optional;

public record ItemOperation(List<OperationType> operations) {

    public static final ItemOperation EMPTY = new ItemOperation(List.of());
    public static final Codec<ItemOperation> CODEC = ExtraCodecs.compactListCodec(OperationType.CODEC).xmap(ItemOperation::new, ItemOperation::operations);

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType operationType = OperationType.documentation(provider);
        return SettingType.combined(operationType, SettingType.array(operationType));
    }

    public boolean run(ItemOperationContext context) {
        if (context.slotAccess() == null || context.slotAccess().get().isEmpty()) {
            return false;
        }

        boolean result = false;

        for (OperationType operation : operations) {
            result |= operation.run(context);
        }

        return result;
    }

    public boolean run(Level level, SlotAccess slotAccess) {
        if (level instanceof ServerLevel serverLevel) {
            return run(new ItemOperationContext(serverLevel, slotAccess));
        }

        return false;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record OperationType(
            Optional<Value> consume,
            Optional<Value> cooldown,
            Optional<DamageOperation> damage,
            Optional<List<Action>> action
    ) {

        private static final MapCodec<OperationType> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Value.NUMBER_CODEC.optionalFieldOf("consume").forGetter(OperationType::consume),
                Value.NUMBER_CODEC.optionalFieldOf("cooldown").forGetter(OperationType::cooldown),
                DamageOperation.CODEC.optionalFieldOf("damage").forGetter(OperationType::damage),
                Action.LIST_CODEC.optionalFieldOf("actions").forGetter(OperationType::action)
        ).apply(instance, OperationType::new));

        public static final Codec<OperationType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("consume", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of items to remove.")
                    .addOptional("cooldown", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Duration of the cooldown in ticks.")
                    .addOptional("damage", DamageOperation.documentation(provider), "Damages the item stack.")
                    .addOptional("actions", Documented.TYPE_ACTION_LIST, "Action(s) performed on the entity holding this item stack. Does nothing if the item has no holder.")
                    .buildObject("Item Operation Types");
        }

        public boolean isEmpty() {
            return consume.isEmpty()
                    && cooldown.isEmpty()
                    && damage.isEmpty()
                    && action.isEmpty();
        }

        public boolean run(ItemOperationContext context) {
            return runConsume(context)
                    | runCooldown(context)
                    | runDamage(context)
                    | runAction(context);
        }

        private boolean runConsume(ItemOperationContext context) {
            if (consume.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null || slot.get().isEmpty()) return false;

            slot.get().shrink(Math.max(0, consume.get().getAsInt(null)));
            return true;
        }

        private boolean runCooldown(ItemOperationContext context) {
            if (cooldown.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null) return false;

            ItemStack stack = slot.get();
            if (context.level().isClientSide() || stack.isEmpty()) return false;

            if (getHolderEntity(stack) instanceof Player player) {
                player.getCooldowns().addCooldown(stack, Math.max(0, cooldown.get().getAsInt(null)));
                return true;
            }

            return false;
        }

        private boolean runDamage(ItemOperationContext context) {
            if (damage.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null) return false;

            ItemStack stack = slot.get();
            if (stack.isEmpty()) return false;

            return damage.get().run(context, stack);
        }

        private boolean runAction(ItemOperationContext context) {
            if (action.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null) return false;

            ItemStack stack = slot.get();
            if (stack.isEmpty()) return false;

            Entity holder = getHolderEntity(stack);
            if (holder == null) return false;

            Action.runList(action.get(), DataContext.forEntity(holder));
            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record DamageOperation(Value amount, BooleanValueHolder ignoreUnbreaking) {

        private static final MapCodec<DamageOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Value.NUMBER_CODEC.optionalFieldOf("amount", new net.threetag.palladium.logic.value.StaticValue(1)).forGetter(DamageOperation::amount),
                BooleanValueHolder.CODEC.optionalFieldOf("ignore_unbreaking", BooleanValueHolder.FALSE).forGetter(DamageOperation::ignoreUnbreaking)
        ).apply(instance, DamageOperation::new));

        public static final Codec<DamageOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("amount", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of damage to apply to the item stack.", 1)
                    .addOptional("ignore_unbreaking", Documented.TYPE_BOOLEAN_VALUE, "If true, it will ignore the \"minecraft:unbreaking\" enchantment.", false)
                    .buildObject("Damage");
        }

        public boolean run(ItemOperationContext context, ItemStack stack) {
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
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static Entity getHolderEntity(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return ((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static <T> Optional<T> none() {
        return Optional.empty();
    }

    private static ItemOperation single(OperationType type) {
        return new ItemOperation(List.of(type));
    }

    public static ItemOperation of(OperationType... operations) {
        return new ItemOperation(List.of(operations));
    }

    public static ItemOperation ofConsume(Value amount) {
        return single(new OperationType(Optional.of(amount), none(), none(), none()));
    }

    public static ItemOperation ofCooldown(Value ticks) {
        return single(new OperationType(none(), Optional.of(ticks), none(), none()));
    }

    public static ItemOperation ofDamage(Value amount, BooleanValueHolder ignoreUnbreaking) {
        return single(new OperationType(none(), none(), Optional.of(new DamageOperation(amount, ignoreUnbreaking)), none()));
    }

    public static ItemOperation ofAction(List<Action> action) {
        return single(new OperationType(none(), none(), none(), Optional.of(action)));
    }

}