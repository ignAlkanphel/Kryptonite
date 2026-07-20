package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class GameEventAction extends Action {

    public static final MapCodec<GameEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.GAME_EVENT).fieldOf("game_events").forGetter(a -> a.gameEvents)
    ).apply(instance, GameEventAction::new));

    private final HolderSet<GameEvent> gameEvents;

    public GameEventAction(HolderSet<GameEvent> gameEvents) {
        this.gameEvents = gameEvents;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        for (Holder<GameEvent> holder : gameEvents) {
            entity.gameEvent(holder);
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.GAME_EVENT.get();
    }

    public static class Serializer extends ActionSerializer<GameEventAction> {

        @Override
        public MapCodec<GameEventAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, GameEventAction> builder, HolderLookup.Provider provider) {
            builder.setName("Game Event")
                    .setDescription("Emits the specified game events at the entity's position. See: https://minecraft.wiki/w/Vibration")
                    .add("game_events", KryptoniteDocumented.TYPE_GAME_EVENT_HOLDER_SET, "The game events to emit.")
                    .addExampleObject(new GameEventAction(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.GAME_EVENT, Identifier.withDefaultNamespace("teleport"))), provider.holderOrThrow(ResourceKey.create(Registries.GAME_EVENT, Identifier.withDefaultNamespace("block_destroy"))))));

        }
    }

}