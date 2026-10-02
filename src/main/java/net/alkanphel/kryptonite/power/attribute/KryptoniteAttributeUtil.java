package net.alkanphel.kryptonite.power.attribute;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class KryptoniteAttributeUtil {

	public static final double MIN_SCALE = 0.0625;
	public static final double MAX_SCALE = 16.0;

    public static double getModelDepth(LivingEntity entity, double original) {
        return Mth.clamp(original * getScaleAttributeModifierValue(entity, KryptoniteAttributes.MODEL_DEPTH), MIN_SCALE, MAX_SCALE);
    }

    public static double getScaleAttributeModifierValue(LivingEntity entity, Holder<Attribute> attribute) {
        double value = 1.0F;

        if (entity == null) return value;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) value = instance.getValue();

        return Mth.clamp(value, MIN_SCALE, MAX_SCALE);
    }

}