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

    public static final Codec<Vec3> VEC3_OBJECT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(Vec3::x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(Vec3::y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(Vec3::z)
    ).apply(instance, Vec3::new));

    public static final Codec<Vec3i> VEC3F_OBJECT_INT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("x", 0).forGetter(Vec3i::getX),
            Codec.INT.optionalFieldOf("y", 0).forGetter(Vec3i::getY),
            Codec.INT.optionalFieldOf("z", 0).forGetter(Vec3i::getZ)
    ).apply(instance, Vec3i::new));

    public static final Codec<Vector3f> VEC3F_OBJECT_FLOAT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("x", 0F).forGetter(Vector3f::x),
            Codec.FLOAT.optionalFieldOf("y", 0F).forGetter(Vector3f::y),
            Codec.FLOAT.optionalFieldOf("z", 0F).forGetter(Vector3f::z)
    ).apply(instance, Vector3f::new));

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