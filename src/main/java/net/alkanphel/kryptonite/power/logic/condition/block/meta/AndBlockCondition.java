package net.alkanphel.kryptonite.power.logic.condition.block.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.FrictionBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.util.NumberComparator;

import java.util.Arrays;
import java.util.List;

public record AndBlockCondition(List<BlockCondition> conditions) implements BlockCondition {

    public static final MapCodec<AndBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.fieldOf("conditions").forGetter(AndBlockCondition::conditions)
    ).apply(instance, AndBlockCondition::new));

    @Override
    public boolean test(BlockConditionContext context) {
        for (BlockCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockConditionSerializer<AndBlockCondition> getSerializer() {
        return BlockConditionSerializers.AND.get();
    }

    public static class Serializer extends BlockConditionSerializer<AndBlockCondition> {

        @Override
        public MapCodec<AndBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, AndBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("AND")
                    .setDescription("Allows you to group multiple block conditions into one using the AND logic. All of the given block conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "List of conditions")
                    .addExampleObject(new AndBlockCondition(Arrays.asList(new FrictionBlockCondition(NumberComparator.EQUALS, new StaticValue(0.98)), new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("ice"))))))));
        }
    }

}