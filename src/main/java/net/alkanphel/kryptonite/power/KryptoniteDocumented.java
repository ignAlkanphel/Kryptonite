package net.alkanphel.kryptonite.power;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.DimensionType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;

public interface KryptoniteDocumented<T, R extends T> {

    SettingType TYPE_DIMENSION_CONDITION_LIST = SettingType.listOrPrimitive("Dimension Condition");

    SettingType TYPE_JSON_OBJECT = SettingType.simple("JSON Object");

    SettingType TYPE_TIMELINE_HOLDER_SET = SettingType.simple("Timeline Type ID(s) / Tag(s)");

    SettingType TYPE_SKYBOX = SettingType.enumList(DimensionType.Skybox.values());
    SettingType TYPE_CARDINAL_LIGHTING = SettingType.enumList(CardinalLighting.Type.values());

    CodecDocumentationBuilder<T, R> getDocumentation(HolderLookup.Provider var1);
}