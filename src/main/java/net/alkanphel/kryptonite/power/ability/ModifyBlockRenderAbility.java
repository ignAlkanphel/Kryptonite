package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.network.p2c.S2CModifyBlockRender;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyBlockRenderAbility extends Ability {

    public static final MapCodec<ModifyBlockRenderAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(ab -> ab.blockConditions),
            BlockState.CODEC.fieldOf("block_state").forGetter(ab -> ab.blockState),
            Mode.CODEC.optionalFieldOf("chunk_mode", Mode.REFRESH_VISIBLE).forGetter(ab -> ab.mode),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyBlockRenderAbility::new));

    public final List<BlockCondition> blockConditions;
    public final BlockState blockState;
    public final Mode mode;

    public ModifyBlockRenderAbility(List<BlockCondition> blockConditions, BlockState blockState, Mode mode, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockConditions = blockConditions;
        this.blockState = blockState;
        this.mode = mode;
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new S2CModifyBlockRender(this.mode));
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new S2CModifyBlockRender(Mode.REFRESH_ALL));
        }
    }

    public boolean doesApply(Level level, BlockPos pos) {
        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, level, pos);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_BLOCK_RENDER.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyBlockRenderAbility> {

        @Override
        public MapCodec<ModifyBlockRenderAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyBlockRenderAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Modifies how a block would look like to the player that has this ability.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only blocks fulfilling these conditions will have their rendering modified.")
                    .add("block_state", TYPE_BLOCK_STATE, "The block state to render in place of the specified block.")
                    .addOptional("chunk_mode", SettingType.enumList(Mode.values()), "How the block visuals are updated on the client. Mode \"refresh_all\" reloads all chunks again. Mode \"refresh_visible\" updates currently visible chunks in sections & is less performance taxing due to that. Regardless of which, \"refresh_all\" activates on the last tick of the ability.", Mode.REFRESH_VISIBLE)
                    .addExampleObject(new ModifyBlockRenderAbility(List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("sponge")))))), Blocks.DIAMOND_BLOCK.defaultBlockState(), Mode.REFRESH_VISIBLE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyBlockRenderAbility(List.of(), Blocks.DIAMOND_BLOCK.defaultBlockState(), Mode.REFRESH_ALL, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum Mode implements StringRepresentable {
        REFRESH_ALL("refresh_all"),
        REFRESH_VISIBLE("refresh_visible");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class Cache {
        private static volatile List<ModifyBlockRenderAbility> active = List.of();

        public static void set(List<ModifyBlockRenderAbility> abilities) {
            active = List.copyOf(abilities);
        }

        public static List<ModifyBlockRenderAbility> get() {
            return active;
        }
    }

}