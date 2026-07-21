package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.NumberComparator;

public record DurabilityRelativeItemCondition(NumberComparator comparator, float compareTo) implements ItemCondition {

    public static final MapCodec<DurabilityRelativeItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(DurabilityRelativeItemCondition::comparator),
            Codec.floatRange(0F, 1F).fieldOf("compare_to").forGetter(DurabilityRelativeItemCondition::compareTo)
    ).apply(instance, DurabilityRelativeItemCondition::new));

    public static final StreamCodec<ByteBuf, DurabilityRelativeItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, DurabilityRelativeItemCondition::comparator,
            ByteBufCodecs.FLOAT, DurabilityRelativeItemCondition::compareTo,
            DurabilityRelativeItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        return stack.isDamageableItem()
                && comparator.compare(Math.abs((float) (stack.getMaxDamage() - stack.getDamageValue())) / (float) stack.getMaxDamage(), compareTo);
    }

    @Override
    public ItemConditionSerializer<DurabilityRelativeItemCondition> getSerializer() {
        return ItemConditionSerializers.DURABILITY_RELATIVE.get();
    }

    public static class Serializer extends ItemConditionSerializer<DurabilityRelativeItemCondition> {

        @Override
        public MapCodec<DurabilityRelativeItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, DurabilityRelativeItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Durability Relative")
                    .setDescription("Checks the current durability of the item relative to its max durability by percentage. The formula is \"currentDurability / maxDurability\"")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", SettingType.floatRange(0F, 1F), "The value that is being compared against")
                    .addExampleObject(new DurabilityRelativeItemCondition(NumberComparator.GREATER_OR_EQUAL, 0.9F));
        }
    }

}