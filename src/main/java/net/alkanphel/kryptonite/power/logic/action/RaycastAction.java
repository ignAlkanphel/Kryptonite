package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.alkanphel.kryptonite.util.apoli.Space;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RaycastAction extends Action {

    private record ActionOptions(List<Action> actionAtHit, List<Action> actionAlongRay, Optional<Double> actionHitOffset, double actionStep, Value actionAlongRayOnlyOnHit) {
        static final MapCodec<ActionOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Action.LIST_CODEC.optionalFieldOf("action_at_hit", List.of()).forGetter(ActionOptions::actionAtHit),
                Action.LIST_CODEC.optionalFieldOf("action_along_ray", List.of()).forGetter(ActionOptions::actionAlongRay),
                Codec.DOUBLE.optionalFieldOf("action_hit_offset").forGetter(ActionOptions::actionHitOffset),
                Codec.DOUBLE.optionalFieldOf("action_step", 1.0D).forGetter(ActionOptions::actionStep),
                Value.CODEC.optionalFieldOf("action_along_ray_only_on_hit", new StaticValue(false)).forGetter(ActionOptions::actionAlongRayOnlyOnHit)
        ).apply(instance, ActionOptions::new));
    }

    public static final MapCodec<RaycastAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("pre_actions", List.of()).forGetter(a -> a.preActions),
            Action.LIST_CODEC.optionalFieldOf("hit_actions", List.of()).forGetter(a -> a.hitActions),
            Action.LIST_CODEC.optionalFieldOf("miss_actions", List.of()).forGetter(a -> a.missActions),
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(ab -> ab.biEntityConditions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            KryptoniteCodecs.CLIP_CONTEXT_BLOCK_CODEC.optionalFieldOf("shape_type", ClipContext.Block.OUTLINE).forGetter(a -> a.shapeType),
            KryptoniteCodecs.CLIP_CONTEXT_FLUID_CODEC.optionalFieldOf("fluid_handling", ClipContext.Fluid.ANY).forGetter(a -> a.fluidHandling),
            Vec3.CODEC.optionalFieldOf("direction").forGetter(a -> a.direction),
            Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(a -> a.space),
            Codec.DOUBLE.optionalFieldOf("entity_distance").forGetter(a -> a.entityDistance),
            Codec.DOUBLE.optionalFieldOf("block_distance").forGetter(a -> a.blockDistance),
            Codec.DOUBLE.optionalFieldOf("distance").forGetter(a -> a.distance),
            ActionOptions.CODEC.forGetter(a -> new ActionOptions(a.actionAtHit, a.actionAlongRay, a.actionHitOffset, a.actionStep, a.actionAlongRayOnlyOnHit)),
            Value.CODEC.optionalFieldOf("include_entities", new StaticValue(true)).forGetter(a -> a.includeEntities),
            Value.CODEC.optionalFieldOf("include_blocks", new StaticValue(true)).forGetter(a -> a.includeBlocks)
    ).apply(instance, (preActions, hitActions, missActions, biEntityConditions, biEntityActions, blockActions, shapeType, fluidHandling, direction, space, entityDistance, blockDistance, distance, actOpts, includeEntities, includeBlocks) ->
            new RaycastAction(preActions, hitActions, missActions, biEntityConditions, biEntityActions, blockActions, shapeType, fluidHandling, direction, space, entityDistance, blockDistance, distance, actOpts.actionAtHit(), actOpts.actionAlongRay(), actOpts.actionHitOffset(), actOpts.actionStep(), actOpts.actionAlongRayOnlyOnHit(), includeEntities, includeBlocks)));

    private final List<Action> preActions, hitActions, missActions;
    private final Optional<BiCondition> biEntityConditions;
    private final List<BiAction> biEntityActions;
    private final List<BlockAction> blockActions;
    private final ClipContext.Block shapeType;
    private final ClipContext.Fluid fluidHandling;
    private final Optional<Vec3> direction;
    private final Space space;
    private final Optional<Double> entityDistance, blockDistance, distance;
    private final List<Action> actionAtHit, actionAlongRay;
    private final Optional<Double> actionHitOffset;
    private final double actionStep;
    private final Value actionAlongRayOnlyOnHit, includeEntities, includeBlocks;

    public RaycastAction(List<Action> preActions, List<Action> hitActions, List<Action> missActions, Optional<BiCondition> biEntityConditions, List<BiAction> biEntityActions, List<BlockAction> blockActions, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling, Optional<Vec3> direction, Space space, Optional<Double> entityDistance, Optional<Double> blockDistance, Optional<Double> distance, List<Action> actionAtHit, List<Action> actionAlongRay, Optional<Double> actionHitOffset, double actionStep, Value actionAlongRayOnlyOnHit, Value includeEntities, Value includeBlocks) {
        this.preActions = preActions;
        this.hitActions = hitActions;
        this.missActions = missActions;
        this.biEntityConditions = biEntityConditions;
        this.biEntityActions = biEntityActions;
        this.blockActions = blockActions;
        this.shapeType = shapeType;
        this.fluidHandling = fluidHandling;
        this.direction = direction;
        this.space = space;
        this.entityDistance = entityDistance;
        this.blockDistance = blockDistance;
        this.distance = distance;
        this.actionAtHit = actionAtHit;
        this.actionAlongRay = actionAlongRay;
        this.actionHitOffset = actionHitOffset;
        this.actionStep = actionStep;
        this.actionAlongRayOnlyOnHit = actionAlongRayOnlyOnHit;
        this.includeEntities = includeEntities;
        this.includeBlocks = includeBlocks;
    }

    @Override
    public boolean run(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        if (!preActions.isEmpty()) Action.runList(preActions, DataContext.forEntity(entity));

        boolean success = false;

        HitResult result = null;
        Vec3 directionVec = this.direction
                .map(self -> this.transformDirection(entity, self))
                .orElseGet(() -> entity.getViewVector(1.0F));

        Vec3 origin = MiscUtil.getPoseDependentEyePos(entity);
        Vec3 destination = this.distance.map(directionVec::scale).orElse(null);

        if (this.includeEntities.getAsBoolean(context)) {
            destination = origin.add(directionVec.scale(this.getEntityReach(entity)));
            result = this.entityRaycast(entity, origin, destination);
        }

        if (this.includeBlocks.getAsBoolean(context)) {
            Vec3 blockDestination = origin.add(directionVec.scale(this.getBlockReach(entity)));
            BlockHitResult blockResult = this.blockRaycast(entity, origin, blockDestination);

            if (blockResult.getType() != HitResult.Type.MISS && overrideHitResult(entity, result, blockResult)) {
                destination = blockDestination;
                result = blockResult;
            }
        }

        if (result != null && result.getType() != HitResult.Type.MISS) {
            success = true;

            if (!actionAtHit.isEmpty()) {
                Offset offset = this.getOffset(entity, result, directionVec);
                Vec3 hitPos = offset.apply(result.getLocation());

                this.executeActionsAtHit(entity, hitPos);
            }
        }

        if (destination != null && !actionAlongRay.isEmpty() && (!actionAlongRayOnlyOnHit.getAsBoolean(context) || success)) {
            this.executeActionsAlongRay(entity, origin, success ? result.getLocation() : destination);
        }

        if (success) {
            switch (result) {
                case BlockHitResult blockResult -> {
                    if (!blockActions.isEmpty()) BlockAction.runList(blockActions, entity.level(), blockResult.getBlockPos(), Optional.of(blockResult.getDirection()));
                }
                case EntityHitResult entityResult -> {
                    if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, entity, entityResult.getEntity());
                }
                default -> {
                    //  No-op; unsupported
                }
            }

            if (!hitActions.isEmpty()) Action.runList(hitActions, DataContext.forEntity(entity));
        }

        else {
            if (!missActions.isEmpty()) Action.runList(missActions, DataContext.forEntity(entity));
        }

        return success;
    }

    private record Offset(Vec3 direction, double amount) implements UnaryOperator<Vec3> {

        @Override
        public Vec3 apply(Vec3 vec) {
            return vec.subtract(direction().scale(amount()));
        }
    }

    private EntityHitResult entityRaycast(Entity caster, Vec3 origin, Vec3 destination) {
        Vec3 ray = destination.subtract(origin);
        AABB box = caster.getBoundingBox().expandTowards(ray).inflate(1.0D);

        Predicate<Entity> intersectPredicate = EntitySelector.NO_SPECTATORS
                .and(intersected -> biEntityConditions.map(condition -> condition.test(caster, intersected)).orElse(true));

        return ProjectileUtil.getEntityHitResult(caster, origin, destination, box, intersectPredicate, ray.lengthSqr());
    }

    private BlockHitResult blockRaycast(Entity caster, Vec3 origin, Vec3 destination) {
        ClipContext context = new ClipContext(origin, destination, shapeType, fluidHandling, caster);
        return caster.level().clip(context);
    }

    private Vec3 transformDirection(Entity entity, Vec3 direction) {
        Vector3f normalizedDirection = new Vector3f((float) direction.x(), (float) direction.y(), (float) direction.z()).normalize();
        space.toGlobal(normalizedDirection, entity);

        return new Vec3(normalizedDirection);
    }

    private Offset getOffset(Entity entity, HitResult hitResult, Vec3 direction) {
        if (actionHitOffset.isPresent()) {
            return new Offset(direction, actionHitOffset.get());
        }

        else {
            Vec3 offsetDirection = direction;
            double offset = 0.0D;

            if (hitResult instanceof BlockHitResult blockResult) {
                Direction hitSide = blockResult.getDirection();

                switch (hitSide) {
                    case DOWN ->
                            offset = entity.getBbHeight();
                    case UP ->
                            offset = 0;
                    default -> {
                        double offsetX = hitSide.getStepX();
                        double offsetY = hitSide.getStepY();
                        double offsetZ = hitSide.getStepZ();

                        offset = entity.getBbWidth() / 2;
                        offsetDirection = new Vec3(offsetX, offsetY, offsetZ).reverse();
                    }
                }
            }

            offset += 0.05;
            return new Offset(offsetDirection, offset);
        }
    }

    private static boolean overrideHitResult(Entity caster, @Nullable HitResult prev, HitResult next) {
        return prev == null
                || prev.getType() == HitResult.Type.MISS
                || prev.distanceTo(caster) > next.distanceTo(caster);
    }

    private double getEntityReach(Entity entity) {
        return entityDistance
                .or(() -> distance)
                .orElseGet(() -> MiscUtil.getAttributeValueOrElse(entity, Attributes.ENTITY_INTERACTION_RANGE, 1.0));
    }

    private double getBlockReach(Entity entity) {
        return blockDistance
                .or(() -> distance)
                .orElseGet(() -> MiscUtil.getAttributeValueOrElse(entity, Attributes.BLOCK_INTERACTION_RANGE, 1.0));
    }

    private void executeActionsAlongRay(Entity entity, Vec3 origin, Vec3 destination) {
        if (actionAlongRay.isEmpty()) return;

        Vec3 direction = destination.subtract(origin).normalize();
        double distance = origin.distanceTo(destination);

        for (double steps = 0; steps < distance; steps += actionStep) {
            Vec3 offsetPos = direction.scale(steps);
            Vec3 newPos = origin.add(offsetPos);

            DataContext context = DataContext.forEntity(entity).with(DataContextKeys.BLOCK_POS, BlockPos.containing(newPos));
            Action.runList(actionAlongRay, context);
        }
    }

    private void executeActionsAtHit(Entity entity, Vec3 hitPos) {
        if (actionAtHit.isEmpty()) return;

        DataContext context = DataContext.forEntity(entity).with(DataContextKeys.BLOCK_POS, BlockPos.containing(hitPos));
        Action.runList(actionAtHit, context);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.RAYCAST.get();
    }

    public static class Serializer extends ActionSerializer<RaycastAction> {

        @Override
        public MapCodec<RaycastAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, RaycastAction> builder, HolderLookup.Provider provider) {
            builder.setName("Raycast")
                    .setDescription("Performs a raycast in the direction the entity is looking. In the context of this action, the \"actor\" is the entity that ran this action & the \"target\" is the entity hit by the raycast.")
                    .addOptional("pre_actions", TYPE_ACTION_LIST, "The actions to run before the raycast is performed.")
                    .addOptional("hit_actions", TYPE_ACTION_LIST, "The actions to run when the raycast hits something.")
                    .addOptional("miss_actions", TYPE_ACTION_LIST, "The actions to run when the raycast misses.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only entities fulfilling these conditions will be considered as valid raycast targets.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both the \"actor\" or \"target\" entities.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "If specified, these action will be run on the block the raycast has hit.")
                    .addOptional("shape_type", KryptoniteDocumented.TYPE_CLIP_CONTEXT_BLOCK, "How the raycast handles blocks.", ClipContext.Block.OUTLINE)
                    .addOptional("fluid_handling", KryptoniteDocumented.TYPE_CLIP_CONTEXT_FLUID, "How the raycast handles fluids.", ClipContext.Fluid.ANY)
                    .addOptional("direction", TYPE_VECTOR3, "If specified, determines the direction of the raycast. Otherwise, defaults to the direction at the entity is facing (basically \"local\").")
                    .addOptional("space", KryptoniteDocumented.TYPE_SPACE, "How the direction will be calculated. Only used if direction is specified.", Space.WORLD)
                    .addOptional("entity_distance", TYPE_DOUBLE, "The raycast distance for entities when the \"entity\" field is true. If this field is omitted, uses the \"distance\" field & ENTITY_INTERACTION_RANGE attribute.")
                    .addOptional("block_distance", TYPE_DOUBLE, "The raycast distance for blocks when the 'blocks' field is true. If this field is omitted, uses the \"distance\" field & BLOCK_INTERACTION_RANGE attribute.")
                    .addOptional("distance", TYPE_DOUBLE, "The maximum distance the raycast will travel.")
                    .addOptional("action_at_hit", TYPE_ACTION_LIST, "The actions to run upon the block/entity the raycast has hit.")
                    .addOptional("action_hit_offset", TYPE_DOUBLE, "The offset of the actions specified in the \"action_at_hit\" field.")
                    .addOptional("action_along_ray", TYPE_ACTION_LIST, "The actions to run for each step of the raycast.")
                    .addOptional("action_step", TYPE_DOUBLE, "The step size of the raycast (in blocks).", 1.0D)
                    .addOptional("action_along_ray_only_on_hit", TYPE_VALUE, "If true, the \"action_along_ray\" field will run those actions ONLY if the raycast hits blocks/entities.", false)
                    .addOptional("include_entities", TYPE_VALUE, "If true, the raycast will include entities.", true)
                    .addOptional("include_blocks", TYPE_VALUE, "If true, the raycast will include blocks.", true)
                    .addExampleObject(new RaycastAction(List.of(new RunCommandAction(new ParsedCommands(List.of("say pre action!")))), List.of(new RunCommandAction(new ParsedCommands(List.of("say hit action!")))), List.of(new RunCommandAction(new ParsedCommands(List.of("say miss action!")))), Optional.empty(), List.of(), List.of(), ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, Optional.empty(), Space.WORLD, Optional.empty(), Optional.empty(), Optional.of(16.0D), List.of(new RunCommandAction(new ParsedCommands("summon pig"))), List.of(new RunCommandAction(new ParsedCommands("say COMMAND ALONG RAY"))), Optional.empty(), 1.0D, new StaticValue(true), new StaticValue(true), new StaticValue(true)));
        }
    }

}