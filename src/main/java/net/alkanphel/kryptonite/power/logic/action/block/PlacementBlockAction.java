package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PlacementBlockAction extends BlockAction {

    public static final MapCodec<PlacementBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.SET).forGetter(a -> a.mode),
            BlockState.CODEC.optionalFieldOf("block", Blocks.AIR.defaultBlockState()).forGetter(a -> a.blockState)
    ).apply(instance, PlacementBlockAction::new));

    private final Mode mode;
    private final BlockState blockState;

    public PlacementBlockAction(Mode mode, BlockState blockState) {
        this.mode = mode;
        this.blockState = blockState;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        return switch (this.mode) {
            case ADD -> context.direction().map(direction -> {
                context.level().setBlockAndUpdate(context.pos().relative(direction), this.blockState);
                return true;
            }).orElse(false);

            case SET -> {
                context.level().setBlockAndUpdate(context.pos(), this.blockState);
                yield true;
            }

            case FALLING -> {
                ServerLevel level = context.level();
                BlockState state = level.getBlockState(context.pos());
                if (state.isAir()) yield false;

                FallingBlockEntity.fall(level, context.pos(), state);
                level.setBlockAndUpdate(context.pos(), Blocks.AIR.defaultBlockState());
                yield true;
            }
        };
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.PLACEMENT.get();
    }

    public static class Serializer extends BlockActionSerializer<PlacementBlockAction> {

        @Override
        public MapCodec<PlacementBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, PlacementBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Placement")
                    .setDescription("Places or replaces a block at the action position, or makes the block fall as an entity.")
                    .addOptional("mode", SettingType.enumList(Mode.values()), "The placement mode to use.", Mode.SET)
                    .addOptional("block", TYPE_BLOCK_STATE, "The block state to place. Only used by the \"add\" & \"set\" modes.", Blocks.STONE.defaultBlockState())
                    .addExampleObject(new PlacementBlockAction(Mode.SET, Blocks.STONE.defaultBlockState()));
        }
    }

    public enum Mode implements StringRepresentable {
        ADD, SET, FALLING;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}