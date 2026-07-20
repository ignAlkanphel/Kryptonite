package net.alkanphel.kryptonite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

public class KryptoniteCodecs {

    public static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.xmap(string -> InteractionHand.valueOf(string.toUpperCase()), hand -> hand.name().toLowerCase());
    public static final Codec<ClipContext.Block> CLIP_CONTEXT_BLOCK_CODEC = Codec.STRING.xmap(string -> ClipContext.Block.valueOf(string.toUpperCase()), block -> block.name().toLowerCase());
    public static final Codec<ClipContext.Fluid> CLIP_CONTEXT_FLUID_CODEC = Codec.STRING.xmap(string -> ClipContext.Fluid.valueOf(string.toUpperCase()), fluid -> fluid.name().toLowerCase());
    public static final Codec<EnumSet<Direction.Axis>> DIRECTION_AXIS_CODEC = ExtraCodecs.compactListCodec(Direction.Axis.CODEC).xmap(list -> list.isEmpty() ? EnumSet.allOf(Direction.Axis.class) : EnumSet.copyOf(list), List::copyOf);

    public record Vec3Value(Value x, Value y, Value z) {
        public static final Codec<Vec3Value> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.optionalFieldOf("x", new StaticValue(0.0D)).forGetter(Vec3Value::x),
                Value.CODEC.optionalFieldOf("y", new StaticValue(0.0D)).forGetter(Vec3Value::y),
                Value.CODEC.optionalFieldOf("z", new StaticValue(0.0D)).forGetter(Vec3Value::z)
        ).apply(instance, Vec3Value::new));

        public static final Vec3Value ZERO = new Vec3Value(new StaticValue(0.0D), new StaticValue(0.0D), new StaticValue(0.0D));

        public Vec3 get(DataContext context) {
            return new Vec3(this.x.getAsDouble(context), this.y.getAsDouble(context), this.z.getAsDouble(context));
        }
    }

    public record Vec3iValue(Value x, Value y, Value z) {
        public static final Codec<Vec3iValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.optionalFieldOf("x", new StaticValue(0)).forGetter(Vec3iValue::x),
                Value.CODEC.optionalFieldOf("y", new StaticValue(0)).forGetter(Vec3iValue::y),
                Value.CODEC.optionalFieldOf("z", new StaticValue(0)).forGetter(Vec3iValue::z)
        ).apply(instance, Vec3iValue::new));

        public static final Vec3iValue ZERO = new Vec3iValue(new StaticValue(0), new StaticValue(0), new StaticValue(0));

        public Vec3i get(DataContext context) {
            return new Vec3i(this.x.getAsInt(context), this.y.getAsInt(context), this.z.getAsInt(context));
        }
    }

    public record Vec3fValue(Value x, Value y, Value z) {
        public static final Codec<Vec3fValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.optionalFieldOf("x", new StaticValue(0.0F)).forGetter(Vec3fValue::x),
                Value.CODEC.optionalFieldOf("y", new StaticValue(0.0F)).forGetter(Vec3fValue::y),
                Value.CODEC.optionalFieldOf("z", new StaticValue(0.0F)).forGetter(Vec3fValue::z)
        ).apply(instance, Vec3fValue::new));

        public static final Vec3fValue ZERO = new Vec3fValue(new StaticValue(0.0F), new StaticValue(0.0F), new StaticValue(0.0F));

        public Vector3f get(DataContext context) {
            return new Vector3f(this.x.getAsFloat(context), this.y.getAsFloat(context), this.z.getAsFloat(context));
        }
    }

    public record RGBValue(Value red, Value green, Value blue) {
        public static final Codec<RGBValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.optionalFieldOf("red", new StaticValue(1.0D)).forGetter(RGBValue::red),
                Value.CODEC.optionalFieldOf("green", new StaticValue(1.0D)).forGetter(RGBValue::green),
                Value.CODEC.optionalFieldOf("blue", new StaticValue(1.0D)).forGetter(RGBValue::blue)
        ).apply(instance, RGBValue::new));

        public static final RGBValue WHITE = new RGBValue(new StaticValue(1.0D), new StaticValue(1.0D), new StaticValue(1.0D));
        public static final RGBValue BLACK = new RGBValue(new StaticValue(0.0D), new StaticValue(0.0D), new StaticValue(0.0D));

        public float red(DataContext context) {
            return (float) this.red.getAsDouble(context);
        }

        public float green(DataContext context) {
            return (float) this.green.getAsDouble(context);
        }

        public float blue(DataContext context) {
            return (float) this.blue.getAsDouble(context);
        }
    }

    public record RGBAValue(Value red, Value green, Value blue, Value alpha) {
        public static final Codec<RGBAValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.optionalFieldOf("red", new StaticValue(1.0D)).forGetter(RGBAValue::red),
                Value.CODEC.optionalFieldOf("green", new StaticValue(1.0D)).forGetter(RGBAValue::green),
                Value.CODEC.optionalFieldOf("blue", new StaticValue(1.0D)).forGetter(RGBAValue::blue),
                Value.CODEC.optionalFieldOf("alpha", new StaticValue(1.0D)).forGetter(RGBAValue::alpha)
        ).apply(instance, RGBAValue::new));

        public static final RGBAValue WHITE = new RGBAValue(new StaticValue(1.0D), new StaticValue(1.0D), new StaticValue(1.0D), new StaticValue(1.0D));
        public static final RGBAValue BLACK = new RGBAValue(new StaticValue(0.0D), new StaticValue(0.0D), new StaticValue(0.0D), new StaticValue(1.0D));

        public float red(DataContext context) {
            return (float) this.red.getAsDouble(context);
        }

        public float green(DataContext context) {
            return (float) this.green.getAsDouble(context);
        }

        public float blue(DataContext context) {
            return (float) this.blue.getAsDouble(context);
        }

        public float alpha(DataContext context) {
            return (float) this.alpha.getAsDouble(context);
        }
    }

}