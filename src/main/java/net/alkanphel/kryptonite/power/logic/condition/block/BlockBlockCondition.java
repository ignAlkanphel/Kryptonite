package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public record BlockBlockCondition(HolderSet<Block> block) implements BlockCondition {

    public static final MapCodec<BlockBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("block").forGetter(BlockBlockCondition::block)
    ).apply(instance, BlockBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.BLOCK), BlockBlockCondition::block,
            BlockBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        Holder<Block> holder = context.blockState().typeHolder();
        return block.contains(holder);
    }

    @Override
    public BlockConditionSerializer<BlockBlockCondition> getSerializer() {
        return BlockConditionSerializers.BLOCK.get();
    }

    public static class Serializer extends BlockConditionSerializer<BlockBlockCondition> {

        @Override
        public MapCodec<BlockBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, BlockBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block")
                    .setDescription("Checks whether the block is of a certain type.")
                    .add("block", KryptoniteDocumented.TYPE_BLOCK_TYPE_HOLDER_SET, "Block IDs or tags this block needs to pass the check.")
                    .addExampleObject(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("stone"))))))
                    .addExampleObject(new BlockBlockCondition(new OrHolderSet<>(List.of(provider.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.STONE_BRICKS), HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("stone"))))))));
            }
        }

}