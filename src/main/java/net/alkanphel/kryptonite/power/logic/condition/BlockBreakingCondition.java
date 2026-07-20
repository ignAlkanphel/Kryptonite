package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.mixin.common.ServerPlayerGameModeAccessor;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;
import java.util.Optional;

public record BlockBreakingCondition(List<BlockCondition> blockConditions, Optional<Value> usingCorrectTool) implements Condition {

    public static final MapCodec<BlockBreakingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(BlockBreakingCondition::blockConditions),
            Value.CODEC.optionalFieldOf("using_correct_tool").forGetter(BlockBreakingCondition::usingCorrectTool)
    ).apply(instance, BlockBreakingCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockBreakingCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.LIST_CODEC), BlockBreakingCondition::blockConditions,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), BlockBreakingCondition::usingCorrectTool,
            BlockBreakingCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        BlockPos breakingPos = getBreakingPos(player);
        if (breakingPos == null) return false;

        if (usingCorrectTool.isPresent()) {
            boolean correctTool = player.hasCorrectToolForDrops(player.level().getBlockState(breakingPos), player.level(), breakingPos);

            if (correctTool != usingCorrectTool.get().getAsBoolean(context)) return false;
        }

        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, player.level(), breakingPos);
    }

    private static BlockPos getBreakingPos(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayerGameModeAccessor accessor = (ServerPlayerGameModeAccessor) serverPlayer.gameMode;
            return accessor.kryptonite$isDestroyingBlock() ? accessor.kryptonite$getDestroyPos() : null;
        }

        return Kryptonite.PROXY.getBreakingBlockPos(player);
    }

    @Override
    public ConditionSerializer<BlockBreakingCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_BREAKING.get();
    }

    public static class Serializer extends ConditionSerializer<BlockBreakingCondition> {

        @Override
        public MapCodec<BlockBreakingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockBreakingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Breaking")
                    .setDescription("Checks if the player is currently breaking a block.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, these conditions must be fulfilled for the block that is breaking.")
                    .addOptional("using_correct_tool", TYPE_VALUE, "If omitted, the tool is ignored. True requires using the correct tool to harvest, while false requires the incorrect one.", false)
                    .addExampleObject(new BlockBreakingCondition(List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("gold_block")))))), Optional.of(new StaticValue(false))))
                    .addExampleObject(new BlockBreakingCondition(List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("diamond_block")))))), Optional.of(new StaticValue(true))));
        }
    }

}