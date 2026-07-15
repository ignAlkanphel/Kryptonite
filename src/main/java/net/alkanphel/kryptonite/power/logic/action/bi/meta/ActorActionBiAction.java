package net.alkanphel.kryptonite.power.logic.action.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActorActionBiAction extends BiAction {

    public static final MapCodec<ActorActionBiAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.actions)
    ).apply(instance, ActorActionBiAction::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActorActionBiAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Action.LIST_CODEC), a -> a.actions,
            ActorActionBiAction::new
    );

    private final List<Action> actions;

    public ActorActionBiAction(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> action() {
        return this.actions;
    }

    @Override
    public boolean run(BiActionContext context) {
        if (context.actor() == null) return false;
        return Action.runList(actions, DataContext.forEntity(context.actor()));
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.ACTOR_ACTION.get();
    }

    public static class Serializer extends BiActionSerializer<ActorActionBiAction> {

        @Override
        public MapCodec<ActorActionBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, ActorActionBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Actor Action")
                    .setDescription("Runs actions on the actor entity.")
                    .add("actions", TYPE_ACTION_LIST, "Actions to run on the actor.")
                    .addExampleObject(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands(List.of("say I am the actor!"))))));
        }
    }

}