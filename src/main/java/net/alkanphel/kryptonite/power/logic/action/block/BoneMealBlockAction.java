package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Optional;

public class BoneMealBlockAction extends BlockAction {

    public static final MapCodec<BoneMealBlockAction> CODEC = MapCodec.unit(new BoneMealBlockAction(true));

    private final boolean showEffects;

    public BoneMealBlockAction(boolean showEffects) {
        this.showEffects = showEffects;
    }

    @Override
    public boolean run(BlockActionContext context) {
        ServerLevel level = context.level();
        BlockPos pos = context.pos();

        Optional<Direction> directionOpt = context.direction();
        ItemStack stack = ItemStack.EMPTY;

        if (BoneMealItem.growCrop(stack, level, pos)) {
            boneMealEvent(level, pos);
            return true;
        }

        if (directionOpt.isPresent()) {
            Direction direction = directionOpt.get();

            BlockState state = level.getBlockState(pos);
            BlockPos offsetPos = pos.relative(direction);

            if (state.isFaceSturdy(level, pos, direction) && BoneMealItem.growWaterPlant(stack, level, offsetPos, direction)) {
                boneMealEvent(level, offsetPos);
                return true;
            }
        }

        return false;
    }

    private void boneMealEvent(ServerLevel level, BlockPos pos) {
        if (showEffects && !level.isClientSide()) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
        }
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.BONE_MEAL.get();
    }

    public static class Serializer extends BlockActionSerializer<BoneMealBlockAction> {

        @Override
        public MapCodec<BoneMealBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, BoneMealBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Bone Meal")
                    .setDescription("Applies bone meal to the target block as if a dispenser or a player used a Bone Meal item to it.")
                    .addOptional("show_effects", TYPE_BOOLEAN, "If particles and effects should be shown when applying.", true)
                    .addExampleObject(new BoneMealBlockAction(true));
        }
    }

}