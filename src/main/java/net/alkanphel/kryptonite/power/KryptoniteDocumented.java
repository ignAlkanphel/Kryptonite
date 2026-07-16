package net.alkanphel.kryptonite.power;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.DimensionType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;

public interface KryptoniteDocumented<T, R extends T> {

    SettingType TYPE_BI_CONDITION_LIST = SettingType.listOrPrimitive("Bi Condition");
    SettingType TYPE_BLOCK_CONDITION_LIST = SettingType.listOrPrimitive("Block Condition");
    SettingType TYPE_ITEM_CONDITION_LIST = SettingType.listOrPrimitive("Item Condition");
    SettingType TYPE_DIMENSION_CONDITION_LIST = SettingType.listOrPrimitive("Dimension Condition");
    SettingType TYPE_DAMAGE_CONDITION_LIST = SettingType.listOrPrimitive("Damage Condition");
    SettingType TYPE_BI_ACTION_LIST = SettingType.listOrPrimitive("Bi Action");
    SettingType TYPE_BLOCK_ACTION_LIST = SettingType.listOrPrimitive("Block Action");
    SettingType TYPE_ITEM_ACTION_LIST = SettingType.listOrPrimitive("Item Action");

    SettingType TYPE_JSON_OBJECT = SettingType.simple("JSON Object");

    SettingType TYPE_ITEM_TYPE_HOLDER_SET = SettingType.simple("Item ID(s) / Tag(s)");
    SettingType TYPE_BLOCK_TYPE_HOLDER_SET = SettingType.simple("Block ID(s) / Tag(s)");
    SettingType TYPE_TIMELINE_HOLDER_SET = SettingType.simple("Timeline Type ID(s) / Tag(s)");

    SettingType TYPE_SKYBOX = SettingType.enumList(DimensionType.Skybox.values());
    SettingType TYPE_CARDINAL_LIGHTING = SettingType.enumList(CardinalLighting.Type.values());
    SettingType TYPE_INTERACTION_HAND = SettingType.enumList(InteractionHand.values());

    CodecDocumentationBuilder<T, R> getDocumentation(HolderLookup.Provider var1);
}