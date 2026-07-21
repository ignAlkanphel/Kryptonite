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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record CooldownRelativeItemCondition(NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<CooldownRelativeItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(CooldownRelativeItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(CooldownRelativeItemCondition::compareTo)
    ).apply(instance, CooldownRelativeItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownRelativeItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, CooldownRelativeItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), CooldownRelativeItemCondition::compareTo,
            CooldownRelativeItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        if (stack.isEmpty()) return false;

        if (!(((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity() instanceof Player player)) {
            return false;
        }

        float cooldownProgress = player.getCooldowns().getCooldownPercent(stack, 0F);
        return comparator.compare(cooldownProgress, Mth.clamp(compareTo.getAsFloat(null), 0F, 1F));
    }

    @Override
    public ItemConditionSerializer<CooldownRelativeItemCondition> getSerializer() {
        return ItemConditionSerializers.COOLDOWN_RELATIVE.get();
    }

    public static class Serializer extends ItemConditionSerializer<CooldownRelativeItemCondition> {

        @Override
        public MapCodec<CooldownRelativeItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, CooldownRelativeItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Cooldown Relative")
                    .setDescription("Checks the current cooldown progress of the item stack relative to its total cooldown by percentage. The formula is \"remainingCooldown / totalCooldown\".")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", KryptoniteSettingType.floatValueRange(0F, 1F), "The value that is being compared against")
                    .addExampleObject(new CooldownRelativeItemCondition(NumberComparator.GREATER_OR_EQUAL, new StaticValue(0.9F)));
        }
    }

}