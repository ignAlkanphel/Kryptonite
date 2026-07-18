package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Optional;

public class SpawnEntityBlockAction extends BlockAction {

    public static final MapCodec<SpawnEntityBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_type").forGetter(a -> a.entityType),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            CompoundTag.CODEC.optionalFieldOf("entity_nbt", new CompoundTag()).forGetter(a -> a.tag)
    ).apply(instance, SpawnEntityBlockAction::new));

    private final HolderSet<EntityType<?>> entityType;
    private final List<Action> entityActions;
    private final CompoundTag tag;

    public SpawnEntityBlockAction(HolderSet<EntityType<?>> entityType, List<Action> entityActions, CompoundTag tag) {
        this.entityType = entityType;
        this.entityActions = entityActions;
        this.tag = tag;
    }

    @Override
    public boolean run(BlockActionContext context) {
        var level = context.level();

        for (var holder : entityType) {
            MiscUtil.getEntityWithPassengers(level, holder.value(), tag, context.pos().getBottomCenter(), Optional.empty(), Optional.empty()).ifPresent(entity -> {
                level.addFreshEntityWithPassengers(entity);
                if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
            });
        }

        return true;
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.SPAWN_ENTITY.get();
    }

    public static class Serializer extends BlockActionSerializer<SpawnEntityBlockAction> {

        @Override
        public MapCodec<SpawnEntityBlockAction> codec() {
            return CODEC;
        }

        public static CompoundTag addExampleNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString(Entity.TAG_CUSTOM_NAME, "Aw, Man");
            tag.putInt(LivingEntity.TAG_HEALTH, 20);
            return tag;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, SpawnEntityBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Spawn Entity")
                    .setDescription("Spawns an entity at the block's position.")
                    .add("entity_type", TYPE_ENTITY_TYPE_HOLDER_SET, "The entity to spawn of the specified Entity IDs or tags.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "If specified, these actions will run on the spawned entity.")
                    .addOptional("entity_nbt", TYPE_NBT, "If specified, this NBT data will apply to the spawned entity.")
                    .addExampleObject(new SpawnEntityBlockAction(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))), List.of(new RunCommandAction(new ParsedCommands("effect give @s glowing 4 0 true"))), addExampleNbt()));
        }
    }

}