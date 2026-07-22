package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.CrouchingCondition;
import net.threetag.palladium.logic.condition.FalseCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class IntangibilityAbility extends Ability {

    public static final MapCodec<IntangibilityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("vertical_conditions", FalseCondition.INSTANCE).forGetter(ab -> ab.verticalConditions),
            BlockCondition.CODEC.optionalFieldOf("block_conditions").forGetter(ab -> ab.blockConditions),
            Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(ab -> ab.blacklist),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, IntangibilityAbility::new));

    private final Condition verticalConditions;
    private final Optional<BlockCondition> blockConditions;
    private final boolean blacklist;

    public IntangibilityAbility(Condition verticalConditions, Optional<BlockCondition> blockConditions, boolean blacklist, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.verticalConditions = verticalConditions;
        this.blockConditions = blockConditions;
        this.blacklist = blacklist;
    }

    public boolean doesApply(Entity entity, BlockPos pos) {
        return blockConditions
                .map(condition -> blacklist != condition.test(entity.level(), pos))
                .orElse(true);
    }

    public boolean shouldPhase(LivingEntity entity, AbilityInstance<IntangibilityAbility> instance, VoxelShape shape, BlockPos pos) {
        return (entity.getY() < pos.getY() + shape.max(Direction.Axis.Y) - (entity.onGround() ? 8.05D / 16.0 : 0.0015)
                || shouldPhaseDown(entity, instance)) && doesApply(entity, pos);
    }

    public boolean shouldPhaseDown(LivingEntity entity, AbilityInstance<IntangibilityAbility> instance) {
        return verticalConditions.test(DataContext.forAbility(entity, instance));
    }

    public static BlockState getInWallBlockState(LivingEntity playerEntity) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double) (((float) ((i) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double) (((float) ((i >> 2) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.level().getBlockState(mutable);
            if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(playerEntity.level(), mutable)) {
                return blockState;
            }
        }

        return null;
    }

    @Override
    public AbilitySerializer<IntangibilityAbility> getSerializer() {
        return KryptoniteAbilitySerializers.INTANGIBILITY.get();
    }

    public static class Serializer extends AbilitySerializer<IntangibilityAbility> {

        @Override
        public MapCodec<IntangibilityAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, IntangibilityAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes the entity intangible to certain blocks.")
                    .addOptional("vertical_conditions", TYPE_CONDITION_LIST, "If specified, the entity will only phase downward (vertically) through blocks if these conditions are fulfilled.", FalseCondition.INSTANCE)
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only blocks fulfilling these conditions are phased through. If omitted, all blocks are phased through.")
                    .addOptional("blacklist", TYPE_BOOLEAN, "If true, the \"block_conditions\" field will instead decide which blocks the entity can NOT phase through.", false)
                    .addExampleObject(new IntangibilityAbility(FalseCondition.INSTANCE, Optional.empty(), false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new IntangibilityAbility(CrouchingCondition.INSTANCE, Optional.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("tripwire")))))), false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}