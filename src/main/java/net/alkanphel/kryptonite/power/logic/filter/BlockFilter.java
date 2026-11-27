package net.alkanphel.kryptonite.power.logic.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.BlockLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.BlockFilterContext;
import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.alkanphel.kryptonite.util.apoli.ability.DistanceFromCoordinates;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import net.threetag.palladium.util.PalladiumHolderSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public record BlockFilter(Operator operator, List<FilterType> filters) {

    public static final BlockFilter EMPTY = new BlockFilter(Operator.AND, List.of());

    private static final MapCodec<BlockFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Operator.CODEC.optionalFieldOf("operator", Operator.AND).forGetter(BlockFilter::operator),
            ExtraCodecs.compactListCodec(FilterType.CODEC).fieldOf("filters").forGetter(BlockFilter::filters)
    ).apply(instance, BlockFilter::new));

    public static final Codec<BlockFilter> CODEC = MAP_CODEC.codec();

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType filterType = FilterType.documentation(provider);
        return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                .addOptional("operator", SettingType.enumList(Operator.values()), "How the filters are combined: \"and\" needs all to pass, \"or\" needs at least one, \"not\" needs none.", Operator.AND)
                .add("filters", SettingType.combined(filterType, SettingType.array(filterType)), "Block filter type(s) that must pass.")
                .buildObject("Block Filters");
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public boolean test(BlockFilterContext context) {
        return operator.test(filters, context);
    }

    public boolean test(SavedBlockPosition savedBlock) {
        return test(new BlockFilterContext(savedBlock));
    }

    public boolean test(Level level, BlockPos pos) {
        return test(new BlockFilterContext(level, pos));
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FilterType(
            Optional<AdjacentFilter> adjacent,
            Optional<Boolean> attachable,
            Optional<PalladiumHolderSet<Block>> block,
            Optional<Boolean> blockEntity,
            Optional<InstanceofType> instanceofBlock,
            Optional<BlockStateFilter> blockState,
            Optional<Boolean> canSeeSky,
            Optional<CommandResultFilter> commandResult,
            Optional<DestroySpeedFilter> destroySpeed,
            Optional<DistanceFromCoordinatesFilter> distanceFromCoordinates,
            Optional<ExplosionResistanceFilter> explosionResistance,
            Optional<PalladiumHolderSet<Fluid>> fluidType,
            Optional<FrictionFilter> friction,
            Optional<HeightFilter> height,
            Optional<Boolean> inRain,
            Optional<Boolean> inSnow,
            Optional<Boolean> inThunder,
            Optional<Boolean> lightBlocking,
            Optional<LightLevelFilter> lightLevel,
            Optional<Boolean> movementBlocking,
            Optional<CompoundTag> nbt,
            Optional<OffsetFilter> offset,
            Optional<Boolean> replaceable,
            Optional<Boolean> waterLoggable
    ) {

        private static final MapCodec<Optional<Boolean>> IN_THUNDER_CODEC = Codec.BOOL.optionalFieldOf("in_thunder");
        private static final MapCodec<Optional<Boolean>> LIGHT_BLOCKING_CODEC = Codec.BOOL.optionalFieldOf("light_blocking");
        private static final MapCodec<Optional<LightLevelFilter>> LIGHT_LEVEL_CODEC = LightLevelFilter.CODEC.optionalFieldOf("light_level");
        private static final MapCodec<Optional<Boolean>> MOVEMENT_BLOCKING_CODEC = Codec.BOOL.optionalFieldOf("movement_blocking");
        private static final MapCodec<Optional<CompoundTag>> NBT_CODEC = CompoundTag.CODEC.optionalFieldOf("nbt");
        private static final MapCodec<Optional<OffsetFilter>> OFFSET_CODEC = OffsetFilter.CODEC.optionalFieldOf("offset");
        private static final MapCodec<Optional<Boolean>> REPLACEABLE_CODEC = Codec.BOOL.optionalFieldOf("replaceable");
        private static final MapCodec<Optional<Boolean>> WATER_LOGGABLE_CODEC = Codec.BOOL.optionalFieldOf("water_loggable");

        private static final MapCodec<FilterType> MAP_CODEC = RecordCodecBuilder.<FilterType>mapCodec(instance -> instance.group(
                AdjacentFilter.CODEC.optionalFieldOf("adjacent").forGetter(FilterType::adjacent),
                Codec.BOOL.optionalFieldOf("attachable").forGetter(FilterType::attachable),
                PalladiumHolderSet.codec(Registries.BLOCK).optionalFieldOf("block").forGetter(FilterType::block),
                Codec.BOOL.optionalFieldOf("block_entity").forGetter(FilterType::blockEntity),
                InstanceofType.CODEC.optionalFieldOf("instanceof").forGetter(FilterType::instanceofBlock),
                BlockStateFilter.CODEC.optionalFieldOf("block_state").forGetter(FilterType::blockState),
                Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(FilterType::canSeeSky),
                CommandResultFilter.CODEC.optionalFieldOf("command_result").forGetter(FilterType::commandResult),
                DestroySpeedFilter.CODEC.optionalFieldOf("destroy_speed").forGetter(FilterType::destroySpeed),
                DistanceFromCoordinatesFilter.CODEC.optionalFieldOf("distance_from_coordinates").forGetter(FilterType::distanceFromCoordinates),
                ExplosionResistanceFilter.CODEC.optionalFieldOf("explosion_resistance").forGetter(FilterType::explosionResistance),
                PalladiumHolderSet.codec(Registries.FLUID).optionalFieldOf("fluid_type").forGetter(FilterType::fluidType),
                FrictionFilter.CODEC.optionalFieldOf("friction").forGetter(FilterType::friction),
                HeightFilter.CODEC.optionalFieldOf("height").forGetter(FilterType::height),
                Codec.BOOL.optionalFieldOf("in_rain").forGetter(FilterType::inRain),
                Codec.BOOL.optionalFieldOf("in_snow").forGetter(FilterType::inSnow)
        ).apply(instance, (adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow) ->
                new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
        ).dependent(IN_THUNDER_CODEC, filter -> Pair.of(filter.inThunder(), IN_THUNDER_CODEC), FilterType::withInThunder
        ).dependent(LIGHT_BLOCKING_CODEC, filter -> Pair.of(filter.lightBlocking(), LIGHT_BLOCKING_CODEC), FilterType::withLightBlocking
        ).dependent(LIGHT_LEVEL_CODEC, filter -> Pair.of(filter.lightLevel(), LIGHT_LEVEL_CODEC), FilterType::withLightLevel
        ).dependent(MOVEMENT_BLOCKING_CODEC, filter -> Pair.of(filter.movementBlocking(), MOVEMENT_BLOCKING_CODEC), FilterType::withMovementBlocking
        ).dependent(NBT_CODEC, filter -> Pair.of(filter.nbt(), NBT_CODEC), FilterType::withNbt
        ).dependent(OFFSET_CODEC, filter -> Pair.of(filter.offset(), OFFSET_CODEC), FilterType::withOffset
        ).dependent(REPLACEABLE_CODEC, filter -> Pair.of(filter.replaceable(), REPLACEABLE_CODEC), FilterType::withReplaceable
        ).dependent(WATER_LOGGABLE_CODEC, filter -> Pair.of(filter.waterLoggable(), WATER_LOGGABLE_CODEC), FilterType::withWaterLoggable);

        private FilterType withInThunder(Optional<Boolean> inThunder) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withLightBlocking(Optional<Boolean> lightBlocking) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withLightLevel(Optional<LightLevelFilter> lightLevel) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withMovementBlocking(Optional<Boolean> movementBlocking) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withNbt(Optional<CompoundTag> nbt) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withOffset(Optional<OffsetFilter> offset) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withReplaceable(Optional<Boolean> replaceable) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        private FilterType withWaterLoggable(Optional<Boolean> waterLoggable) {
            return new FilterType(adjacent, attachable, block, blockEntity, instanceofBlock, blockState, canSeeSky, commandResult, destroySpeed, distanceFromCoordinates, explosionResistance, fluidType, friction, height, inRain, inSnow, inThunder, lightBlocking, lightLevel, movementBlocking, nbt, offset, replaceable, waterLoggable);
        }

        public static final Codec<FilterType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("adjacent", AdjacentFilter.documentation(provider), "Checks the number of adjacent blocks passing the specified filter.")
                    .addOptional("attachable", Documented.TYPE_BOOLEAN, "Checks if the block has a sturdy supporting face adjacent to it.")
                    .addOptional("block", KryptoniteDocumented.TYPE_BLOCK_TYPE_HOLDER_SET, "Checks if the block is one of these block IDs or tags.")
                    .addOptional("block_entity", Documented.TYPE_BOOLEAN, "Checks if the block is a block entity.")
                    .addOptional("instanceof", SettingType.enumList(InstanceofType.values()), "Checks what \"instanceof\" type the block is.")
                    .addOptional("block_state", BlockStateFilter.documentation(), "Checks a block state property.")
                    .addOptional("can_see_sky", Documented.TYPE_BOOLEAN, "Checks if the block can see the sky.")
                    .addOptional("command_result", CommandResultFilter.documentation(provider), "Runs a command at the block's position and compares its result.")
                    .addOptional("destroy_speed", DestroySpeedFilter.documentation(), "Checks and compares the destroy speed of the block.")
                    .addOptional("distance_from_coordinates", DistanceFromCoordinatesFilter.documentation(provider), "Checks and compares the distance of the block's current position to the specified coordinates.")
                    .addOptional("explosion_resistance", ExplosionResistanceFilter.documentation(), "Checks and compares the explosion resistance of the block.")
                    .addOptional("fluid_type", FluidTypeFilter.documentation(provider), "Checks if the fluid at the block position is one of these fluid IDs or tags.")
                    .addOptional("friction", FrictionFilter.documentation(), "Checks and compares the friction of the block.")
                    .addOptional("height", HeightFilter.documentation(), "Checks and compares the y-axis position of the block.")
                    .addOptional("in_rain", Documented.TYPE_BOOLEAN, "Checks if it's raining above the block.")
                    .addOptional("in_snow", Documented.TYPE_BOOLEAN, "Checks if it's snowing above the block.")
                    .addOptional("in_thunder", Documented.TYPE_BOOLEAN, "Checks if it's thundering above the block.")
                    .addOptional("light_blocking", Documented.TYPE_BOOLEAN, "Checks if the block blocks light from passing through.")
                    .addOptional("light_level", LightLevelFilter.documentation(), "Checks and compares the light level at the block's position.")
                    .addOptional("movement_blocking", Documented.TYPE_BOOLEAN, "Checks if the block blocks movement and has a non-empty collision shape.")
                    .addOptional("nbt", Documented.TYPE_NBT, "Checks the NBT data of the block entity.", addExampleNbt())
                    .addOptional("offset", OffsetFilter.documentation(provider), "Checks a block filter at an offset position.")
                    .addOptional("replaceable", Documented.TYPE_BOOLEAN, "Checks if the block can be replaced with another.")
                    .addOptional("water_loggable", Documented.TYPE_BOOLEAN, "Checks if the block can be waterlogged.")
                    .buildObject("Block Filter Types");
        }

        public boolean isEmpty() {
            return adjacent.isEmpty()
                    && attachable.isEmpty()
                    && block.isEmpty()
                    && blockEntity.isEmpty()
                    && instanceofBlock.isEmpty()
                    && blockState.isEmpty()
                    && canSeeSky.isEmpty()
                    && commandResult.isEmpty()
                    && destroySpeed.isEmpty()
                    && distanceFromCoordinates.isEmpty()
                    && explosionResistance.isEmpty()
                    && fluidType.isEmpty()
                    && friction.isEmpty()
                    && height.isEmpty()
                    && inRain.isEmpty()
                    && inSnow.isEmpty()
                    && inThunder.isEmpty()
                    && lightBlocking.isEmpty()
                    && lightLevel.isEmpty()
                    && movementBlocking.isEmpty()
                    && nbt.isEmpty()
                    && offset.isEmpty()
                    && replaceable.isEmpty()
                    && waterLoggable.isEmpty();
        }

        public boolean test(BlockFilterContext context) {
            return testAdjacent(context)
                    && testAttachable(context)
                    && testBlock(context)
                    && testBlockEntity(context)
                    && testInstanceof(context)
                    && testBlockState(context)
                    && testCanSeeSky(context)
                    && testCommandResult(context)
                    && testDestroySpeed(context)
                    && testDistanceFromCoordinates(context)
                    && testExplosionResistance(context)
                    && testFluidType(context)
                    && testFriction(context)
                    && testHeight(context)
                    && testInRain(context)
                    && testInSnow(context)
                    && testInThunder(context)
                    && testLightBlocking(context)
                    && testLightLevel(context)
                    && testMovementBlocking(context)
                    && testNbt(context)
                    && testOffset(context)
                    && testReplaceable(context)
                    && testWaterLoggable(context);
        }

        private boolean testAdjacent(BlockFilterContext context) {
            return adjacent.isEmpty() || adjacent.get().test(context);
        }

        private boolean testAttachable(BlockFilterContext context) {
            return attachable.isEmpty() || attachable.get() == isAttachable(context);
        }

        private boolean testBlock(BlockFilterContext context) {
            return block.isEmpty() || block.get().contains(context.blockState().typeHolder());
        }

        private boolean testBlockEntity(BlockFilterContext context) {
            return blockEntity.isEmpty() || blockEntity.get() == context.blockEntity().isPresent();
        }

        private boolean testInstanceof(BlockFilterContext context) {
            if (instanceofBlock.isEmpty()) return true;

            var state = context.blockState();
            return switch (instanceofBlock.get()) {
                case AIR -> state.isAir();
                case CROP -> state.getBlock() instanceof CropBlock;
                case FLOWER -> state.getBlock() instanceof FlowerBlock;
                case SAPLING -> state.getBlock() instanceof SaplingBlock;
                case VEGETATION -> state.getBlock() instanceof VegetationBlock;
                case BONEMEALABLE -> state.getBlock() instanceof BonemealableBlock;
                case FLUID -> !state.getFluidState().isEmpty();
                case FALLING -> state.getBlock() instanceof FallingBlock;
                case LEAVES -> state.getBlock() instanceof LeavesBlock;
            };
        }

        private boolean testBlockState(BlockFilterContext context) {
            return blockState.isEmpty() || blockState.get().test(context);
        }

        private boolean testCanSeeSky(BlockFilterContext context) {
            return canSeeSky.isEmpty() || canSeeSky.get() == context.level().canSeeSky(context.pos());
        }

        private boolean testCommandResult(BlockFilterContext context) {
            return commandResult.isEmpty() || !BlockLogicEnvironment.SERVER.matches(context) || commandResult.get().test(context);
        }

        private boolean testDestroySpeed(BlockFilterContext context) {
            return destroySpeed.isEmpty() || destroySpeed.get().test(context);
        }

        private boolean testDistanceFromCoordinates(BlockFilterContext context) {
            return distanceFromCoordinates.isEmpty() || distanceFromCoordinates.get().test(context);
        }

        private boolean testExplosionResistance(BlockFilterContext context) {
            return explosionResistance.isEmpty() || explosionResistance.get().test(context);
        }

        private boolean testFluidType(BlockFilterContext context) {
            return fluidType.isEmpty() || fluidType.get().contains(context.blockState().getFluidState().typeHolder());
        }

        private boolean testFriction(BlockFilterContext context) {
            return friction.isEmpty() || friction.get().test(context);
        }

        private boolean testHeight(BlockFilterContext context) {
            return height.isEmpty() || height.get().test(context);
        }

        private boolean testInRain(BlockFilterContext context) {
            return inRain.isEmpty() || inRain.get() == context.level().isRainingAt(context.pos().above());
        }

        private boolean testInSnow(BlockFilterContext context) {
            if (inSnow.isEmpty()) return true;
            boolean snowing = context.level().getBiome(context.pos()).value().getPrecipitationAt(context.pos(), context.level().getSeaLevel()) == Biome.Precipitation.SNOW && context.level().isRaining() && context.level().canSeeSky(context.pos());
            return inSnow.get() == snowing;
        }

        private boolean testInThunder(BlockFilterContext context) {
            return inThunder.isEmpty() || inThunder.get() == inThunderstorm(context.level(), context.pos().above());
        }

        private boolean testLightBlocking(BlockFilterContext context) {
            return lightBlocking.isEmpty() || lightBlocking.get() == context.blockState().isSolidRender();
        }

        private boolean testLightLevel(BlockFilterContext context) {
            return lightLevel.isEmpty() || lightLevel.get().test(context);
        }

        private boolean testMovementBlocking(BlockFilterContext context) {
            if (movementBlocking.isEmpty()) return true;

            var state = context.blockState();
            boolean blocksMovement = state.blocksMotion() && !state.getCollisionShape(context.level(), context.pos()).isEmpty();

            return movementBlocking.get() == blocksMovement;
        }

        private boolean testNbt(BlockFilterContext context) {
            return nbt.isEmpty() || context.blockEntity()
                    .map(blockEntity -> blockEntity.saveWithoutMetadata(context.level().registryAccess()))
                    .map(blockEntityNbt -> NbtUtils.compareNbt(nbt.get(), blockEntityNbt, true))
                    .orElse(false);
        }

        private boolean testOffset(BlockFilterContext context) {
            return offset.isEmpty() || offset.get().test(context);
        }

        private boolean testReplaceable(BlockFilterContext context) {
            return replaceable.isEmpty() || replaceable.get() == context.blockState().canBeReplaced();
        }

        private boolean testWaterLoggable(BlockFilterContext context) {
            return waterLoggable.isEmpty() || waterLoggable.get() == (context.blockState().getBlock() instanceof SimpleWaterloggedBlock);
        }

        // -------------------------------------------------------------------------------------------------------------------------------

        private static boolean inThunderstorm(Level level, BlockPos pos) {
            return level.isThundering() && level.isRaining() && level.canSeeSky(pos);
        }

        private static boolean isAttachable(BlockFilterContext context) {
            Level level = context.level();
            BlockPos pos = context.pos();

            for (Direction direction : Direction.values()) {
                BlockPos offsetPos = pos.relative(direction);

                if (level.hasChunkAt(offsetPos) && level.getBlockState(offsetPos).isFaceSturdy(level, pos, direction.getOpposite())) {
                    return true;
                }
            }

            return false;
        }
    }

    public static CompoundTag addExampleNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("CustomName", "Test Chest");
        tag.put("Items", new ListTag());
        return tag;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record AdjacentFilter(NumberComparator comparator, float compareTo, BlockFilter filter) {

        private static final MapCodec<AdjacentFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(AdjacentFilter::comparator),
                Codec.FLOAT.fieldOf("compare_to").forGetter(AdjacentFilter::compareTo),
                Codec.lazyInitialized(() -> BlockFilter.CODEC).fieldOf("filter").forGetter(AdjacentFilter::filter)
        ).apply(instance, AdjacentFilter::new));

        public static final Codec<AdjacentFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", net.threetag.palladium.documentation.Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", net.threetag.palladium.documentation.Documented.TYPE_FLOAT, "Value that is being compared against.")
                    .add("filter", SettingType.simple("Block Filters"), "Block filter(s) that adjacent blocks must pass.")
                    .buildObject("Adjacent Blocks");
        }

        public boolean test(BlockFilterContext context) {
            Level level = context.level();
            BlockPos pos = context.pos();

            int matches = 0;
            for (Direction direction : Direction.values()) {
                BlockPos offsetPos = pos.relative(direction);
                if (level.hasChunkAt(offsetPos) && filter.test(level, offsetPos)) {
                    matches++;
                }
            }

            return comparator.compare(matches, compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public enum InstanceofType implements StringRepresentable {
        AIR, CROP, FLOWER, SAPLING, VEGETATION, BONEMEALABLE, FLUID, FALLING, LEAVES;

        public static final Codec<InstanceofType> CODEC = StringRepresentable.fromEnum(InstanceofType::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record BlockStateFilter(String property, Optional<NumberComparator> comparator, Optional<Integer> compareTo, Optional<Boolean> boolValue, Optional<String> enumValue) {

        private static final MapCodec<BlockStateFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(BlockStateFilter::property),
                NumberComparator.CODEC.optionalFieldOf("comparator").forGetter(BlockStateFilter::comparator),
                Codec.INT.optionalFieldOf("compare_to").forGetter(BlockStateFilter::compareTo),
                Codec.BOOL.optionalFieldOf("value").forGetter(BlockStateFilter::boolValue),
                Codec.STRING.optionalFieldOf("enum").forGetter(BlockStateFilter::enumValue)
        ).apply(instance, BlockStateFilter::new));

        public static final Codec<BlockStateFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .add("property", Documented.TYPE_STRING, "The name of the block state property. See: https://minecraft.wiki/w/Block_states#List_of_block_states")
                    .addOptional("comparator", Documented.TYPE_NUMBER_COMPARATOR, "The comparison operator used for integer properties.")
                    .addOptional("compare_to", Documented.TYPE_INT, "The integer value to compare against.")
                    .addOptional("value", Documented.TYPE_BOOLEAN, "The boolean value to compare against.")
                    .addOptional("enum", Documented.TYPE_STRING, "The enum value to compare against.")
                    .buildObject("Block State");
        }

        public boolean test(BlockFilterContext context) {
            var blockState = context.blockState();
            var propValue = blockState.getProperties()
                    .stream()
                    .filter(prop -> prop.getName().equals(property))
                    .map(blockState::getValue)
                    .findFirst()
                    .orElse(null);

            return switch (propValue) {
                case Enum<?> enumProp when enumValue.isPresent() -> enumProp.name().equalsIgnoreCase(enumValue.get());
                case Boolean boolProp when boolValue.isPresent() -> boolProp == boolValue.get();
                case Integer intProp when comparator.isPresent() && compareTo.isPresent() -> comparator.get().compare(intProp, compareTo.get());
                case null, default -> propValue != null;
            };
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record CommandResultFilter(String command, NumberComparator comparator, int compareTo) {

        private static final MapCodec<CommandResultFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("command").forGetter(CommandResultFilter::command),
                NumberComparator.CODEC.fieldOf("comparator").forGetter(CommandResultFilter::comparator),
                Codec.INT.fieldOf("compare_to").forGetter(CommandResultFilter::compareTo)
        ).apply(instance, CommandResultFilter::new));

        public static final Codec<CommandResultFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("command", Documented.TYPE_STRING, "Command to run.")
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_INT, "Value that is being compared against.")
                    .buildObject("Command Result");
        }

        public boolean test(BlockFilterContext context) {
            if (!(context.level() instanceof ServerLevel serverLevel)) return false;

            MinecraftServer server = serverLevel.getServer();
            AtomicInteger result = new AtomicInteger();

            String blockName = context.blockState().getBlock().toString();

            CommandSourceStack source = new CommandSourceStack(
                    CommandSource.NULL,
                    Vec3.atCenterOf(context.pos()),
                    Vec2.ZERO,
                    serverLevel,
                    PermissionSet.ALL_PERMISSIONS,
                    blockName,
                    Component.literal(blockName),
                    server,
                    null
            );

            source = source.withCallback((success, returnValue) -> result.set(returnValue));
            server.getCommands().performPrefixedCommand(source, command);

            return comparator.compare(result.get(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record DestroySpeedFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<DestroySpeedFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(DestroySpeedFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(DestroySpeedFilter::compareTo)
        ).apply(instance, DestroySpeedFilter::new));

        public static final Codec<DestroySpeedFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_FLOAT_VALUE, "Value that is being compared against.")
                    .buildObject("Destroy Speed");
        }

        public boolean test(BlockFilterContext context) {
            float destroySpeed = context.blockState().getDestroySpeed(context.level(), context.pos());
            float comparedValue = compareTo.getAsFloat(null);

            return comparator.compare(destroySpeed, comparedValue);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record DistanceFromCoordinatesFilter(DistanceFromCoordinates.Reference reference, Shape shape, Optional<Integer> roundToDigit, Optional<DistanceFromCoordinates.Offset> offset, NumberComparator comparator, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ) implements DistanceFromCoordinates {

        public static final MapCodec<DistanceFromCoordinatesFilter> MAP_CODEC = DistanceFromCoordinates.mapCodec(DistanceFromCoordinatesFilter::new);
        public static final Codec<DistanceFromCoordinatesFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("reference", SettingType.enumList(DistanceFromCoordinates.Reference.values()), "The point to compare the distance to.", Reference.WORLD_ORIGIN)
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "Determines the shape of the check.", Shape.CUBE)
                    .addOptional("round_to_digit", Documented.TYPE_INT, "Rounds the result to the closest number with the specified amount of digits after the comma. Negative numbers also work (e.g: -2 rounds to multiples of 100).")
                    .addOptional("offset", Documented.TYPE_VECTOR3, "Determines how much the reference point should be offset.")
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Determines how the calculated distance (in blocks) should be compared to the specified value.")
                    .add("compare_to", Documented.TYPE_DOUBLE, "The value at which the calculated distance (in blocks) will be compared to.")
                    .addOptional("scale_reference_to_dimension", Documented.TYPE_BOOLEAN, "Whether to scale the reference coordinates to the current dimension.", true)
                    .addOptional("scale_distance_to_dimension", Documented.TYPE_BOOLEAN, "Whether to scale the calculated distance to the current dimension.", false)
                    .addOptional("ignore_x", Documented.TYPE_BOOLEAN, "If to consider the X axis to be 0.", false)
                    .addOptional("ignore_y", Documented.TYPE_BOOLEAN, "If to consider the Y axis to be 0.", false)
                    .addOptional("ignore_z", Documented.TYPE_BOOLEAN, "If to consider the Z axis to be 0.", false)
                    .buildObject("Distance From Coordinates");
        }

        public boolean test(BlockFilterContext context) {
            return DistanceFromCoordinates.super.testCondition(Either.left(context));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record ExplosionResistanceFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<ExplosionResistanceFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(ExplosionResistanceFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(ExplosionResistanceFilter::compareTo)
        ).apply(instance, ExplosionResistanceFilter::new));

        public static final Codec<ExplosionResistanceFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_FLOAT_VALUE, "Value that is being compared against.")
                    .buildObject("Explosion Resistance");
        }

        public boolean test(BlockFilterContext context) {
            float resistance = context.blockState().getBlock().getExplosionResistance();
            float comparedValue = compareTo.getAsFloat(null);

            return comparator.compare(resistance, comparedValue);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FluidTypeFilter(PalladiumHolderSet<Fluid> fluid) {

        private static final MapCodec<FluidTypeFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PalladiumHolderSet.codec(Registries.FLUID).fieldOf("fluid_type").forGetter(FluidTypeFilter::fluid)
        ).apply(instance, FluidTypeFilter::new));

        public static final Codec<FluidTypeFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("fluid_type", KryptoniteDocumented.TYPE_FLUID_TYPE_HOLDER_SET, "Fluid IDs or tags of the required fluid type.")
                    .buildObject("Fluid Type");
        }

        public boolean test(BlockFilterContext context) {
            return fluid.contains(context.blockState().getFluidState().typeHolder());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FrictionFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<FrictionFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(FrictionFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(FrictionFilter::compareTo)
        ).apply(instance, FrictionFilter::new));

        public static final Codec<FrictionFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_FLOAT_VALUE, "Value that is being compared against.")
                    .buildObject("Friction");
        }

        public boolean test(BlockFilterContext context) {
            float friction = context.blockState().getBlock().getFriction();
            float comparedValue = compareTo.getAsFloat(null);

            return comparator.compare(friction, comparedValue);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record HeightFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<HeightFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(HeightFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(HeightFilter::compareTo)
        ).apply(instance, HeightFilter::new));

        public static final Codec<HeightFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_INT_VALUE, "Value that is being compared against.")
                    .buildObject("Height");
        }

        public boolean test(BlockFilterContext context) {
            int y = context.pos().getY();
            int comparedValue = compareTo.getAsInt(null);

            return comparator.compare(y, comparedValue);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record LightLevelFilter(Optional<LightType> lightType, NumberComparator comparator, Value compareTo) {

        private static final MapCodec<LightLevelFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                LightType.CODEC.optionalFieldOf("light_type").forGetter(LightLevelFilter::lightType),
                NumberComparator.CODEC.fieldOf("comparator").forGetter(LightLevelFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(LightLevelFilter::compareTo)
        ).apply(instance, LightLevelFilter::new));

        public static final Codec<LightLevelFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation() {
            return new CodecDocumentationBuilder<>(MAP_CODEC, null)
                    .addOptional("light_type", SettingType.enumList(LightType.values()), "Checks and compares a specific type of light.")
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", Documented.TYPE_INT_VALUE, "Value that is being compared against.")
                    .buildObject("Light Level");
        }

        public boolean test(BlockFilterContext context) {
            Level level = context.level();
            BlockPos pos = context.pos();

            int lightLevel = lightType.map(type -> switch (type) {
                case SKY -> level.getBrightness(LightLayer.SKY, pos);
                case BLOCK -> level.getBrightness(LightLayer.BLOCK, pos);
            }).orElseGet(() -> level.getLightEngine().getRawBrightness(pos, level.getSkyDarken()));

            int comparedValue = compareTo.getAsInt(null);
            return comparator.compare(lightLevel, comparedValue);
        }
    }

    public enum LightType implements StringRepresentable {
        SKY, BLOCK;

        public static final Codec<LightType> CODEC = StringRepresentable.fromEnum(LightType::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record OffsetFilter(BlockFilter blockFilter, Value x, Value y, Value z) {

        private static final MapCodec<OffsetFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.lazyInitialized(() -> BlockFilter.CODEC).fieldOf("block_filters").forGetter(OffsetFilter::blockFilter),
                Value.NUMBER_CODEC.optionalFieldOf("x", new StaticValue(0)).forGetter(OffsetFilter::x),
                Value.NUMBER_CODEC.optionalFieldOf("y", new StaticValue(0)).forGetter(OffsetFilter::y),
                Value.NUMBER_CODEC.optionalFieldOf("z", new StaticValue(0)).forGetter(OffsetFilter::z)
        ).apply(instance, OffsetFilter::new));

        public static final Codec<OffsetFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("block_filters", SettingType.simple("Block Filters"), "Block filter(s) that must pass at the offset position.")
                    .addOptional("x", Documented.TYPE_FLOAT_VALUE, "How much to offset the position on the X axis.", new StaticValue(0))
                    .addOptional("y", Documented.TYPE_FLOAT_VALUE, "How much to offset the position on the Y axis.", new StaticValue(0))
                    .addOptional("z", Documented.TYPE_FLOAT_VALUE, "How much to offset the position on the Z axis.", new StaticValue(0))
                    .buildObject("Offset");
        }

        public boolean test(BlockFilterContext context) {
            BlockPos offsetBlockPos = context.pos().offset(x.getAsInt(null), y.getAsInt(null), z.getAsInt(null));
            return context.level().hasChunkAt(offsetBlockPos) && blockFilter.test(context.level(), offsetBlockPos);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public enum Operator {
        AND, OR, NOT;

        public static final Codec<Operator> CODEC = Codec.STRING.xmap(name -> Operator.valueOf(name.toUpperCase(Locale.ROOT)), operator -> operator.name().toLowerCase(Locale.ROOT));

        public boolean test(List<FilterType> filters, BlockFilterContext context) {
            return switch (this) {
                case AND -> filters.stream().allMatch(filter -> filter.test(context));
                case OR -> filters.stream().anyMatch(filter -> filter.test(context));
                case NOT -> filters.stream().noneMatch(filter -> filter.test(context));
            };
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static <T> Optional<T> none() {
        return Optional.empty();
    }

    private static BlockFilter single(FilterType type) {
        return new BlockFilter(Operator.AND, List.of(type));
    }

    public static BlockFilter ofAdjacent(NumberComparator comparator, float compareTo, BlockFilter filter) {
        return single(new FilterType(Optional.of(new AdjacentFilter(comparator, compareTo, filter)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofAttachable(boolean attachable) {
        return single(new FilterType(none(), Optional.of(attachable), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofBlock(PalladiumHolderSet<Block> block) {
        return single(new FilterType(none(), none(), Optional.of(block), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofBlockEntity(boolean blockEntity) {
        return single(new FilterType(none(), none(), none(), Optional.of(blockEntity), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofInstanceof(InstanceofType type) {
        return single(new FilterType(none(), none(), none(), none(), Optional.of(type), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofBlockState(String property, Optional<NumberComparator> comparator, Optional<Integer> compareTo, Optional<Boolean> value, Optional<String> enumValue) {
        return single(new FilterType(none(), none(), none(), none(), none(), Optional.of(new BlockStateFilter(property, comparator, compareTo, value, enumValue)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofCanSeeSky(boolean canSeeSky) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), Optional.of(canSeeSky), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofCommandResult(String command, NumberComparator comparator, int compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), Optional.of(new CommandResultFilter(command, comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofDestroySpeed(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new DestroySpeedFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofDistanceFromCoordinates(DistanceFromCoordinates.Reference reference, Shape shape, Optional<Integer> roundToDigit, Optional<DistanceFromCoordinates.Offset> offset, NumberComparator comparator, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new DistanceFromCoordinatesFilter(reference, shape, roundToDigit, offset, comparator, compareTo, scaleReferenceToDimension, scaleDistanceToDimension, ignoreX, ignoreY, ignoreZ)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofExplosionResistance(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new ExplosionResistanceFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofFluidType(PalladiumHolderSet<Fluid> fluid) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(fluid), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofFriction(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new FrictionFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofHeight(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new HeightFilter(comparator, compareTo)), none(), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofInRain(boolean inRain) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(inRain), none(), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofInSnow(boolean inSnow) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(inSnow), none(), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofInThunder(boolean inThunder) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(inThunder), none(), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofLightBlocking(boolean lightBlocking) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(lightBlocking), none(), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofLightLevel(Optional<LightType> lightType, NumberComparator comparator, Value compareTo) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new LightLevelFilter(lightType, comparator, compareTo)), none(), none(), none(), none(), none()));
    }

    public static BlockFilter ofMovementBlocking(boolean movementBlocking) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(movementBlocking), none(), none(), none(), none()));
    }

    public static BlockFilter ofNbt(CompoundTag nbt) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(nbt), none(), none(), none()));
    }

    public static BlockFilter ofOffset(BlockFilter blockFilter, Value x, Value y, Value z) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(new OffsetFilter(blockFilter, x, y, z)), none(), none()));
    }

    public static BlockFilter ofReplaceable(boolean replaceable) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(replaceable), none()));
    }

    public static BlockFilter ofWaterLoggable(boolean waterLoggable) {
        return single(new FilterType(none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), none(), Optional.of(waterLoggable)));
    }

}