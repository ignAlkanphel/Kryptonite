package net.alkanphel.kryptonite.power.logic.action.bi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.Space;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class AddVelocityBiAction extends BiAction {

    public static final MapCodec<AddVelocityBiAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.Vec3fValue.CODEC.optionalFieldOf("velocity", KryptoniteCodecs.Vec3fValue.ZERO).forGetter(a -> a.velocity),
            Reference.CODEC.optionalFieldOf("reference", Reference.POSITION).forGetter(a -> a.reference),
            Value.CODEC.optionalFieldOf("set", new StaticValue(false)).forGetter(a -> a.set)
    ).apply(instance, AddVelocityBiAction::new));

    private final KryptoniteCodecs.Vec3fValue velocity;
    private final Reference reference;
    private final Value set;

    public AddVelocityBiAction(KryptoniteCodecs.Vec3fValue velocity, Reference reference, Value set) {
        this.velocity = velocity;
        this.reference = reference;
        this.set = set;
    }

    @Override
    public boolean run(BiActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();

        DataContext dataContext = DataContext.forEntity(actor);

        Vector3f velocityCopy = this.velocity.get(dataContext);
        TriConsumer<Float, Float, Float> method = set.getAsBoolean(dataContext)
                ? target::setDeltaMovement
                : target::push;

        Vec3 referenceVec = reference.apply(actor, target);
        Space.transformVectorToBase(referenceVec, velocityCopy, actor.getYRot(), true); // vector is normalized by method

        method.accept(velocityCopy.x(), velocityCopy.y(), velocityCopy.z());
        target.hurtMarked = true;
        return true;
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.ADD_VELOCITY.get();
    }

    public static class Serializer extends BiActionSerializer<AddVelocityBiAction> {

        @Override
        public MapCodec<AddVelocityBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, AddVelocityBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Add Velocity")
                    .setDescription("Adds or sets the velocity of the target entity, based on the direction from the actor entity to the target entity.")
                    .addOptional("velocity", KryptoniteDocumented.TYPE_VECTOR3f_VALUE, "The amount of velocity to apply to the xyz axis.", Vec3.ZERO)
                    .addOptional("reference", SettingType.enumList(Reference.values()), "Determines whether to use the target entity's 'position' or 'rotation' when calculating the velocity that will be applied to the target entity.", Space.WORLD)
                    .addOptional("set", TYPE_VALUE, "If true, replaces velocity instead of adding to it.", false)
                    .addExampleObject(new AddVelocityBiAction(new KryptoniteCodecs.Vec3fValue(new StaticValue(0F), new StaticValue(0.9F), new StaticValue(0F)), Reference.POSITION, new StaticValue(false)));
        }
    }

    public enum Reference implements StringRepresentable, BiFunction<Entity, Entity, Vec3> {

        POSITION {
            @Override
            public Vec3 apply(Entity actor, Entity target) {
                return target.position().subtract(actor.position());
            }
        },

        ROTATION {
            @Override
            public Vec3 apply(Entity actor, Entity target) {
                float pitch = actor.getXRot();
                float yaw = actor.getYRot();

                float i = 0.017453292F;

                float j = -Mth.sin(yaw * i) * Mth.cos(pitch * i);
                float k = -Mth.sin(pitch * i);
                float l =  Mth.cos(yaw * i) * Mth.cos(pitch * i);

                return new Vec3(j, k, l);
            }
        };

        public static final Codec<Reference> CODEC = StringRepresentable.fromEnum(Reference::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}