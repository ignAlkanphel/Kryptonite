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
import org.jetbrains.annotations.NotNull;

public record IsBlockBlockCondition(Type type) implements BlockCondition {

    public static final MapCodec<IsBlockBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(IsBlockBlockCondition::type)
    ).apply(instance, IsBlockBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IsBlockBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Type.class), IsBlockBlockCondition::type,
            IsBlockBlockCondition::new
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
    public BlockConditionSerializer<IsBlockBlockCondition> getSerializer() {
        return BlockConditionSerializers.IS_BLOCK.get();
    }

    public static class Serializer extends BlockConditionSerializer<IsBlockBlockCondition> {

        @Override
        public MapCodec<IsBlockBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, IsBlockBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Block")
                    .setDescription("Checks what \"instanceof\" type the block is.")
                    .addOptional("type", TYPE_STRING, "The \"instanceof\" type.")
                    .addExampleObject(new IsBlockBlockCondition(Type.FLOWER))
                    .addExampleObject(new IsBlockBlockCondition(Type.CROP));
        }
    }

    public enum Type implements StringRepresentable {
        AIR,
        CROP,
        FLOWER,
        SAPLING,
        VEGETATION,
        BONEMEALABLE,
        FLUID,
        FALLING,
        LEAVES;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}