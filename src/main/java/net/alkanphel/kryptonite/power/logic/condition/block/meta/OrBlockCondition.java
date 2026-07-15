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

public record OrBlockCondition(List<BlockCondition> conditions) implements BlockCondition {

    public static final MapCodec<OrBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.fieldOf("conditions").forGetter(OrBlockCondition::conditions)
    ).apply(instance, OrBlockCondition::new));

    @Override
    public boolean test(BlockConditionContext context) {
        for (BlockCondition condition : this.conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockConditionSerializer<OrBlockCondition> getSerializer() {
        return BlockConditionSerializers.OR.get();
    }

    public static class Serializer extends BlockConditionSerializer<OrBlockCondition> {

        @Override
        public MapCodec<OrBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, OrBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("OR")
                    .setDescription("Allows you to group multiple block conditions into one using the OR logic. At least one of the given block conditions must be true for this one to be true aswell.")
                    .add("conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "List of conditions")
                    .addExampleObject(new OrBlockCondition(Arrays.asList(new FrictionBlockCondition(NumberComparator.EQUALS, new StaticValue(0.98)), new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("ice"))))))));
        }
    }

}