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
import java.util.Optional;

public record AndBlockCondition(List<BlockCondition> blockConditions) implements BlockCondition {

    public static final MapCodec<AndBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions").forGetter(c -> Optional.of(c.blockConditions())),
            BlockCondition.LIST_CODEC.optionalFieldOf("conditions").forGetter(c -> Optional.empty())
    ).apply(instance, (blockConditions, conditions) ->
            new AndBlockCondition(blockConditions.orElseGet(() -> conditions.orElse(List.of())))
    ));

    @Override
    public boolean test(BlockConditionContext context) {
        for (BlockCondition blockCondition : this.blockConditions) {
            if (!blockCondition.test(context)) {
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
                    .setDescription("Allows you to group multiple block conditions into one using the AND logic. All of the given block conditions must be true for this one to be true aswell. The namespace alias \"palladium:and\" is supported.")
                    .add("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "List of block conditions. This field supports aliases: \"block_conditions\" & \"conditions\"")
                    .addExampleObject(new AndBlockCondition(Arrays.asList(new FrictionBlockCondition(NumberComparator.EQUALS, new StaticValue(0.98)), new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("ice"))))))));
        }
    }

}