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

public class TargetActionBiAction extends BiAction {

    public static final MapCodec<TargetActionBiAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.actions)
    ).apply(instance, TargetActionBiAction::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TargetActionBiAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Action.LIST_CODEC), a -> a.actions,
            TargetActionBiAction::new
    );

    private final List<Action> actions;

    public TargetActionBiAction(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> action() {
        return this.actions;
    }

    @Override
    public boolean run(BiActionContext context) {
        if (context.target() == null) return false;
        return Action.runList(actions, DataContext.forEntity(context.target()));
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.TARGET_ACTION.get();
    }

    public static class Serializer extends BiActionSerializer<TargetActionBiAction> {

        @Override
        public MapCodec<TargetActionBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, TargetActionBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Target Action")
                    .setDescription("Runs actions on the target entity.")
                    .add("actions", TYPE_ACTION_LIST, "The actions to run on the target entity.")
                    .addExampleObject(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands(List.of("say I am the target!"))))));
        }
    }

}