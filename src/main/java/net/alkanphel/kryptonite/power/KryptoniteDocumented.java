package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.util.apoli.BlockUsagePhase;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionResultUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
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

    SettingType TYPE_VALUE_MODIFIER = SettingType.simple("Value Modifier (amount*, operation*)");
    SettingType TYPE_RGB_VALUE = SettingType.simple("(Dynamic) RGB Value");
    SettingType TYPE_JSON_OBJECT = SettingType.simple("JSON Object");

    SettingType TYPE_ITEM_TYPE_HOLDER_SET = SettingType.simple("Item ID(s) / Tag(s)");
    SettingType TYPE_BLOCK_TYPE_HOLDER_SET = SettingType.simple("Block ID(s) / Tag(s)");
    SettingType TYPE_FLUID_TYPE_HOLDER_SET = SettingType.simple("Fluid ID(s) / Tag(s)");
    SettingType TYPE_GAME_EVENT_HOLDER_SET = SettingType.simple("Game Event ID(s) / Tag(s)");
    SettingType TYPE_TIMELINE_HOLDER_SET = SettingType.simple("Timeline ID(s) / Tag(s)");

    SettingType TYPE_SKYBOX = SettingType.enumList(DimensionType.Skybox.values());
    SettingType TYPE_CARDINAL_LIGHTING = SettingType.enumList(CardinalLighting.Type.values());
    SettingType TYPE_FOG_TYPE = SettingType.enumList(FogType.values());
    SettingType TYPE_INTERACTION_HAND = SettingType.enumList(InteractionHand.values());
    SettingType TYPE_INTERACTION_RESULT = SettingType.enumList(InteractionResultUtil.InteractionResultType.values());
    SettingType TYPE_CLIP_CONTEXT_BLOCK = SettingType.enumList(ClipContext.Block.values());
    SettingType TYPE_CLIP_CONTEXT_FLUID = SettingType.enumList(ClipContext.Fluid.values());
    SettingType TYPE_DIRECTION = SettingType.enumList(Direction.values());
    SettingType TYPE_BLOCK_USAGE_PHASE = SettingType.enumList(BlockUsagePhase.values());
    SettingType TYPE_SHAPE = SettingType.enumList(Shape.values());

    CodecDocumentationBuilder<T, R> getDocumentation(HolderLookup.Provider var1);
}