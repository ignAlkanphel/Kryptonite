package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record BlockInstanceofBlockCondition(Type type) implements BlockCondition {

    public static final MapCodec<BlockInstanceofBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("instanceof").forGetter(BlockInstanceofBlockCondition::type)
    ).apply(instance, BlockInstanceofBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockInstanceofBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Type.class), BlockInstanceofBlockCondition::type,
            BlockInstanceofBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        var state = context.blockState();

        return switch (type()) {
            case AIR -> state.isAir();
            case CROP -> state.getBlock() instanceof CropBlock;
            case FLOWER -> state.getBlock() instanceof FlowerBlock;
            case SAPLING -> state.getBlock() instanceof SaplingBlock;
            case VEGETATION -> state.getBlock() instanceof VegetationBlock;
            case BONEMEALABLE -> state.getBlock() instanceof BonemealableBlock;
            case FLUID -> !state.getFluidState().isEmpty();
            case FALLING -> state.getBlock() instanceof FallingBlock;
            case LEAVES -> state.getBlock() instanceof LeavesBlock;
        };
    }

    @Override
    public BlockConditionSerializer<BlockInstanceofBlockCondition> getSerializer() {
        return BlockConditionSerializers.BLOCK_INSTANCEOF.get();
    }

    public static class Serializer extends BlockConditionSerializer<BlockInstanceofBlockCondition> {

        @Override
        public MapCodec<BlockInstanceofBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, BlockInstanceofBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Instanceof")
                    .setDescription("Checks what \"instanceof\" type the block is.")
                    .add("instanceof", SettingType.enumList(Type.values()), "The \"instanceof\" type.")
                    .addExampleObject(new BlockInstanceofBlockCondition(Type.FLOWER))
                    .addExampleObject(new BlockInstanceofBlockCondition(Type.CROP));
        }
    }

    public enum Type implements StringRepresentable {
        AIR, CROP, FLOWER, SAPLING, VEGETATION, BONEMEALABLE, FLUID, FALLING, LEAVES;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}