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
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record HolderConditionItemCondition(Optional<Condition> conditions) implements ItemCondition {

    public static final MapCodec<HolderConditionItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("conditions").forGetter(HolderConditionItemCondition::conditions)
    ).apply(instance, HolderConditionItemCondition::new));

    @Override
    public boolean test(ItemConditionContext context) {
        if (context.stack().isEmpty()) return false;

        Entity holder = ((EntityLinkedItemStack) (Object) context.stack()).kryptonite$getEntity();
        if (holder == null) return false;

        return conditions.map(c -> c.test(DataContext.forEntity(holder))).orElse(true);
    }

    @Override
    public ItemConditionSerializer<HolderConditionItemCondition> getSerializer() {
        return ItemConditionSerializers.HOLDER_CONDITION.get();
    }

    public static class Serializer extends ItemConditionSerializer<HolderConditionItemCondition> {

        @Override
        public MapCodec<HolderConditionItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, HolderConditionItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Holder Condition")
                    .setDescription("Checks whether the entity holding this item stack fulfills the specified conditions. Returns false if the item stack has no holder.")
                    .addOptional("conditions", TYPE_CONDITION_LIST, "If specified, these condition must be fulfilled by the holding entity. If omitted, it returns true when a holder exists.");
        }
    }

}