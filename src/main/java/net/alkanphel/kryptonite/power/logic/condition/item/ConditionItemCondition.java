package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;

public record ConditionItemCondition(Condition condition) implements ItemCondition {

    public static final MapCodec<ConditionItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("conditions", TrueCondition.INSTANCE).forGetter(ConditionItemCondition::condition)
    ).apply(instance, ConditionItemCondition::new));

    @Override
    public boolean test(ItemConditionContext context) {
        if (context.stack().isEmpty()) return false;

        Entity holder = ((EntityLinkedItemStack) (Object) context.stack()).kryptonite$getEntity();
        if (holder == null) return false;

        return condition.test(DataContext.forEntity(holder));
    }

    @Override
    public ItemConditionSerializer<ConditionItemCondition> getSerializer() {
        return ItemConditionSerializers.CONDITION.get();
    }

    public static class Serializer extends ItemConditionSerializer<ConditionItemCondition> {

        @Override
        public MapCodec<ConditionItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, ConditionItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Condition")
                    .setDescription("Checks if the item stack held by this entity meets conditions.")
                    .addOptional("conditions", TYPE_CONDITION_LIST, "Condition(s) the holding entity must meet. Returns false if the item stack has no holder.");
        }
    }

}