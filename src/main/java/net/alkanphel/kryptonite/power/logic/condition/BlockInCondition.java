package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
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
import net.threetag.palladium.util.PalladiumHolderSet;

public record BlockInCondition(BlockFilter blockFilter) implements Condition {

    public static final MapCodec<BlockInCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(BlockInCondition::blockFilter)
    ).apply(instance, BlockInCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockFilter.CODEC), BlockInCondition::blockFilter,
            BlockInCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return blockFilter.test(entity.level(), entity.blockPosition());
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
                    .setDescription("Checks if a block is overlapping with the entity's feet.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filter(s) the block overlapping with the entity's feet must pass.")
                    .addExampleObject(new BlockInCondition(BlockFilter.EMPTY))
                    .addExampleObject(new BlockInCondition(BlockFilter.ofBlock((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("sand")))))))));
        }
    }

}