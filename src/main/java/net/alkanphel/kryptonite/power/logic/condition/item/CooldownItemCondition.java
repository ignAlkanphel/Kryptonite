package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record CooldownItemCondition(NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<CooldownItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(CooldownItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(CooldownItemCondition::compareTo)
    ).apply(instance, CooldownItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, CooldownItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), CooldownItemCondition::compareTo,
            CooldownItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        if (stack.isEmpty()) return false;

        if (!(((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity() instanceof Player player)) {
            return false;
        }

        ItemCooldowns cooldowns = player.getCooldowns();
        ItemCooldowns.CooldownInstance cooldownEntry = cooldowns.cooldowns.get(cooldowns.getCooldownGroup(stack));

        int cooldown = cooldownEntry != null
                ? Math.abs(cooldownEntry.endTime() - cooldownEntry.startTime())
                : 0;

        return comparator.compare(cooldown, Math.max(0, compareTo.getAsInt(null)));
    }

    @Override
    public ItemConditionSerializer<CooldownItemCondition> getSerializer() {
        return ItemConditionSerializers.COOLDOWN.get();
    }

    public static class Serializer extends ItemConditionSerializer<CooldownItemCondition> {

        @Override
        public MapCodec<CooldownItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, CooldownItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Cooldown")
                    .setDescription("Checks the remaining cooldown duration of the item stack in ticks for the holding player.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "The cooldown duration in ticks to compare against.")
                    .addExampleObject(new CooldownItemCondition(NumberComparator.GREATER_OR_EQUAL, new StaticValue(20)));
        }
    }

}