package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RunCommandBlockAction extends BlockAction implements CommandSource {

    public static final MapCodec<RunCommandBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParsedCommands.CODEC.optionalFieldOf("command", ParsedCommands.EMPTY).forGetter(a -> a.command)
    ).apply(instance, RunCommandBlockAction::new));

    private final ParsedCommands command;

    public RunCommandBlockAction(ParsedCommands command) {
        this.command = command;
    }

    @Override
    public boolean run(BlockActionContext context) {
        if (!(context.level() instanceof ServerLevel serverLevel)) return false;

        BlockPos pos = context.pos();
        BlockState blockState = serverLevel.getBlockState(pos);
        String translationKey = blockState.getBlock().getDescriptionId();

        MinecraftServer server = Objects.requireNonNull(serverLevel.getServer());

        CommandSourceStack commandSource = new CommandSourceStack(
                CommandSource.NULL,
                pos.getCenter(),
                Vec2.ZERO,
                serverLevel,
                MiscUtil.applyPermissionLevel(PermissionLevel.GAMEMASTERS),
                translationKey,
                Component.translatable(translationKey),
                server,
                null
        );

        server.getFunctions().execute(command.getCommandFunction(server), commandSource.withSuppressedOutput());
        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.RUN_COMMAND.get();
    }

    @Override
    public void sendSystemMessage(Component component) {}

    @Override
    public boolean acceptsSuccess() {
        return false;
    }

    @Override
    public boolean acceptsFailure() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    public static class Serializer extends BlockActionSerializer<RunCommandBlockAction> {

        @Override
        public MapCodec<RunCommandBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, RunCommandBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Run Command")
                    .setDescription("Runs commands at the block's position.")
                    .addOptional("command", TYPE_STRING_ARRAY, "The commands to run.")
                    .addExampleObject(new RunCommandBlockAction(new ParsedCommands(Collections.singletonList("say Hello from a block!"))))
                    .addExampleObject(new RunCommandBlockAction(new ParsedCommands(List.of("say Command 1!", "say Command 2!"))));
        }
    }

}