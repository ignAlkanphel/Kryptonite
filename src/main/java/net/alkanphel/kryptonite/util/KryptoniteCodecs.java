package net.alkanphel.kryptonite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionHand;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class KryptoniteCodecs {

    public static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.xmap(string -> InteractionHand.valueOf(string.toUpperCase()), hand -> hand.name().toLowerCase());

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