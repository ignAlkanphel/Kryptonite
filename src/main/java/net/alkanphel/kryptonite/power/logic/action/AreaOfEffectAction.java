package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AreaOfEffectAction extends Action {

    public static final MapCodec<AreaOfEffectAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.fieldOf("bientity_actions").forGetter(a -> a.biActions),
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(a -> a.biConditions),
            Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(a -> a.shape),
            Value.CODEC.optionalFieldOf("radius", new StaticValue(16)).forGetter(a -> a.radius),
            Value.CODEC.optionalFieldOf("include_actor", new StaticValue(false)).forGetter(a -> a.includeActor)
    ).apply(instance, AreaOfEffectAction::new));

    private final List<BiAction> biActions;
    private final Optional<BiCondition> biConditions;
    private final Shape shape;
    private final Value radius, includeActor;

    public AreaOfEffectAction(List<BiAction> biActions, Optional<BiCondition> biConditions, Shape shape, Value radius, Value includeActor) {
        this.biActions = biActions;
        this.biConditions = biConditions;
        this.shape = shape;
        this.radius = radius;
        this.includeActor = includeActor;
    }

    @Override
    public boolean run(DataContext context) {
        var actor = context.getEntity();
        if (actor == null) return false;

        shape.getEntities(actor.level(), actor.getPosition(1.0F), Math.max(0, radius.getAsDouble(context)))
                .stream()
                .filter(target -> includeActor.getAsBoolean(context) || !Objects.equals(actor, target))
                .filter(target -> biConditions.map(c -> c.test(actor, target)).orElse(true))
                .map(target -> new BiActionContext(actor, target))
                .forEach(biCtx -> BiAction.runList(biActions, biCtx));

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.AREA_OF_EFFECT.get();
    }

    public static class Serializer extends ActionSerializer<AreaOfEffectAction> {

        @Override
        public MapCodec<AreaOfEffectAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, AreaOfEffectAction> builder, HolderLookup.Provider provider) {
            builder.setName("Area of Effect")
                    .setDescription("Runs bi actions on all entities within a specified radius. In the context of this action, the \"actor\" is the entity that ran the action & the \"targets\" are the entities within the specified radius.")
                    .add("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on each entity in range.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, filters which entities in range the actions run on.")
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "The shape of the area.", Shape.CUBE)
                    .addOptional("radius", KryptoniteSettingType.doubleValueRange(0, Integer.MAX_VALUE), "The radius of the area.", 16)
                    .addOptional("include_actor", TYPE_VALUE, "If the \"actor\" should be included as a target.", false)
                    .addExampleObject(new AreaOfEffectAction(List.of(new TargetActionBiAction(List.of(new SetOnFireAction(new StaticValue(60))))), Optional.empty(), Shape.CUBE, new StaticValue(5), new StaticValue(false)));
        }
    }

}