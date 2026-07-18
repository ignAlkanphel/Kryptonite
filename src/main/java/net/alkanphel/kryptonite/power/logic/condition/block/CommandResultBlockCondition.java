package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.NumberComparator;

import java.util.concurrent.atomic.AtomicInteger;

public record CommandResultBlockCondition(String command, NumberComparator comparator, int compareTo) implements BlockCondition {

    public static final MapCodec<CommandResultBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("command").forGetter(CommandResultBlockCondition::command),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(CommandResultBlockCondition::comparator),
            Codec.INT.fieldOf("compare_to").forGetter(CommandResultBlockCondition::compareTo)
    ).apply(instance, CommandResultBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CommandResultBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CommandResultBlockCondition::command,
            NumberComparator.STREAM_CODEC, CommandResultBlockCondition::comparator,
            ByteBufCodecs.INT, CommandResultBlockCondition::compareTo,
            CommandResultBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        BlockState blockState = context.blockState();
        BlockPos pos = context.pos();

        if (!(context.level() instanceof ServerLevel serverLevel)) return false;

        MinecraftServer server = serverLevel.getServer();
        AtomicInteger result = new AtomicInteger();

        String blockName = blockState.getBlock().toString();

        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                serverLevel,
                MiscUtil.applyPermissionLevel(PermissionLevel.GAMEMASTERS),
                blockName,
                Component.literal(blockName),
                server,
                null
        );

        source = source.withCallback((success, returnValue) -> result.set(returnValue));
        server.getCommands().performPrefixedCommand(source, command);

        return comparator.compare(result.get(), compareTo);
    }

    @Override
    public BlockConditionSerializer<CommandResultBlockCondition> getSerializer() {
        return BlockConditionSerializers.COMMAND_RESULT.get();
    }

    public static class Serializer extends BlockConditionSerializer<CommandResultBlockCondition> {

        @Override
        public MapCodec<CommandResultBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, CommandResultBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Command Result")
                    .setDescription("Compares the result of the specified command to the specified value at the position of the block. This operates server-side.")
                    .add("command", TYPE_STRING, "The commands to run.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .addOptional("compare_to", TYPE_INT, "The value that is being compared against")
                    .addExampleObject(new CommandResultBlockCondition("execute align xyz if entity @e[dy=0,dx=0,dz=0]", NumberComparator.GREATER_OR_EQUAL, 1));
        }
    }

}