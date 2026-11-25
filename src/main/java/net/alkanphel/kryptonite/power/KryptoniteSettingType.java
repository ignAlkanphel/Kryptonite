package net.alkanphel.kryptonite.power;

import net.threetag.palladium.documentation.SettingType;

public abstract class KryptoniteSettingType extends SettingType {

    public static SettingType intValueRange(int min, int max) {
        return simple("Integer / Value (" + min + " ~ " + max + ")");
    }

    public static SettingType floatValueRange(float min, float max) {
        return simple("Float / Value (" + min + " ~ " + max + ")");
    }

    public static SettingType doubleValueRange(double min, double max) {
        return simple("Double / Value (" + min + " ~ " + max + ")");
    }

    public static SettingType longValueRange(long min, long max) {
        return simple("Long / Value (" + min + " ~ " + max + ")");
    }

}