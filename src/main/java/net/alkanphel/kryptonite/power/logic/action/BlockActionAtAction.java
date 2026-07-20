package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.BoneMealBlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.List;
import java.util.Optional;

public class BlockActionAtAction extends Action {

    public static final MapCodec<BlockActionAtAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockAction.LIST_CODEC.fieldOf("block_actions").forGetter(a -> a.actions)
    ).apply(instance, BlockActionAtAction::new));

    private final List<BlockAction> actions;

    public BlockActionAtAction(List<BlockAction> actions) {
        this.actions = actions;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.level() instanceof ServerLevel level)) return false;

        BlockPos pos = BlockPos.containing(entity.position());
        BlockAction.runList(actions, level, pos, Optional.empty());
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.BLOCK_ACTION_AT.get();
    }

    public static class Serializer extends ActionSerializer<BlockActionAtAction> {

        @Override
        public MapCodec<BlockActionAtAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, BlockActionAtAction> builder, HolderLookup.Provider provider) {
            builder.setName("Block Action At")
                    .setDescription("Runs block actions at the entity's position.")
                    .add("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "The block actions to run.")
                    .addExampleObject(new BlockActionAtAction(List.of(new BoneMealBlockAction(false))));
        }
    }

}