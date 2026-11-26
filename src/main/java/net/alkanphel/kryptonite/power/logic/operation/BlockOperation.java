package net.alkanphel.kryptonite.power.logic.operation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.context.BlockOperationContext;
import net.alkanphel.kryptonite.power.logic.context.BlockFilterContext;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.gamerule.PalladiumGameRules;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record BlockOperation(List<OperationType> operations) {

    public static final BlockOperation EMPTY = new BlockOperation(List.of());
    public static final Codec<BlockOperation> CODEC = ExtraCodecs.compactListCodec(OperationType.CODEC).xmap(BlockOperation::new, BlockOperation::operations);

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType operationType = OperationType.documentation(provider);
        return SettingType.combined(operationType, SettingType.array(operationType));
    }

    public boolean run(BlockOperationContext context) {
        boolean result = false;

        for (OperationType operation : operations) {
            result |= operation.run(context);
        }

        return result;
    }

    public boolean run(Level level, BlockPos pos) {
        return run(level, pos, Optional.empty());
    }

    public boolean run(Level level, BlockPos pos, Direction direction) {
        return run(level, pos, Optional.ofNullable(direction));
    }

    public boolean run(Level level, BlockPos pos, Optional<Direction> direction) {
        if (level instanceof ServerLevel serverLevel) {
            return run(new BlockOperationContext(serverLevel, pos, direction));
        }

        return false;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record OperationType(
            Optional<AreaOfEffectOperation> areaOfEffect,
            Optional<BlockState> blockAdd,
            Optional<BlockState> blockSet,
            Optional<BoneMealOperation> boneMeal,
            Optional<ExplodeOperation> explode,
            Optional<Boolean> fallingBlock,
            Optional<ModifyBlockStateOperation> modifyBlockState,
            Optional<OffsetOperation> offset,
            Optional<ParsedCommands> runCommand,
            Optional<SpawnEntityOperation> spawnEntity
    ) {

        private static final MapCodec<OperationType> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AreaOfEffectOperation.CODEC.optionalFieldOf("area_of_effect").forGetter(OperationType::areaOfEffect),
                BlockState.CODEC.optionalFieldOf("block_add").forGetter(OperationType::blockAdd),
                BlockState.CODEC.optionalFieldOf("block_set").forGetter(OperationType::blockSet),
                BoneMealOperation.CODEC.optionalFieldOf("bone_meal").forGetter(OperationType::boneMeal),
                ExplodeOperation.CODEC.optionalFieldOf("explode").forGetter(OperationType::explode),
                Codec.BOOL.optionalFieldOf("falling_block").forGetter(OperationType::fallingBlock),
                ModifyBlockStateOperation.CODEC.optionalFieldOf("modify_block_state").forGetter(OperationType::modifyBlockState),
                OffsetOperation.CODEC.optionalFieldOf("offset").forGetter(OperationType::offset),
                ParsedCommands.CODEC.optionalFieldOf("run_command").forGetter(OperationType::runCommand),
                SpawnEntityOperation.CODEC.optionalFieldOf("spawn_entity").forGetter(OperationType::spawnEntity)
        ).apply(instance, OperationType::new));

        public static final Codec<OperationType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("area_of_effect", AreaOfEffectOperation.documentation(provider), "Performs block operations on blocks within a radius.")
                    .addOptional("block_add", Documented.TYPE_BLOCK_STATE, "Adds/places a block at the operation position (offset by the direction of the operation).")
                    .addOptional("block_set", Documented.TYPE_BLOCK_STATE, "Sets/replaces a block at the operation position (offset by the direction of the operation).")
                    .addOptional("bone_meal", BoneMealOperation.documentation(provider), "Applies bone meal to the target block.")
                    .addOptional("explode", ExplodeOperation.documentation(provider), "Creates an explosion at the block's position.")
                    .addOptional("falling_block", Documented.TYPE_BOOLEAN, "Converts the block at the operation position into a falling block entity.")
                    .addOptional("modify_block_state", ModifyBlockStateOperation.documentation(provider), "Modifies a block state property at the operation position.")
                    .addOptional("offset", OffsetOperation.documentation(provider), "Performs block operations at an offset position relative to the original block.")
                    .addOptional("run_command", Documented.TYPE_STRING_ARRAY, "Runs commands at the block's position.")
                    .addOptional("spawn_entity", SpawnEntityOperation.documentation(provider), "Spawns entities at the block's position.")
                    .buildObject("Block Operation Types");
        }

        public boolean isEmpty() {
            return areaOfEffect.isEmpty()
                    && blockAdd.isEmpty()
                    && blockSet.isEmpty()
                    && boneMeal.isEmpty()
                    && explode.isEmpty()
                    && fallingBlock.isEmpty()
                    && modifyBlockState.isEmpty()
                    && offset.isEmpty()
                    && runCommand.isEmpty()
                    && spawnEntity.isEmpty();
        }

        public boolean run(BlockOperationContext context) {
            return runAreaOfEffect(context)
                    | runBlockAdd(context)
                    | runBlockSet(context)
                    | runBoneMeal(context)
                    | runExplode(context)
                    | runFallingBlock(context)
                    | runModifyBlockState(context)
                    | runOffset(context)
                    | runRunCommand(context)
                    | runSpawnEntity(context);
        }

        private boolean runAreaOfEffect(BlockOperationContext context) {
            return areaOfEffect.map(areaOfEffectOperation -> areaOfEffectOperation.run(context)).orElse(false);
        }

        private boolean runBlockAdd(BlockOperationContext context) {
            return blockAdd.map(blockState -> context.direction().map(direction -> {
                context.level().setBlockAndUpdate(context.pos().relative(direction), blockState);
                return true;
            }).orElse(false)).orElse(false);
        }

        private boolean runBlockSet(BlockOperationContext context) {
            return blockSet.map(blockState -> {
                context.level().setBlockAndUpdate(context.pos(), blockState);
                return true;
            }).orElse(false);
        }

        private boolean runBoneMeal(BlockOperationContext context) {
            return boneMeal.map(boneMealOperation -> boneMealOperation.run(context)).orElse(false);
        }

        private boolean runExplode(BlockOperationContext context) {
            return explode.map(explodeOperation -> explodeOperation.run(context)).orElse(false);
        }

        private boolean runFallingBlock(BlockOperationContext context) {
            return fallingBlock.filter(Boolean::booleanValue).map(enabled -> {
                ServerLevel level = context.level();
                BlockPos pos = context.pos();
                BlockState state = level.getBlockState(pos);

                if (state.isAir()) return false;

                FallingBlockEntity.fall(level, pos, state);
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                return true;
            }).orElse(false);
        }

        private boolean runModifyBlockState(BlockOperationContext context) {
            return modifyBlockState.map(operation -> operation.run(context)).orElse(false);
        }

        private boolean runOffset(BlockOperationContext context) {
            return offset.map(operation -> operation.run(context)).orElse(false);
        }

        private boolean runRunCommand(BlockOperationContext context) {
            return runCommand.map(command -> {
                ServerLevel serverLevel = context.level();
                BlockPos pos = context.pos();
                BlockState blockState = serverLevel.getBlockState(pos);
                String translationKey = blockState.getBlock().getDescriptionId();

                MinecraftServer server = Objects.requireNonNull(serverLevel.getServer());

                CommandSourceStack commandSource = new CommandSourceStack(
                        CommandSource.NULL,
                        pos.getCenter(),
                        Vec2.ZERO,
                        serverLevel,
                        PermissionSet.ALL_PERMISSIONS,
                        translationKey,
                        Component.translatable(translationKey),
                        server,
                        null
                );

                server.getFunctions().execute(command.getCommandFunction(server), commandSource.withSuppressedOutput().withMaximumPermission(PermissionSet.ALL_PERMISSIONS));
                return true;
            }).orElse(false);
        }

        private boolean runSpawnEntity(BlockOperationContext context) {
            return spawnEntity.map(operation -> operation.run(context)).orElse(false);
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record AreaOfEffectOperation(
            BlockOperation blockOperation,
            BlockFilter blockFilter,
            Shape shape,
            Value radius
    ) {

        private static final MapCodec<AreaOfEffectOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.lazyInitialized(() -> BlockOperation.CODEC).fieldOf("block_operations").forGetter(AreaOfEffectOperation::blockOperation),
                Codec.lazyInitialized(() -> BlockFilter.CODEC).optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(AreaOfEffectOperation::blockFilter),
                Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(AreaOfEffectOperation::shape),
                Value.NUMBER_CODEC.optionalFieldOf("radius", new StaticValue(16)).forGetter(AreaOfEffectOperation::radius)
        ).apply(instance, AreaOfEffectOperation::new));

        public static final Codec<AreaOfEffectOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("block_operations", SettingType.simple("Block Operations"), "Block operation(s) performed on the blocks in range.")
                    .addOptional("block_filters", SettingType.simple("Block Filters"), "Block filter(s) the blocks in range must pass for the operations to perform.")
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "The shape of the area.", Shape.CUBE)
                    .addOptional("radius", KryptoniteSettingType.intValueRange(0, Integer.MAX_VALUE), "The radius of the area.", 16)
                    .buildObject("Area Of Effect");
        }

        public boolean run(BlockOperationContext context) {
            var level = context.level();

            shape.getBlockPositions(context.pos(), radius.getAsInt(null))
                    .stream()
                    .filter(pos -> blockFilter.test(level, pos))
                    .forEach(pos -> blockOperation.run(level, pos, context.direction()));

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record BoneMealOperation(boolean showEffects) {

        private static final MapCodec<BoneMealOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("show_effects", true).forGetter(BoneMealOperation::showEffects)
        ).apply(instance, BoneMealOperation::new));

        public static final Codec<BoneMealOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("show_effects", Documented.TYPE_BOOLEAN, "If true, its particles/effects will be visible.", true)
                    .buildObject("Bone Meal");
        }

        public boolean run(BlockOperationContext context) {
            ServerLevel level = context.level();
            BlockPos pos = context.pos();

            ItemStack stack = ItemStack.EMPTY;

            if (BoneMealItem.applyBonemeal(stack, level, pos, null)) {
                boneMealEvent(level, pos);
                return true;
            }

            if (context.direction().isPresent()) {
                Direction direction = context.direction().get();

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
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record ExplodeOperation(
            BlockFilter destructible,
            BlockFilter indestructible,
            Level.ExplosionInteraction destructionType,
            BooleanValueHolder createFire,
            Value indestructibleResistance,
            Value power
    ) {

        private static final MapCodec<ExplodeOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockFilter.CODEC.optionalFieldOf("destructible", BlockFilter.EMPTY).forGetter(ExplodeOperation::destructible),
                BlockFilter.CODEC.optionalFieldOf("indestructible", BlockFilter.EMPTY).forGetter(ExplodeOperation::indestructible),
                Level.ExplosionInteraction.CODEC.optionalFieldOf("destruction_type", Level.ExplosionInteraction.BLOCK).forGetter(ExplodeOperation::destructionType),
                BooleanValueHolder.CODEC.optionalFieldOf("create_fire", BooleanValueHolder.FALSE).forGetter(ExplodeOperation::createFire),
                Value.NUMBER_CODEC.optionalFieldOf("indestructible_resistance", new StaticValue(10)).forGetter(ExplodeOperation::indestructibleResistance),
                Value.NUMBER_CODEC.fieldOf("power").forGetter(ExplodeOperation::power)
        ).apply(instance, ExplodeOperation::new));

        public static final Codec<ExplodeOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("destructible", BlockFilter.documentation(provider), "Block filter(s) the blocks in the explosion must pass to be destroyed.")
                    .addOptional("indestructible", BlockFilter.documentation(provider), "Block filter(s) the blocks in the explosion must pass to not be affected.")
                    .addOptional("destruction_type", KryptoniteDocumented.TYPE_EXPLOSION_INTERACTION, "How the explosion interacts with blocks.", Level.ExplosionInteraction.BLOCK)
                    .addOptional("create_fire", Documented.TYPE_BOOLEAN_VALUE, "If true, the explosion will create fire (e.g. ghast fireball).", false)
                    .addOptional("indestructible_resistance", KryptoniteSettingType.floatValueRange(0, Float.MAX_VALUE), "The explosion resistance value used for indestructible blocks.", 10.0F)
                    .add("power", KryptoniteSettingType.floatValueRange(0, Float.MAX_VALUE), "The strength/radius of the explosion.")
                    .buildObject("Explode");
        }

        public boolean run(BlockOperationContext context) {
            ServerLevel level = context.level();
            if (level.isClientSide()) return false;

            boolean griefing = PalladiumGameRules.allowsPowerGriefing(level);

            Predicate<BlockFilterContext> behaviorCondition = ctx -> false;

            if (!indestructible.isEmpty()) {
                behaviorCondition = indestructible::test;
            }

            if (!destructible.isEmpty()) {
                Predicate<BlockFilterContext> destructiblePredicate = destructible::test;
                behaviorCondition = MiscUtil.combineOr(destructiblePredicate.negate(), behaviorCondition);
            }

            MiscUtil.createExplosion(
                    level,
                    context.pos().getCenter(),
                    Math.max(0, power.getAsFloat(null)),
                    griefing && createFire.get(null),
                    griefing ? destructionType : Level.ExplosionInteraction.NONE,
                    MiscUtil.createExplosionDamageCalculator(behaviorCondition, Math.max(0, indestructibleResistance.getAsFloat(null)))
            );

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record ModifyBlockStateOperation(
            String property,
            ResourceOperation operation,
            Optional<Integer> change,
            Optional<Boolean> boolValue,
            Optional<String> enumValue,
            boolean cycle
    ) {

        private static final MapCodec<ModifyBlockStateOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(ModifyBlockStateOperation::property),
                ResourceOperation.CODEC.optionalFieldOf("operation", ResourceOperation.ADD).forGetter(ModifyBlockStateOperation::operation),
                Codec.INT.optionalFieldOf("change").forGetter(ModifyBlockStateOperation::change),
                Codec.BOOL.optionalFieldOf("value").forGetter(ModifyBlockStateOperation::boolValue),
                Codec.STRING.optionalFieldOf("enum").forGetter(ModifyBlockStateOperation::enumValue),
                Codec.BOOL.optionalFieldOf("cycle", false).forGetter(ModifyBlockStateOperation::cycle)
        ).apply(instance, ModifyBlockStateOperation::new));

        public static final Codec<ModifyBlockStateOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("property", Documented.TYPE_STRING, "The name of the block state property to modify. See: https://minecraft.wiki/w/Block_states#List_of_block_states")
                    .addOptional("operation", SettingType.enumList(ResourceOperation.values()), "Determines how the value specified in the \"change\" field is operated on the specified property.", ResourceOperation.ADD)
                    .addOptional("change", Documented.TYPE_INT, "If specified, the value to add, remove or set to/from the specified property if the specified property is an integer.")
                    .addOptional("value", Documented.TYPE_BOOLEAN, "If specified, the boolean to use as the new value for the specified property if the specified property is a boolean.")
                    .addOptional("enum", Documented.TYPE_STRING, "If specified, the string to use as the new value for the specified property if the specified property is a string.")
                    .addOptional("cycle", Documented.TYPE_BOOLEAN, "If true, changes the property to the next state in the cycle, ignoring all other optional fields. All property types can use this operation.", false)
                    .buildObject("Modify Block State");
        }

        public boolean run(BlockOperationContext context) {
            Level level = context.level();
            BlockPos pos = context.pos();

            BlockState blockState = level.getBlockState(pos);
            Property<?> blockProperty = blockState.getProperties()
                    .stream()
                    .filter(prop -> prop.getName().equals(property))
                    .findFirst()
                    .orElse(null);

            if (blockProperty == null) return false;

            if (cycle) {
                level.setBlockAndUpdate(pos, blockState.cycle(blockProperty));
                return true;
            }

            switch (blockProperty) {
                case EnumProperty<?> enumProp when enumValue.isPresent() && !enumValue.get().isEmpty() ->
                        setEnumProperty(enumProp, enumValue.get(), level, pos, blockState);
                case BooleanProperty boolProp when boolValue.isPresent() ->
                        level.setBlockAndUpdate(pos, blockState.setValue(boolProp, boolValue.get()));
                case IntegerProperty intProp when change.isPresent() -> {
                    int newValue = switch (operation) {
                        case ADD -> blockState.getValue(intProp) + change.get();
                        case SET -> change.get();
                    };

                    if (intProp.getPossibleValues().contains(newValue)) {
                        level.setBlockAndUpdate(pos, blockState.setValue(intProp, newValue));
                    }
                }
                default -> {
                    return false;
                }
            }

            return true;
        }

        private <T extends Enum<T> & StringRepresentable> void setEnumProperty(EnumProperty<T> property, String name, Level level, BlockPos pos, BlockState originalState) {
            property.getValue(name).ifPresentOrElse(
                    propValue -> level.setBlockAndUpdate(pos, originalState.setValue(property, propValue)),
                    () -> Kryptonite.LOGGER.warn("Couldn't set enum property \"{}\" of block at {} to \"{}\"! Expected value to be any of {}", property.getName(), pos.toShortString(), name, property.getPossibleValues().stream().map(StringRepresentable::getSerializedName).collect(Collectors.joining(", ")))
            );
        }

        public enum ResourceOperation implements StringRepresentable {
            ADD, SET;

            public static final Codec<ResourceOperation> CODEC = StringRepresentable.fromEnum(ResourceOperation::values);

            @Override
            public @NotNull String getSerializedName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record OffsetOperation(List<BlockOperation> blockOperations, Value x, Value y, Value z) {

        private static final MapCodec<OffsetOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.lazyInitialized(() -> BlockOperation.CODEC).listOf().fieldOf("block_operations").forGetter(OffsetOperation::blockOperations),
                Value.NUMBER_CODEC.optionalFieldOf("x", new StaticValue(0)).forGetter(OffsetOperation::x),
                Value.NUMBER_CODEC.optionalFieldOf("y", new StaticValue(0)).forGetter(OffsetOperation::y),
                Value.NUMBER_CODEC.optionalFieldOf("z", new StaticValue(0)).forGetter(OffsetOperation::z)
        ).apply(instance, OffsetOperation::new));

        public static final Codec<OffsetOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("block_operations", SettingType.array(SettingType.simple("Block Operations")), "Block operation(s) performed at the offset position.")
                    .addOptional("x", KryptoniteSettingType.intValueRange(Integer.MIN_VALUE, Integer.MAX_VALUE), "How much to offset the position on the X axis.", 0)
                    .addOptional("y", KryptoniteSettingType.intValueRange(Integer.MIN_VALUE, Integer.MAX_VALUE), "How much to offset the position on the Y axis.", 0)
                    .addOptional("z", KryptoniteSettingType.intValueRange(Integer.MIN_VALUE, Integer.MAX_VALUE), "How much to offset the position on the Z axis.", 0)
                    .buildObject("Offset");
        }

        public boolean run(BlockOperationContext context) {
            BlockPos offsetPos = context.pos().offset(x.getAsInt(null), y.getAsInt(null), z.getAsInt(null));

            for (BlockOperation blockOperation : blockOperations) {
                blockOperation.run(context.level(), offsetPos, context.direction());
            }

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record SpawnEntityOperation(
            HolderSet<EntityType<?>> entityTypes,
            List<Action> entityActions,
            CompoundTag entityNbt
    ) {

        private static final MapCodec<SpawnEntityOperation> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(SpawnEntityOperation::entityTypes),
                Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(SpawnEntityOperation::entityActions),
                CompoundTag.CODEC.optionalFieldOf("entity_nbt", new CompoundTag()).forGetter(SpawnEntityOperation::entityNbt)
        ).apply(instance, SpawnEntityOperation::new));

        public static final Codec<SpawnEntityOperation> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("entity_types", Documented.TYPE_ENTITY_TYPE_HOLDER_SET, "The entity to spawn of the specified Entity IDs or tags.")
                    .addOptional("entity_actions", Documented.TYPE_ACTION_LIST, "Action(s) performed on the spawned entity.")
                    .addOptional("entity_nbt", Documented.TYPE_NBT, "The NBT data applied to the spawned entity.")
                    .buildObject("Spawn Entity");
        }

        public static CompoundTag addExampleNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString(Entity.TAG_CUSTOM_NAME, "Aw, Man");
            tag.putInt(LivingEntity.TAG_HEALTH, 20);
            return tag;
        }

        public boolean run(BlockOperationContext context) {
            var level = context.level();
            for (var holder : entityTypes) {
                MiscUtil.getEntityWithPassengers(level, holder.value(), entityNbt, context.pos().getBottomCenter(), Optional.empty(), Optional.empty()).ifPresent(entity -> {
                    level.addFreshEntityWithPassengers(entity);
                    if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
                });
            }

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static <T> Optional<T> none() {
        return Optional.empty();
    }

    private static BlockOperation single(OperationType type) {
        return new BlockOperation(List.of(type));
    }

    public static BlockOperation of(OperationType... operations) {
        return new BlockOperation(List.of(operations));
    }

    public static BlockOperation ofAreaOfEffect(AreaOfEffectOperation areaOfEffect) {
        return single(new OperationType(Optional.of(areaOfEffect), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockOperation ofBlockAdd(BlockState block) {
        return single(new OperationType(none(), Optional.of(block), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockOperation ofBlockSet(BlockState block) {
        return single(new OperationType(none(), none(), Optional.of(block), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockOperation ofBoneMeal(boolean showEffects) {
        return single(new OperationType(none(), none(), none(), Optional.of(new BoneMealOperation(showEffects)), none(), none(), none(), none(), none(), none()));
    }

    public static BlockOperation ofExplode(ExplodeOperation explode) {
        return single(new OperationType(none(), none(), none(), none(), Optional.of(explode), none(), none(), none(), none(), none()));
    }

    public static BlockOperation ofFallingBlock(boolean fallingBlock) {
        return single(new OperationType(none(), none(), none(), none(), none(), Optional.of(fallingBlock), none(), none(), none(), none()));
    }

    public static BlockOperation ofModifyBlockState(ModifyBlockStateOperation modifyBlockState) {
        return single(new OperationType(none(), none(), none(), none(), none(), none(), Optional.of(modifyBlockState), none(), none(), none()));
    }

    public static BlockOperation ofOffset(OffsetOperation offset) {
        return single(new OperationType(none(), none(), none(), none(), none(), none(), none(), Optional.of(offset), none(), none()));
    }

    public static BlockOperation ofRunCommand(ParsedCommands runCommand) {
        return single(new OperationType(none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(runCommand), none()));
    }

    public static BlockOperation ofSpawnEntity(SpawnEntityOperation spawnEntity) {
        return single(new OperationType(none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(spawnEntity)));
    }

}