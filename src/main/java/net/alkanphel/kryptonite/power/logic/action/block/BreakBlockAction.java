package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public class BreakBlockAction extends BlockAction {

    public static final MapCodec<BreakBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("drop_loot", true).forGetter(a -> a.dropLoot),
            Codec.BOOL.optionalFieldOf("particles", true).forGetter(a -> a.particles)
    ).apply(instance, BreakBlockAction::new));

    private final boolean dropLoot;
    private final boolean particles;

    public BreakBlockAction(boolean dropLoot, boolean particles) {
        this.dropLoot = dropLoot;
        this.particles = particles;
    }

    @Override
    public boolean execute(BlockActionContext context) {
        Level level = context.level();
        if (level.isClientSide()) return false;

        BlockPos pos = context.pos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        if (this.particles) {
            return level.destroyBlock(pos, this.dropLoot);
        }

        if (this.dropLoot) {
            BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(state, level, pos, blockEntity);
        }

        FluidState fluid = level.getFluidState(pos);
        if (level.setBlock(pos, fluid.createLegacyBlock(), Block.UPDATE_ALL)) {
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
            return true;
        }

        return false;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.BREAK.get();
    }

    public static class Serializer extends BlockActionSerializer<BreakBlockAction> {

        @Override
        public MapCodec<BreakBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, BreakBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Break")
                    .setDescription("Breaks the block at the action position.")
                    .addOptional("drop_loot", TYPE_BOOLEAN, "If the block drops its loot.", true)
                    .addOptional("particles", TYPE_BOOLEAN, "If the break particles & sound emit.", true)
                    .addExampleObject(new BreakBlockAction(true, true));
        }
    }

}