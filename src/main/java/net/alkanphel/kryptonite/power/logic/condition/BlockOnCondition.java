package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumHolderSet;

public record BlockOnCondition(BlockFilter blockFilter) implements Condition {

    public static final MapCodec<BlockOnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(BlockOnCondition::blockFilter)
    ).apply(instance, BlockOnCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockOnCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockFilter.CODEC), BlockOnCondition::blockFilter,
            BlockOnCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !entity.onGround()) return false;

        return blockFilter.test(entity.level(), entity.getOnPos());
    }

    @Override
    public ConditionSerializer<BlockOnCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_ON.get();
    }

    public static class Serializer extends ConditionSerializer<BlockOnCondition> {

        @Override
        public MapCodec<BlockOnCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockOnCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block On")
                    .setDescription("Checks if the entity is standing on a block.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filters(s) the block underneath the entity's feet must pass.")
                    .addExampleObject(new BlockOnCondition(BlockFilter.EMPTY))
                    .addExampleObject(new BlockOnCondition(BlockFilter.ofBlock((new PalladiumHolderSet.Direct<>(provider.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.BEDS))))));
        }
    }

}