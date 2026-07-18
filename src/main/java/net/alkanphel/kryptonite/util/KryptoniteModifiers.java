package net.alkanphel.kryptonite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class KryptoniteModifiers {

    public static final Codec<List<ValueModifier>> VALUE_MODIFIERS_CODEC = ExtraCodecs.compactListCodec(ValueModifier.CODEC);

    public record ValueModifier(Value amount, Operation operation) {
        public static final Codec<ValueModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.fieldOf("amount").forGetter(ValueModifier::amount),
                Operation.CODEC.fieldOf("operation").forGetter(ValueModifier::operation)
        ).apply(instance, ValueModifier::new));
    }

    public enum Operation implements StringRepresentable {
        ADD_BASE_EARLY(Phase.BASE, 0, (values, base, total) -> values.stream().reduce(total, Double::sum)),
        MULTIPLY_BASE_ADDITIVE(Phase.BASE, 100, (values, base, total) -> total + (base * values.stream().reduce(0.0, Double::sum))),
        MULTIPLY_BASE_MULTIPLICATIVE(Phase.BASE, 200, (values, base, total) -> total * (1.0 + values.stream().reduce(0.0, Double::sum))),
        ADD_BASE_LATE(Phase.BASE, 300, (values, base, total) -> values.stream().reduce(total, Double::sum)),
        MIN_BASE(Phase.BASE, 400, (values, base, total) -> values.stream().reduce(total, Math::max)),
        MAX_BASE(Phase.BASE, 500, (values, base, total) -> values.stream().reduce(total, Math::min)),
        SET_BASE(Phase.BASE, 600, (values, base, total) -> values.stream().reduce(total, (a, b) -> b)),

        ADD_TOTAL_EARLY(Phase.TOTAL, 0, (values, totalBase, total) -> values.stream().reduce(total, Double::sum)),
        MULTIPLY_TOTAL_ADDITIVE(Phase.TOTAL, 100, (values, totalBase, total) -> total + (totalBase * values.stream().reduce(0.0, Double::sum))),
        MULTIPLY_TOTAL_MULTIPLICATIVE(Phase.TOTAL, 200, (values, totalBase, total) -> total * (1.0 + values.stream().reduce(0.0, Double::sum))),
        ADD_TOTAL_LATE(Phase.TOTAL, 300, (values, totalBase, total) -> values.stream().reduce(total, Double::sum)),
        MIN_TOTAL(Phase.TOTAL, 400, (values, totalBase, total) -> values.stream().reduce(total, Math::max)),
        MAX_TOTAL(Phase.TOTAL, 500, (values, totalBase, total) -> values.stream().reduce(total, Math::min)),
        SET_TOTAL(Phase.TOTAL, 600, (values, totalBase, total) -> values.stream().reduce(total, (a, b) -> b));

        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final Phase phase;
        private final int order;
        private final TriFunction function;
        private final String name;

        Operation(Phase phase, int order, TriFunction function) {
            this.phase = phase;
            this.order = order;
            this.function = function;
            this.name = name().toLowerCase();
        }

        public Phase getPhase() {
            return phase;
        }

        public int getOrder() {
            return order;
        }

        public double apply(List<Double> values, double base, double current) {
            return function.apply(values, base, current);
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        @FunctionalInterface
        private interface TriFunction {
            double apply(List<Double> values, double base, double current);
        }

        public enum Phase {
            BASE, TOTAL
        }
    }

    public static int applyModifiers(int base, Collection<ValueModifier> modifiers, DataContext context) {
        if (modifiers.isEmpty()) return base;

        return (int) applyModifiers((double) base, modifiers, context);
    }

    public static float applyModifiers(float base, Collection<ValueModifier> modifiers, DataContext context) {
        if (modifiers.isEmpty()) return base;

        return (float) applyModifiers((double) base, modifiers, context);
    }

    public static double applyModifiers(double base, Collection<ValueModifier> modifiers, DataContext context) {
        if (modifiers.isEmpty()) return base;

        Map<Operation, List<ValueModifier>> buckets = modifiers
                .stream()
                .sorted(Comparator.comparingInt(m -> m.operation().getOrder()))
                .collect(Collectors.groupingBy(ValueModifier::operation, LinkedHashMap::new, Collectors.toList()));

        double currentBase = base;
        double currentValue = base;

        Operation.Phase previousPhase = Operation.Phase.BASE;

        for (var entry : buckets.entrySet()) {
            Operation operation = entry.getKey();
            Operation.Phase currentPhase = operation.getPhase();

            if (currentPhase != previousPhase) {
                previousPhase = currentPhase;
                currentBase = currentValue;
            }

            List<Double> values = entry.getValue()
                    .stream()
                    .map(m -> m.amount().getAsDouble(context))
                    .toList();

            currentValue = operation.apply(values, currentBase, currentValue);
        }

        return currentValue;
    }

}