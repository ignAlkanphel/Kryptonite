package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.List;

public record BlockInCondition(List<BlockCondition> blockConditions) implements Condition {

    public static final MapCodec<BlockInCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(BlockInCondition::blockConditions)
    ).apply(instance, BlockInCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.LIST_CODEC), BlockInCondition::blockConditions,
            BlockInCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, entity.level(), entity.blockPosition());
    }

    @Override
    public ConditionSerializer<BlockInCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_IN.get();
    }

    public static class Serializer extends ConditionSerializer<BlockInCondition> {

        @Override
        public MapCodec<BlockInCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockInCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block In")
                    .setDescription("Checks if a is overlapping with the entity's feet.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, these conditions must be fulfilled for the block that is overlapping with the entity's feet.")
                    .addExampleObject(new BlockInCondition(List.of()))
                    .addExampleObject(new BlockInCondition(List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("sand"))))))));
        }
    }

}