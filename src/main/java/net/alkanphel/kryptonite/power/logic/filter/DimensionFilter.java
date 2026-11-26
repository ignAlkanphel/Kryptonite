package net.alkanphel.kryptonite.power.logic.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.NumberComparator;

import java.util.*;

public record DimensionFilter(Operator operator, List<FilterType> filters) {

    public static final DimensionFilter EMPTY = new DimensionFilter(Operator.AND, List.of());

    private static final MapCodec<DimensionFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Operator.CODEC.optionalFieldOf("operator", Operator.AND).forGetter(DimensionFilter::operator),
            ExtraCodecs.compactListCodec(FilterType.CODEC).fieldOf("filters").forGetter(DimensionFilter::filters)
    ).apply(instance, DimensionFilter::new));

    public static final Codec<DimensionFilter> CODEC = MAP_CODEC.codec();

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType filterType = FilterType.documentation(provider);
        return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                .addOptional("operator", SettingType.enumList(Operator.values()), "How the filters are combined: \"and\" needs all to pass, \"or\" needs at least one, \"not\" needs none.", Operator.AND)
                .add("filters", SettingType.combined(filterType, SettingType.array(filterType)), "Dimension filter type(s) that must pass.")
                .buildObject("Dimension Filters");
    }

    public boolean test(DimensionConditionContext context) {
        return operator.test(filters, context);
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FilterType(
            Optional<AmbientLightFilter> ambientLight,
            Optional<EnvironmentAttributeMap> attributes,
            Optional<CardinalLighting.Type> cardinalLight,
            Optional<CoordinateScaleFilter> coordinateScale,
            Optional<Holder<WorldClock>> defaultClock,
            Optional<ResourceKey<Level>> dimension,
            Optional<Boolean> hasCeiling,
            Optional<Boolean> hasEnderDragonFight,
            Optional<Boolean> hasEndFlashes,
            Optional<Boolean> hasFixedTime,
            Optional<Boolean> hasSkylight,
            Optional<HeightFilter> height,
            Optional<TagKey<Block>> infiniburn,
            Optional<LogicalHeightFilter> logicalHeight,
            Optional<MinYFilter> minY,
            Optional<MonsterSettingsFilter> monsterSettings,
            Optional<DimensionType.Skybox> skybox,
            Optional<HolderSet<Timeline>> timelines
    ) {

        private static final MapCodec<Optional<MonsterSettingsFilter>> MONSTER_SETTINGS_CODEC =
                MonsterSettingsFilter.CODEC.optionalFieldOf("monster_settings");

        private static final MapCodec<Optional<HolderSet<Timeline>>> TIMELINES_CODEC =
                RegistryCodecs.homogeneousList(Registries.TIMELINE).optionalFieldOf("timelines");

        private static final MapCodec<FilterType> MAP_CODEC = RecordCodecBuilder.<FilterType>mapCodec(instance -> instance.group(
                AmbientLightFilter.CODEC.optionalFieldOf("ambient_light").forGetter(FilterType::ambientLight),
                EnvironmentAttributeMap.CODEC.optionalFieldOf("attributes").forGetter(FilterType::attributes),
                CardinalLighting.Type.CODEC.optionalFieldOf("cardinal_light").forGetter(FilterType::cardinalLight),
                CoordinateScaleFilter.CODEC.optionalFieldOf("coordinate_scale").forGetter(FilterType::coordinateScale),
                WorldClock.CODEC.optionalFieldOf("default_clock").forGetter(FilterType::defaultClock),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(FilterType::dimension),
                Codec.BOOL.optionalFieldOf("has_ceiling").forGetter(FilterType::hasCeiling),
                Codec.BOOL.optionalFieldOf("has_ender_dragon_fight").forGetter(FilterType::hasEnderDragonFight),
                Codec.BOOL.optionalFieldOf("has_end_flashes").forGetter(FilterType::hasEndFlashes),
                Codec.BOOL.optionalFieldOf("has_fixed_time").forGetter(FilterType::hasFixedTime),
                Codec.BOOL.optionalFieldOf("has_skylight").forGetter(FilterType::hasSkylight),
                HeightFilter.CODEC.optionalFieldOf("height").forGetter(FilterType::height),
                TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("infiniburn").forGetter(FilterType::infiniburn),
                LogicalHeightFilter.CODEC.optionalFieldOf("logical_height").forGetter(FilterType::logicalHeight),
                MinYFilter.CODEC.optionalFieldOf("min_y").forGetter(FilterType::minY),
                DimensionType.Skybox.CODEC.optionalFieldOf("skybox").forGetter(FilterType::skybox)
        ).apply(instance, (ambientLight, attributes, cardinalLight, coordinateScale, defaultClock,
                           dimension, hasCeiling, hasEnderDragonFight, hasEndFlashes, hasFixedTime,
                           hasSkylight, height, infiniburn, logicalHeight, minY, skybox) ->
                new FilterType(
                        ambientLight, attributes, cardinalLight, coordinateScale, defaultClock,
                        dimension, hasCeiling, hasEnderDragonFight, hasEndFlashes, hasFixedTime,
                        hasSkylight, height, infiniburn, logicalHeight, minY, Optional.empty(),
                        skybox, Optional.empty()
                )
        )).dependent(
                MONSTER_SETTINGS_CODEC,
                filter -> Pair.of(filter.monsterSettings(), MONSTER_SETTINGS_CODEC),
                FilterType::withMonsterSettings
        ).dependent(
                TIMELINES_CODEC,
                filter -> Pair.of(filter.timelines(), TIMELINES_CODEC),
                FilterType::withTimelines
        );

        private FilterType withMonsterSettings(Optional<MonsterSettingsFilter> monsterSettings) {
            return new FilterType(
                    ambientLight, attributes, cardinalLight, coordinateScale, defaultClock,
                    dimension, hasCeiling, hasEnderDragonFight, hasEndFlashes, hasFixedTime,
                    hasSkylight, height, infiniburn, logicalHeight, minY, monsterSettings,
                    skybox, timelines
            );
        }

        private FilterType withTimelines(Optional<HolderSet<Timeline>> timelines) {
            return new FilterType(
                    ambientLight, attributes, cardinalLight, coordinateScale, defaultClock,
                    dimension, hasCeiling, hasEnderDragonFight, hasEndFlashes, hasFixedTime,
                    hasSkylight, height, infiniburn, logicalHeight, minY, monsterSettings,
                    skybox, timelines
            );
        }

        public static final Codec<FilterType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("ambient_light", AmbientLightFilter.documentation(provider), "Checks the ambient light level of the dimension.")
                    .addOptional("attributes", KryptoniteDocumented.TYPE_JSON_OBJECT, "Checks the environment attributes of the dimension.")
                    .addOptional("cardinal_light", KryptoniteDocumented.TYPE_CARDINAL_LIGHTING, "Checks the cardinal lighting type of the dimension.")
                    .addOptional("coordinate_scale", CoordinateScaleFilter.documentation(provider), "Checks the coordinate scale of the dimension.")
                    .addOptional("default_clock", Documented.TYPE_IDENTIFIER, "Checks the default world clock of the dimension.")
                    .addOptional("dimension", Documented.TYPE_IDENTIFIER, "Checks the dimension id.")
                    .addOptional("has_ceiling", Documented.TYPE_BOOLEAN, "Checks if the dimension has a ceiling.")
                    .addOptional("has_ender_dragon_fight", Documented.TYPE_BOOLEAN, "Checks if the dimension has an Ender Dragon fight.")
                    .addOptional("has_end_flashes", Documented.TYPE_BOOLEAN, "Checks if the dimension has an End flashes.")
                    .addOptional("has_fixed_time", Documented.TYPE_BOOLEAN, "Checks if the dimension has a fixed time.")
                    .addOptional("has_skylight", Documented.TYPE_BOOLEAN, "Checks if the dimension has skylight (daylight cycle / weather).")
                    .addOptional("height", HeightFilter.documentation(provider), "Checks the height of the dimension.")
                    .addOptional("infiniburn", Documented.TYPE_BLOCK_TAG, "Checks the infiniburn block tag used by the dimension.")
                    .addOptional("logical_height", LogicalHeightFilter.documentation(provider), "Checks the logical height of the dimension.")
                    .addOptional("min_y", MinYFilter.documentation(provider), "Checks the minimum Y of the dimension.")
                    .addOptional("monster_settings", MonsterSettingsFilter.documentation(provider), "Checks the monster settings of the dimension.")
                    .addOptional("skybox", KryptoniteDocumented.TYPE_SKYBOX, "Checks the skybox type of the dimension.")
                    .addOptional("timelines", KryptoniteDocumented.TYPE_TIMELINE_HOLDER_SET, "Checks the timelines of the dimension.")
                    .buildObject("Dimension Filter Types");
        }

        public boolean isEmpty() {
            return ambientLight.isEmpty()
                    && attributes.isEmpty()
                    && cardinalLight.isEmpty()
                    && coordinateScale.isEmpty()
                    && defaultClock.isEmpty()
                    && dimension.isEmpty()
                    && hasCeiling.isEmpty()
                    && hasEnderDragonFight.isEmpty()
                    && hasEndFlashes.isEmpty()
                    && hasFixedTime.isEmpty()
                    && hasSkylight.isEmpty()
                    && height.isEmpty()
                    && infiniburn.isEmpty()
                    && logicalHeight.isEmpty()
                    && minY.isEmpty()
                    && monsterSettings.isEmpty()
                    && skybox.isEmpty()
                    && timelines.isEmpty();
        }

        public boolean test(DimensionConditionContext context) {
            return testAmbientLight(context)
                    && testAttributes(context)
                    && testCardinalLight(context)
                    && testCoordinateScale(context)
                    && testDefaultClock(context)
                    && testDimension(context)
                    && testHasCeiling(context)
                    && testHasEnderDragonFight(context)
                    && testHasEndFlashes(context)
                    && testHasFixedTime(context)
                    && testHasSkylight(context)
                    && testHeight(context)
                    && testInfiniburn(context)
                    && testLogicalHeight(context)
                    && testMinY(context)
                    && testMonsterSettings(context)
                    && testSkybox(context)
                    && testTimelines(context);
        }

        private boolean testAmbientLight(DimensionConditionContext context) {
            return ambientLight.isEmpty() || ambientLight.get().test(context);
        }

        private boolean testAttributes(DimensionConditionContext context) {
            return attributes.isEmpty() || testAttributes(attributes.get(), context);
        }

        private boolean testCardinalLight(DimensionConditionContext context) {
            if (cardinalLight.isEmpty()) return true;
            if (context.level() == null) return false;

            return context.dimensionType().cardinalLightType() == cardinalLight.get();
        }

        private boolean testCoordinateScale(DimensionConditionContext context) {
            return coordinateScale.isEmpty() || coordinateScale.get().test(context);
        }

        private boolean testDefaultClock(DimensionConditionContext context) {
            if (defaultClock.isEmpty()) return true;
            if (context.level() == null || context.dimensionType().defaultClock().isEmpty()) return false;

            return context.dimensionType().defaultClock().get().equals(defaultClock.get());
        }

        private boolean testDimension(DimensionConditionContext context) {
            return dimension.isEmpty() || context.level() != null && context.level().dimension().equals(dimension.get());
        }

        private boolean testHasCeiling(DimensionConditionContext context) {
            return hasCeiling.isEmpty() || hasCeiling.get() == context.dimensionType().hasCeiling();
        }

        private boolean testHasEnderDragonFight(DimensionConditionContext context) {
            return hasEnderDragonFight.isEmpty() || hasEnderDragonFight.get() == context.dimensionType().hasEnderDragonFight();
        }

        private boolean testHasEndFlashes(DimensionConditionContext context) {
            return hasEndFlashes.isEmpty() || hasEndFlashes.get() == context.dimensionType().hasEndFlashes();
        }

        private boolean testHasFixedTime(DimensionConditionContext context) {
            return hasFixedTime.isEmpty() || hasFixedTime.get() == context.dimensionType().hasFixedTime();
        }

        private boolean testHasSkylight(DimensionConditionContext context) {
            return hasSkylight.isEmpty() || hasSkylight.get() == context.dimensionType().hasSkyLight();
        }

        private boolean testSkybox(DimensionConditionContext context) {
            return skybox.isEmpty() || context.dimensionType().skybox() == skybox.get();
        }

        private boolean testHeight(DimensionConditionContext context) {
            return height.isEmpty() || height.get().test(context);
        }

        private boolean testInfiniburn(DimensionConditionContext context) {
            return infiniburn.isEmpty() || context.dimensionType().infiniburn().equals(infiniburn.get());
        }

        private boolean testLogicalHeight(DimensionConditionContext context) {
            return logicalHeight.isEmpty() || logicalHeight.get().test(context);
        }

        private boolean testMinY(DimensionConditionContext context) {
            return minY.isEmpty() || minY.get().test(context);
        }

        private boolean testMonsterSettings(DimensionConditionContext context) {
            return monsterSettings.isEmpty() || monsterSettings.get().test(context);
        }

        private boolean testTimelines(DimensionConditionContext context) {
            if (timelines.isEmpty()) return true;
            if (context.level() == null) return false;

            HolderSet<Timeline> dimensionTimelines = context.dimensionType().timelines();
            if (dimensionTimelines instanceof HolderSet.Named<Timeline> dimTag && timelines.get() instanceof HolderSet.Named<Timeline> conditionTag) {
                return dimTag.key().equals(conditionTag.key());
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------

        private static boolean testAttributes(EnvironmentAttributeMap attributes, DimensionConditionContext context) {
            Level level = context.level();
            Entity entity = context.entity();
            if (level == null || entity == null) return false;

            EnvironmentAttributeSystem system = level.environmentAttributes();
            Vec3 pos = entity.position();

            for (EnvironmentAttribute attribute : attributes.keySet()) {
                var expectedEntry = attributes.get(attribute);
                if (expectedEntry == null) return false;

                Object expectedValue = expectedEntry.applyModifier(attribute.defaultValue());
                Object actualValue = attribute.isPositional()
                        ? system.getValue(attribute, pos)
                        : system.getDimensionValue(attribute);

                if (!Objects.equals(expectedValue, actualValue)) {
                    return false;
                }
            }

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record AmbientLightFilter(NumberComparator comparator, float compareTo) {

        private static final MapCodec<AmbientLightFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(AmbientLightFilter::comparator),
                Codec.FLOAT.fieldOf("compare_to").forGetter(AmbientLightFilter::compareTo)
        ).apply(instance, AmbientLightFilter::new));

        public static final Codec<AmbientLightFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", SettingType.combined(SettingType.doubleRange(0.0F, 1.0F)), "Value that is being compared against.")
                    .buildObject("Dimension Ambient Light");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.level() == null) return false;
            return comparator.compare(context.dimensionType().ambientLight(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record CoordinateScaleFilter(NumberComparator comparator, double compareTo) {

        private static final MapCodec<CoordinateScaleFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(CoordinateScaleFilter::comparator),
                Codec.DOUBLE.fieldOf("compare_to").forGetter(CoordinateScaleFilter::compareTo)
        ).apply(instance, CoordinateScaleFilter::new));

        public static final Codec<CoordinateScaleFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", SettingType.combined(SettingType.doubleRange(1.0E-5F, 3.0E7)), "Value that is being compared against.")
                    .buildObject("Dimension Coordinate Scale");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.level() == null) return false;
            return comparator.compare(context.dimensionType().coordinateScale(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record HeightFilter(NumberComparator comparator, int compareTo) {

        private static final MapCodec<HeightFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(HeightFilter::comparator),
                Codec.INT.fieldOf("compare_to").forGetter(HeightFilter::compareTo)
        ).apply(instance, HeightFilter::new));

        public static final Codec<HeightFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", SettingType.combined(SettingType.intRange(16, DimensionType.Y_SIZE)), "Value that is being compared against.")
                    .buildObject("Dimension Height");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.level() == null) return false;
            return comparator.compare(context.dimensionType().height(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record LogicalHeightFilter(NumberComparator comparator, int compareTo) {

        private static final MapCodec<LogicalHeightFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(LogicalHeightFilter::comparator),
                Codec.INT.fieldOf("compare_to").forGetter(LogicalHeightFilter::compareTo)
        ).apply(instance, LogicalHeightFilter::new));

        public static final Codec<LogicalHeightFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", SettingType.combined(SettingType.intRange(0, DimensionType.Y_SIZE)), "Value that is being compared against.")
                    .buildObject("Dimension Logical Height");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.level() == null) return false;
            return comparator.compare(context.dimensionType().logicalHeight(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record MinYFilter(NumberComparator comparator, int compareTo) {

        private static final MapCodec<MinYFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(MinYFilter::comparator),
                Codec.INT.fieldOf("compare_to").forGetter(MinYFilter::compareTo)
        ).apply(instance, MinYFilter::new));

        public static final Codec<MinYFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", SettingType.combined(SettingType.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)), "Value that is being compared against.")
                    .buildObject("Dimension Min Y");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.level() == null) return false;
            return comparator.compare(context.dimensionType().minY(), compareTo);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record MonsterSettingsFilter(
            Optional<Integer> monsterSpawnBlockLightLimit,
            Optional<IntProvider> monsterSpawnLightLevel
    ) {

        private static final Codec<IntProvider> INT_PROVIDER_CODEC = Codec.either(Codec.INT, IntProviders.CODEC).xmap(
                either -> either.map(ConstantInt::of, provider -> provider),
                provider -> provider instanceof ConstantInt constantInt
                        ? Either.left(constantInt.value())
                        : Either.right(provider)
        );

        private static final MapCodec<MonsterSettingsFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("monster_spawn_block_light_limit").forGetter(MonsterSettingsFilter::monsterSpawnBlockLightLimit),
                INT_PROVIDER_CODEC.optionalFieldOf("monster_spawn_light_level").forGetter(MonsterSettingsFilter::monsterSpawnLightLevel)
        ).apply(instance, MonsterSettingsFilter::new));

        public static final Codec<MonsterSettingsFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("monster_spawn_block_light_limit", SettingType.combined(SettingType.intRange(0, 15)), "The block light limit for monsters to spawn.")
                    .addOptional("monster_spawn_light_level", SettingType.combined(SettingType.intRange(0, 15)), "The light level for monsters to spawn.")
                    .buildObject("Dimension Monster Settings");
        }

        public boolean test(DimensionConditionContext context) {
            if (context.dimensionType() == null) return false;

            var settings = context.dimensionType().monsterSettings();

            if (monsterSpawnBlockLightLimit.isPresent() && settings.monsterSpawnBlockLightLimit() != monsterSpawnBlockLightLimit.get()) {
                return false;
            }

            if (monsterSpawnLightLevel.isPresent() && !sameIntProvider(monsterSpawnLightLevel.get(), settings.monsterSpawnLightTest())) {
                return false;
            }

            return true;
        }

        private static boolean sameIntProvider(IntProvider a, IntProvider b) {
            if (a == b) return true;

            if (a instanceof ConstantInt ac && b instanceof ConstantInt bc) {
                return ac.value() == bc.value();
            }

            if (a instanceof UniformInt au && b instanceof UniformInt bu) {
                return au.minInclusive() == bu.minInclusive() && au.maxInclusive() == bu.maxInclusive();
            }

            return a.equals(b);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public enum Operator {
        AND, OR, NOT;

        public static final Codec<Operator> CODEC = Codec.STRING.xmap(name -> Operator.valueOf(name.toUpperCase(Locale.ROOT)), operator -> operator.name().toLowerCase(Locale.ROOT));

        public boolean test(List<FilterType> filters, DimensionConditionContext context) {
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

    private static DimensionFilter single(FilterType type) {
        return new DimensionFilter(Operator.AND, List.of(type));
    }

    public static DimensionFilter ofAmbientLight(NumberComparator comparator, float compareTo) {
        return single(new FilterType(
                Optional.of(new AmbientLightFilter(comparator, compareTo)),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofAttributes(EnvironmentAttributeMap attributes) {
        return single(new FilterType(
                none(),
                Optional.of(attributes),
                none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofCardinalLight(CardinalLighting.Type cardinalLight) {
        return single(new FilterType(
                none(), none(),
                Optional.of(cardinalLight),
                none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofCoordinateScale(NumberComparator comparator, double compareTo) {
        return single(new FilterType(
                none(), none(), none(),
                Optional.of(new CoordinateScaleFilter(comparator, compareTo)),
                none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofDefaultClock(Holder<WorldClock> defaultClock) {
        return single(new FilterType(
                none(), none(), none(), none(),
                Optional.of(defaultClock),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none()
        ));
    }

    public static DimensionFilter ofDimension(ResourceKey<Level> dimension) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                Optional.of(dimension),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHasCeiling(boolean hasCeiling) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(),
                Optional.of(hasCeiling),
                none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHasEnderDragonFight(boolean hasEnderDragonFight) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(),
                Optional.of(hasEnderDragonFight),
                none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHasEndFlashes(boolean hasEndFlashes) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(),
                Optional.of(hasEndFlashes),
                none(), none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHasFixedTime(boolean hasFixedTime) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(),
                Optional.of(hasFixedTime),
                none(),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHasSkylight(boolean hasSkylight) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                Optional.of(hasSkylight),
                none(), none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofHeight(NumberComparator comparator, int compareTo) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(),
                Optional.of(new HeightFilter(comparator, compareTo)),
                none(), none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofInfiniburn(TagKey<Block> infiniburn) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(),
                Optional.of(infiniburn),
                none(), none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofLogicalHeight(NumberComparator comparator, int compareTo) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(),
                Optional.of(new LogicalHeightFilter(comparator, compareTo)),
                none(), none(),
                none(), none()
        ));
    }

    public static DimensionFilter ofMinY(NumberComparator comparator, int compareTo) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(),
                Optional.of(new MinYFilter(comparator, compareTo)),
                none(), none(), none()
        ));
    }

    public static DimensionFilter ofMonsterSettings(MonsterSettingsFilter monsterSettings) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                Optional.of(monsterSettings),
                none(), none()
        ));
    }

    public static DimensionFilter ofSkybox(DimensionType.Skybox skybox) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(),
                Optional.of(skybox),
                none()
        ));
    }

    public static DimensionFilter ofTimelines(HolderSet<Timeline> timelines) {
        return single(new FilterType(
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(), none(), none(), none(),
                none(), none(),
                Optional.of(timelines)
        ));
    }

}