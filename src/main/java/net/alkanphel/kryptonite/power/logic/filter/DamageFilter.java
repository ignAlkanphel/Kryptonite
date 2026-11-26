package net.alkanphel.kryptonite.power.logic.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.context.DamageFilterContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DamageFilter(Operator operator, List<FilterType> filters) {

    public static final DamageFilter EMPTY = new DamageFilter(Operator.AND, List.of());

    private static final MapCodec<DamageFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Operator.CODEC.optionalFieldOf("operator", Operator.AND).forGetter(DamageFilter::operator),
            ExtraCodecs.compactListCodec(FilterType.CODEC).fieldOf("filters").forGetter(DamageFilter::filters)
    ).apply(instance, DamageFilter::new));

    public static final Codec<DamageFilter> CODEC = MAP_CODEC.codec();

    public static SettingType documentation(HolderLookup.Provider provider) {
        SettingType filterType = FilterType.documentation(provider);
        return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                .addOptional("operator", SettingType.enumList(Operator.values()), "How the filters are combined: \"and\" needs all to pass, \"or\" needs at least one, \"not\" needs none.", Operator.AND)
                .add("filters", SettingType.combined(filterType, SettingType.array(filterType)), "Damage filter type(s) that must pass.")
                .buildObject("Damage Filters");
    }

    public boolean test(DamageFilterContext context) {
        return operator.test(filters, context);
    }

    public boolean test(DamageSource source, float damage) {
        return test(new DamageFilterContext(source, damage));
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record FilterType(Optional<AmountFilter> amount, Optional<PalladiumHolderSet<DamageType>> damageTypes, Optional<Condition> attacker, Optional<ProjectileFilter> projectile) {

        private static final MapCodec<FilterType> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AmountFilter.CODEC.optionalFieldOf("amount").forGetter(FilterType::amount),
                PalladiumHolderSet.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_types").forGetter(FilterType::damageTypes),
                Condition.CODEC.optionalFieldOf("attacker_conditions").forGetter(FilterType::attacker),
                ProjectileFilter.CODEC.optionalFieldOf("projectile").forGetter(FilterType::projectile)
        ).apply(instance, FilterType::new));

        public static final Codec<FilterType> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("amount", AmountFilter.documentation(provider), "Checks if the damage is of a specified amount.")
                    .addOptional("damage_types", Documented.TYPE_DAMAGE_TYPE_HOLDER_SET, "Checks if the damage is of one of these damage types or tags.")
                    .addOptional("attacker_conditions", Documented.TYPE_CONDITION_LIST, "Checks if the damage came from an entity that meets these conditions.")
                    .addOptional("projectile", ProjectileFilter.documentation(provider), "Checks if the damage was projectile damage, and if the projectile matches.")
                    .buildObject("Damage Filter Types");
        }

        public boolean isEmpty() {
            return amount.isEmpty() && damageTypes.isEmpty() && attacker.isEmpty() && projectile.isEmpty();
        }

        public boolean test(DamageFilterContext context) {
            DamageSource source = context.source();

            if (amount.isPresent() && !amount.get().test(context)) return false;

            if (damageTypes.isPresent() && !damageTypes.get().contains(source.typeHolder())) return false;

            if (attacker.isPresent()) {
                Entity entity = source.getEntity();
                if (entity == null || !attacker.get().test(DataContext.forEntity(entity))) return false;
            }

            if (projectile.isPresent() && !projectile.get().test(context)) return false;

            return true;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record AmountFilter(NumberComparator comparator, Value compareTo) {

        private static final MapCodec<AmountFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NumberComparator.CODEC.fieldOf("comparator").forGetter(AmountFilter::comparator),
                Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(AmountFilter::compareTo)
        ).apply(instance, AmountFilter::new));

        public static final Codec<AmountFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .add("comparator", Documented.TYPE_NUMBER_COMPARATOR, "How the damage amount is compared.")
                    .add("compare_to", Documented.TYPE_FLOAT_VALUE, "Value the damage amount is compared to.")
                    .buildObject("Damage Amount");
        }

        public boolean test(DamageFilterContext context) {
            return comparator.compare(context.amount(), compareTo.getAsFloat(null));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public record ProjectileFilter(Optional<PalladiumHolderSet<EntityType<?>>> types, Condition conditions) {

        private static final MapCodec<ProjectileFilter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PalladiumHolderSet.codec(Registries.ENTITY_TYPE).optionalFieldOf("types").forGetter(ProjectileFilter::types),
                Condition.CODEC.optionalFieldOf("conditions", TrueCondition.INSTANCE).forGetter(ProjectileFilter::conditions)
        ).apply(instance, ProjectileFilter::new));

        public static final Codec<ProjectileFilter> CODEC = MAP_CODEC.codec();

        private static SettingType documentation(HolderLookup.Provider provider) {
            return new CodecDocumentationBuilder<>(MAP_CODEC, provider)
                    .addOptional("types", Documented.TYPE_ENTITY_TYPE_HOLDER_SET, "Entity type or tags the projectile must be.")
                    .addOptional("conditions", Documented.TYPE_CONDITION_LIST, "Condition(s) the projectile entity must meet.")
                    .buildObject("Projectile");
        }

        public boolean test(DamageFilterContext context) {
            DamageSource source = context.source();
            Entity direct = source.getDirectEntity();
            return source.is(DamageTypeTags.IS_PROJECTILE)
                    && direct != null
                    && types.map(set -> set.contains(direct.getType().builtInRegistryHolder())).orElse(true)
                    && conditions.test(DataContext.forEntity(direct));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    public enum Operator {
        AND, OR, NOT;

        public static final Codec<Operator> CODEC = Codec.STRING.xmap(name -> Operator.valueOf(name.toUpperCase(Locale.ROOT)), operator -> operator.name().toLowerCase(Locale.ROOT));

        public boolean test(List<FilterType> filters, DamageFilterContext context) {
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

    private static DamageFilter single(FilterType type) {
        return new DamageFilter(Operator.AND, List.of(type));
    }

    public static DamageFilter ofAttacker(Condition condition) {
        return single(new FilterType(none(), none(), Optional.of(condition), none()));
    }

    public static DamageFilter ofDamageTypes(PalladiumHolderSet<DamageType> types) {
        return single(new FilterType(none(), Optional.of(types), none(), none()));
    }

    public static DamageFilter ofAmount(NumberComparator comparator, Value compareTo) {
        return single(new FilterType(Optional.of(new AmountFilter(comparator, compareTo)), none(), none(), none()));
    }

    public static DamageFilter ofProjectile(Optional<PalladiumHolderSet<EntityType<?>>> types, Condition conditions) {
        return single(new FilterType(none(), none(), none(), Optional.of(new ProjectileFilter(types, conditions))));
    }

}