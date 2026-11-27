package net.alkanphel.kryptonite.power.logic.mutation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.context.ItemMutationContext;
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

public record ItemMutation(List<MutationType> mutations) {

    public static final ItemMutation EMPTY = new ItemMutation(List.of());
    public static final Codec<ItemMutation> CODEC = ExtraCodecs.compactListCodec(MutationType.CODEC).xmap(ItemMutation::new, ItemMutation::mutations);

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType mutationType = MutationType.documentation(provider);
        return SettingType.combined(mutationType, SettingType.array(mutationType));
    }

    public boolean run(ItemMutationContext context) {
        if (context.slotAccess() == null || context.slotAccess().get().isEmpty()) {
            return false;
        }

        boolean result = false;

        for (MutationType mutation : mutations) {
            result |= mutation.run(context);
        }

        return result;
    }

    public boolean run(Level level, SlotAccess slotAccess) {
        if (level instanceof ServerLevel serverLevel) {
            return run(new ItemMutationContext(serverLevel, slotAccess));
        }

        return false;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record MutationType(
            Optional<Value> consume,
            Optional<Value> cooldown,
            Optional<DamageMutation> damage,
            Optional<List<Action>> action
    ) {

        private static final MapCodec<MutationType> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Value.NUMBER_CODEC.optionalFieldOf("consume").forGetter(MutationType::consume),
                Value.NUMBER_CODEC.optionalFieldOf("cooldown").forGetter(MutationType::cooldown),
                DamageMutation.CODEC.optionalFieldOf("damage").forGetter(MutationType::damage),
                Action.LIST_CODEC.optionalFieldOf("actions").forGetter(MutationType::action)
        ).apply(instance, MutationType::new));

        public static final Codec<MutationType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("consume", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of items to remove.")
                    .addOptional("cooldown", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Duration of the cooldown in ticks.")
                    .addOptional("damage", DamageMutation.documentation(provider), "Damages the item stack.")
                    .addOptional("actions", Documented.TYPE_ACTION_LIST, "Action(s) performed on the entity holding this item stack. Does nothing if the item has no holder.")
                    .buildObject("Item Mutation Types");
        }

        public boolean isEmpty() {
            return consume.isEmpty()
                    && cooldown.isEmpty()
                    && damage.isEmpty()
                    && action.isEmpty();
        }

        public boolean run(ItemMutationContext context) {
            return runConsume(context)
                    | runCooldown(context)
                    | runDamage(context)
                    | runAction(context);
        }

        private boolean runConsume(ItemMutationContext context) {
            if (consume.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null || slot.get().isEmpty()) return false;

            slot.get().shrink(Math.max(0, consume.get().getAsInt(null)));
            return true;
        }

        private boolean runCooldown(ItemMutationContext context) {
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

        private boolean runDamage(ItemMutationContext context) {
            if (damage.isEmpty()) return false;

            SlotAccess slot = context.slotAccess();
            if (slot == null) return false;

            ItemStack stack = slot.get();
            if (stack.isEmpty()) return false;

            return damage.get().run(context, stack);
        }

        private boolean runAction(ItemMutationContext context) {
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

    public record DamageMutation(Value amount, BooleanValueHolder ignoreUnbreaking) {

        private static final MapCodec<DamageMutation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Value.NUMBER_CODEC.optionalFieldOf("amount", new net.threetag.palladium.logic.value.StaticValue(1)).forGetter(DamageMutation::amount),
                BooleanValueHolder.CODEC.optionalFieldOf("ignore_unbreaking", BooleanValueHolder.FALSE).forGetter(DamageMutation::ignoreUnbreaking)
        ).apply(instance, DamageMutation::new));

        public static final Codec<DamageMutation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("amount", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Amount of damage to apply to the item stack.", 1)
                    .addOptional("ignore_unbreaking", Documented.TYPE_BOOLEAN_VALUE, "If true, it will ignore the \"minecraft:unbreaking\" enchantment.", false)
                    .buildObject("Damage");
        }

        public boolean run(ItemMutationContext context, ItemStack stack) {
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

    private static ItemMutation single(MutationType type) {
        return new ItemMutation(List.of(type));
    }

    public static ItemMutation ofConsume(Value amount) {
        return single(new MutationType(Optional.of(amount), none(), none(), none()));
    }

    public static ItemMutation ofCooldown(Value ticks) {
        return single(new MutationType(none(), Optional.of(ticks), none(), none()));
    }

    public static ItemMutation ofDamage(Value amount, BooleanValueHolder ignoreUnbreaking) {
        return single(new MutationType(none(), none(), Optional.of(new DamageMutation(amount, ignoreUnbreaking)), none()));
    }

    public static ItemMutation ofAction(List<Action> action) {
        return single(new MutationType(none(), none(), none(), Optional.of(action)));
    }

}