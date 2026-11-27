package net.alkanphel.kryptonite.power.logic.filter;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.mixin.common.DelayedDataComponentMapAccessor;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.context.ItemFilterContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.threetag.palladium.component.DelayedDataComponentMap;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.*;

public record ItemFilter(Operator operator, List<FilterType> filters) {

    public static final ItemFilter EMPTY = new ItemFilter(Operator.AND, List.of());

    private static final MapCodec<ItemFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Operator.CODEC.optionalFieldOf("operator", Operator.AND).forGetter(ItemFilter::operator),
            ExtraCodecs.compactListCodec(FilterType.CODEC).fieldOf("filters").forGetter(ItemFilter::filters)
    ).apply(instance, ItemFilter::new));

    public static final Codec<ItemFilter> CODEC = MAP_CODEC.codec();

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType filterType = FilterType.documentation(provider);
        return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                .addOptional("operator", SettingType.enumList(Operator.values()), "How the filters are combined: \"and\" needs all to pass, \"or\" needs at least one, \"not\" needs none.", Operator.AND)
                .add("filters", SettingType.combined(filterType, SettingType.array(filterType)), "Item filter type(s) that must pass.")
                .buildObject("Item Filters");
    }

    public boolean test(ItemFilterContext context) {
        return operator.test(filters, context);
    }

    public boolean test(Level level, ItemStack stack) {
        return test(new ItemFilterContext(level, stack));
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FilterType(
            Optional<AmountFilter> amount,
            Optional<Boolean> blockItem,
            Optional<DelayedDataComponentMap> components,
            Optional<CooldownFilter> cooldown,
            Optional<CooldownRelativeFilter> cooldownRelative,
            Optional<Boolean> damageable,
            Optional<DurabilityFilter> durability,
            Optional<DurabilityRelativeFilter> durabilityRelative,
            Optional<Boolean> empty,
            Optional<Boolean> enchantable,
            Optional<EnchantmentFilter> enchantment,
            Optional<EquipableFilter> equipable,
            Optional<FuelFilter> fuel,
            Optional<Condition> holderConditions,
            Optional<PalladiumHolderSet<Item>> item,
            Optional<String> modLoaded,
            Optional<Boolean> onCooldown,
            Optional<Boolean> smeltable
    ) {

        private static final MapCodec<Optional<Boolean>> ON_COOLDOWN_CODEC = Codec.BOOL.optionalFieldOf("on_cooldown");
        private static final MapCodec<Optional<Boolean>> SMELTABLE_CODEC = Codec.BOOL.optionalFieldOf("smeltable");

        private static final MapCodec<FilterType> MAP_CODEC = RecordCodecBuilder.<FilterType>mapCodec(instance -> instance.group(
                AmountFilter.CODEC.optionalFieldOf("amount").forGetter(FilterType::amount),
                Codec.BOOL.optionalFieldOf("block_item").forGetter(FilterType::blockItem),
                DelayedDataComponentMap.CODEC.optionalFieldOf("components").forGetter(FilterType::components),
                CooldownFilter.CODEC.optionalFieldOf("cooldown").forGetter(FilterType::cooldown),
                CooldownRelativeFilter.CODEC.optionalFieldOf("cooldown_relative").forGetter(FilterType::cooldownRelative),
                Codec.BOOL.optionalFieldOf("damageable").forGetter(FilterType::damageable),
                DurabilityFilter.CODEC.optionalFieldOf("durability").forGetter(FilterType::durability),
                DurabilityRelativeFilter.CODEC.optionalFieldOf("durability_relative").forGetter(FilterType::durabilityRelative),
                Codec.BOOL.optionalFieldOf("empty").forGetter(FilterType::empty),
                Codec.BOOL.optionalFieldOf("enchantable").forGetter(FilterType::enchantable),
                EnchantmentFilter.CODEC.optionalFieldOf("enchantment").forGetter(FilterType::enchantment),
                EquipableFilter.CODEC.optionalFieldOf("equipable").forGetter(FilterType::equipable),
                FuelFilter.CODEC.optionalFieldOf("fuel").forGetter(FilterType::fuel),
                Condition.CODEC.optionalFieldOf("holder_conditions").forGetter(FilterType::holderConditions),
                PalladiumHolderSet.codec(Registries.ITEM).optionalFieldOf("item").forGetter(FilterType::item),
                Codec.STRING.optionalFieldOf("mod_loaded").forGetter(FilterType::modLoaded)
        ).apply(instance, (amount, blockItem, components, cooldown, cooldownRelative, damageable, durability, durabilityRelative, empty, enchantable, enchantment, equipable, fuel, holderConditions, item, modLoaded) ->
                new FilterType(amount, blockItem, components, cooldown, cooldownRelative, damageable, durability, durabilityRelative, empty, enchantable, enchantment, equipable, fuel, holderConditions, item, modLoaded, Optional.empty(), Optional.empty()))
        ).dependent(ON_COOLDOWN_CODEC, filter -> Pair.of(filter.onCooldown(), ON_COOLDOWN_CODEC), FilterType::withOnCooldown
        ).dependent(SMELTABLE_CODEC, filter -> Pair.of(filter.smeltable(), SMELTABLE_CODEC), FilterType::withSmeltable);

        private FilterType withOnCooldown(Optional<Boolean> onCooldown) {
            return new FilterType(amount, blockItem, components, cooldown, cooldownRelative, damageable, durability, durabilityRelative, empty, enchantable, enchantment, equipable, fuel, holderConditions, item, modLoaded, onCooldown, smeltable);
        }

        private FilterType withSmeltable(Optional<Boolean> smeltable) {
            return new FilterType(amount, blockItem, components, cooldown, cooldownRelative, damageable, durability, durabilityRelative, empty, enchantable, enchantment, equipable, fuel, holderConditions, item, modLoaded, onCooldown, smeltable);
        }

        public static final Codec<FilterType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("amount", AmountFilter.documentation(provider), "Checks the amount of items in the stack.")
                    .addOptional("block_item", Documented.TYPE_BOOLEAN, "Checks if the item is a block item (an item that can place blocks).")
                    .addOptional("components", KryptoniteDocumented.TYPE_DATA_COMPONENT, "Checks if the item has these (data) components. Use an empty object, like \"minecraft:damage_resistant\": {}, to only check that the component exists.")
                    .addOptional("cooldown", CooldownFilter.documentation(provider), "Checks the remaining cooldown of the item for the holding player.")
                    .addOptional("cooldown_relative", CooldownRelativeFilter.documentation(provider), "Checks the remaining cooldown of the item relative to its total cooldown.")
                    .addOptional("damageable", Documented.TYPE_BOOLEAN, "Checks if the item can be damaged.")
                    .addOptional("durability", DurabilityFilter.documentation(provider), "Checks the current durability of the item.")
                    .addOptional("durability_relative", DurabilityRelativeFilter.documentation(provider), "Checks the current durability of the item relative to its max durability.")
                    .addOptional("empty", Documented.TYPE_BOOLEAN, "Checks if there is no item.")
                    .addOptional("enchantable", Documented.TYPE_BOOLEAN, "Checks if the item can be enchanted.")
                    .addOptional("enchantment", EnchantmentFilter.documentation(provider), "Checks the enchantment level of the item.")
                    .addOptional("equipable", EquipableFilter.documentation(provider), "Checks if the item can be equipped.")
                    .addOptional("fuel", FuelFilter.documentation(provider), "Checks the fuel time of the item.")
                    .addOptional("holder_conditions", Documented.TYPE_CONDITION_LIST, "Condition(s) the entity holding the item must meet. Fails if the item has no holder.")
                    .addOptional("item", KryptoniteDocumented.TYPE_ITEM_TYPE_HOLDER_SET, "Checks if the item is one of these item IDs or tags.")
                    .addOptional("mod_loaded", Documented.TYPE_STRING, "Checks if the mod with this ID is loaded.")
                    .addOptional("on_cooldown", Documented.TYPE_BOOLEAN, "Checks if the item is on cooldown for the holding player.")
                    .addOptional("smeltable", Documented.TYPE_BOOLEAN, "Checks if the item can be smelted.")
                    .buildObject("Item Filter Types");
        }

        public boolean isEmpty() {
            return amount.isEmpty()
                    && blockItem.isEmpty()
                    && components.isEmpty()
                    && cooldown.isEmpty()
                    && cooldownRelative.isEmpty()
                    && damageable.isEmpty()
                    && durability.isEmpty()
                    && durabilityRelative.isEmpty()
                    && empty.isEmpty()
                    && enchantable.isEmpty()
                    && enchantment.isEmpty()
                    && equipable.isEmpty()
                    && fuel.isEmpty()
                    && holderConditions.isEmpty()
                    && item.isEmpty()
                    && modLoaded.isEmpty()
                    && onCooldown.isEmpty()
                    && smeltable.isEmpty();
        }

        public boolean test(ItemFilterContext context) {
            return testAmount(context)
                    && testBlockItem(context)
                    && testComponents(context)
                    && testCooldown(context)
                    && testCooldownRelative(context)
                    && testDamageable(context)
                    && testDurability(context)
                    && testDurabilityRelative(context)
                    && testEmpty(context)
                    && testEnchantable(context)
                    && testEnchantment(context)
                    && testEquipable(context)
                    && testFuel(context)
                    && testHolderConditions(context)
                    && testItem(context)
                    && testModLoaded()
                    && testOnCooldown(context)
                    && testSmeltable(context);
        }

        private boolean testAmount(ItemFilterContext context) {
            return amount.isEmpty() || amount.get().test(context);
        }

        private boolean testBlockItem(ItemFilterContext context) {
            return blockItem.isEmpty() || blockItem.get() == context.stack().getItem() instanceof BlockItem;
        }

        private boolean testComponents(ItemFilterContext context) {
            return components.isEmpty() || testComponents(components.get(), context);
        }

        private boolean testCooldown(ItemFilterContext context) {
            return cooldown.isEmpty() || cooldown.get().test(context);
        }

        private boolean testCooldownRelative(ItemFilterContext context) {
            return cooldownRelative.isEmpty() || cooldownRelative.get().test(context);
        }

        private boolean testDamageable(ItemFilterContext context) {
            return damageable.isEmpty() || damageable.get() == context.stack().isDamageableItem();
        }

        private boolean testDurability(ItemFilterContext context) {
            return durability.isEmpty() || durability.get().test(context);
        }

        private boolean testDurabilityRelative(ItemFilterContext context) {
            return durabilityRelative.isEmpty() || durabilityRelative.get().test(context);
        }

        private boolean testEmpty(ItemFilterContext context) {
            return empty.isEmpty() || empty.get() == context.stack().isEmpty();
        }

        private boolean testEnchantable(ItemFilterContext context) {
            return enchantable.isEmpty() || enchantable.get() == context.stack().isEnchantable();
        }

        private boolean testEnchantment(ItemFilterContext context) {
            return enchantment.isEmpty() || enchantment.get().test(context);
        }

        private boolean testEquipable(ItemFilterContext context) {
            return equipable.isEmpty() || equipable.get().test(context);
        }

        private boolean testFuel(ItemFilterContext context) {
            return fuel.isEmpty() || fuel.get().test(context);
        }

        private boolean testHolderConditions(ItemFilterContext context) {
            if (holderConditions.isEmpty()) return true;

            Entity holder = getHolderEntity(context);
            return holder != null && holderConditions.get().test(DataContext.forEntity(holder));
        }

        private boolean testItem(ItemFilterContext context) {
            return item.isEmpty() || item.get().contains(context.stack().typeHolder());
        }

        private boolean testModLoaded() {
            return modLoaded.isEmpty() || ModList.get().isLoaded(modLoaded.get());
        }

        private boolean testOnCooldown(ItemFilterContext context) {
            if (onCooldown.isEmpty()) return true;

            Player player = getHolder(context);
            return player != null && onCooldown.get() == player.getCooldowns().isOnCooldown(context.stack());
        }

        private boolean testSmeltable(ItemFilterContext context) {
            return smeltable.isEmpty() || smeltable.get() == isSmeltable(context);
        }

        // -------------------------------------------------------------------------------------------------------------------------------

        private static boolean isSmeltable(ItemFilterContext context) {
            Level level = context.level();
            return level.recipeAccess() instanceof RecipeManager recipeManager && recipeManager.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(context.stack()), level).isPresent();
        }

        private static boolean testComponents(DelayedDataComponentMap components, ItemFilterContext context) {
            ItemStack stack = context.stack();
            Map<DataComponentType<?>, Dynamic<?>> values = ((DelayedDataComponentMapAccessor) components).kryptonite$getValues();
            var ops = context.level().registryAccess().createSerializationContext(JsonOps.INSTANCE);

            for (Map.Entry<DataComponentType<?>, Dynamic<?>> entry : values.entrySet()) {
                DataComponentType type = entry.getKey();

                if (!stack.has(type)) return false;

                Dynamic<?> dynamic = entry.getValue();

                if (dynamic.getValue() instanceof JsonObject jsonObject && jsonObject.isEmpty()) {
                    continue;
                }

                Object expected = type.codecOrThrow().parse(ops, dynamic.getValue()).getOrThrow();

                if (!Objects.equals(expected, stack.get(type))) {
                    return false;
                }
            }

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record AmountFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<AmountFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(AmountFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(AmountFilter::compareTo)
        ).apply(instance, AmountFilter::new));

        public static final Codec<AmountFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the item amount is compared.")
                    .add("compare_to", Documented.TYPE_INT_VALUE, "Value the item amount is compared to.")
                    .buildObject("Amount");
        }

        public boolean test(ItemFilterContext context) {
            return comparator.compare(context.stack().getCount(), compareTo.getAsInt(null));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record CooldownFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<CooldownFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(CooldownFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(CooldownFilter::compareTo)
        ).apply(instance, CooldownFilter::new));

        public static final Codec<CooldownFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the remaining cooldown is compared.")
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Cooldown in ticks the remaining cooldown is compared to.")
                    .buildObject("Cooldown");
        }

        public boolean test(ItemFilterContext context) {
            Player player = getHolder(context);
            if (player == null) return false;

            return comparator.compare(getCooldown(player, context.stack()), Math.max(0, compareTo.getAsInt(null)));
        }

        private static int getCooldown(Player player, ItemStack stack) {
            ItemCooldowns cooldowns = player.getCooldowns();
            ItemCooldowns.CooldownInstance entry = cooldowns.cooldowns.get(cooldowns.getCooldownGroup(stack));

            return entry != null ? Math.abs(entry.endTime() - entry.startTime()) : 0;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record CooldownRelativeFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<CooldownRelativeFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(CooldownRelativeFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(CooldownRelativeFilter::compareTo)
        ).apply(instance, CooldownRelativeFilter::new));

        public static final Codec<CooldownRelativeFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the relative cooldown is compared.")
                    .add("compare_to", KryptoniteSettingType.floatValueRange(0F, 1F), "Value the relative cooldown is compared to, calculated as remaining cooldown / total cooldown.")
                    .buildObject("Relative Cooldown");
        }

        public boolean test(ItemFilterContext context) {
            Player player = getHolder(context);
            if (player == null) return false;

            float progress = player.getCooldowns().getCooldownPercent(context.stack(), 0F);
            return comparator.compare(progress, Mth.clamp(compareTo.getAsFloat(null), 0F, 1F));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record DurabilityFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<DurabilityFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(DurabilityFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(DurabilityFilter::compareTo)
        ).apply(instance, DurabilityFilter::new));

        public static final Codec<DurabilityFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the durability is compared.")
                    .add("compare_to", Documented.TYPE_INT_VALUE, "Value the durability is compared to.")
                    .buildObject("Durability");
        }

        public boolean test(ItemFilterContext context) {
            ItemStack stack = context.stack();
            return stack.isDamageableItem() && comparator.compare(Math.abs(stack.getMaxDamage() - stack.getDamageValue()), compareTo.getAsInt(null));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record DurabilityRelativeFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<DurabilityRelativeFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(DurabilityRelativeFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(DurabilityRelativeFilter::compareTo)
        ).apply(instance, DurabilityRelativeFilter::new));

        public static final Codec<DurabilityRelativeFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the relative durability is compared.")
                    .add("compare_to", KryptoniteSettingType.floatValueRange(0F, 1F), "Value the relative durability is compared to, calculated as current durability / max durability.")
                    .buildObject("Relative Durability");
        }

        public boolean test(ItemFilterContext context) {
            ItemStack stack = context.stack();
            return stack.isDamageableItem() && comparator.compare(Math.abs((float) (stack.getMaxDamage() - stack.getDamageValue())) / (float) stack.getMaxDamage(), Mth.clamp(compareTo.getAsFloat(null), 0F, 1F));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record EnchantmentFilter(Optional<Holder<Enchantment>> enchantment, NumberComparator comparator, Value compareTo) {

        private static final MapCodec<EnchantmentFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Enchantment.CODEC.optionalFieldOf("enchantment").forGetter(EnchantmentFilter::enchantment),
                NumberComparator.CODEC.fieldOf("comparator").forGetter(EnchantmentFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(EnchantmentFilter::compareTo)
        ).apply(instance, EnchantmentFilter::new));

        public static final Codec<EnchantmentFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("enchantment", SettingType.simple("Enchantment"), "The enchantment to check. If omitted, checks any enchantment.")
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the enchantment level is compared.")
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Value the enchantment level is compared to.")
                    .buildObject("Enchantment");
        }

        public boolean test(ItemFilterContext context) {
            ItemEnchantments enchantments = context.stack().get(DataComponents.ENCHANTMENTS);
            if (enchantments == null) return false;

            int compare = Math.max(0, compareTo.getAsInt(null));
            return enchantment.isPresent() ? testSingle(enchantments, compare) : testAny(enchantments, compare);
        }

        private boolean testSingle(ItemEnchantments enchantments, int compare) {
            return comparator.compare(enchantments.getLevel(enchantment.get()), compare);
        }

        private boolean testAny(ItemEnchantments enchantments, int compare) {
            for (var entry : enchantments.entrySet()) {
                if (comparator.compare(entry.getIntValue(), compare)) {
                    return true;
                }
            }

            return false;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record EquipableFilter(Optional<EquipmentSlotGroup> equipmentSlot) {

        private static final MapCodec<EquipableFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                EquipmentSlotGroup.CODEC.optionalFieldOf("equipment_slot").forGetter(EquipableFilter::equipmentSlot)
        ).apply(instance, EquipableFilter::new));

        public static final Codec<EquipableFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "Checks if the item can be equipped in this equipment slot.")
                    .buildObject("Equipable");
        }

        public boolean test(ItemFilterContext context) {
            Equippable equippable = context.stack().get(DataComponents.EQUIPPABLE);
            if (equippable == null) return false;

            return equipmentSlot.isEmpty() || equipmentSlot.get().test(equippable.slot());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FuelFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<FuelFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(FuelFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(FuelFilter::compareTo)
        ).apply(instance, FuelFilter::new));

        public static final Codec<FuelFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the fuel time is compared.", ">")
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "Fuel time in ticks the fuel time is compared to.")
                    .buildObject("Fuel");
        }

        public boolean test(ItemFilterContext context) {
            int burnTime = context.stack().getBurnTime(null, context.level().fuelValues());
            return comparator.compare(burnTime, Math.max(0, compareTo.getAsInt(null)));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public enum Operator {
        AND, OR, NOT;

        public static final Codec<Operator> CODEC = Codec.STRING.xmap(name -> Operator.valueOf(name.toUpperCase(Locale.ROOT)), operator -> operator.name().toLowerCase(Locale.ROOT));

        public boolean test(List<FilterType> filters, ItemFilterContext context) {
            return switch (this) {
                case AND -> filters.stream().allMatch(filter -> filter.test(context));
                case OR -> filters.stream().anyMatch(filter -> filter.test(context));
                case NOT -> filters.stream().noneMatch(filter -> filter.test(context));
            };
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static Entity getHolderEntity(ItemFilterContext context) {
        ItemStack stack = context.stack();
        if (stack.isEmpty()) return null;

        return ((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity();
    }

    private static Player getHolder(ItemFilterContext context) {
        return getHolderEntity(context) instanceof Player player ? player : null;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static <T> Optional<T> none() {
        return Optional.empty();
    }

    private static ItemFilter single(FilterType type) {
        return new ItemFilter(Operator.AND, List.of(type));
    }

    public static ItemFilter ofAmount(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(Optional.of(new AmountFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofBlockItem(boolean blockItem) {
        return single(new FilterType(none(), Optional.of(blockItem), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofComponents(DelayedDataComponentMap components) {
        return single(new FilterType(none(), none(), Optional.of(components), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofCooldown(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), Optional.of(new CooldownFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofCooldownRelative(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), Optional.of(new CooldownRelativeFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofDamageable(boolean damageable) {
        return single(new FilterType(none(), none(), none(), none(), none(), Optional.of(damageable), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofDurability(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), Optional.of(new DurabilityFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofDurabilityRelative(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), Optional.of(new DurabilityRelativeFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofEmpty(boolean empty) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(empty), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofEnchantable(boolean enchantable) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(enchantable), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofEnchantment(Optional<Holder<Enchantment>> enchantment, NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new EnchantmentFilter(enchantment, comparator, compareTo)), none(), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofEquipable(Optional<EquipmentSlotGroup> equipmentSlot) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new EquipableFilter(equipmentSlot)), none(), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofFuel(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new FuelFilter(comparator, compareTo)), none(), none(), none(), none(), none()));
    }

    public static ItemFilter ofHolderConditions(Condition conditions) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(conditions), none(), none(), none(), none()));
    }

    public static ItemFilter ofItem(PalladiumHolderSet<Item> item) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(item), none(), none(), none()));
    }

    public static ItemFilter ofModLoaded(String modId) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(modId), none(), none()));
    }

    public static ItemFilter ofOnCooldown(boolean onCooldown) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(onCooldown), none()));
    }

    public static ItemFilter ofSmeltable(boolean smeltable) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(smeltable)));
    }

}