package net.alkanphel.kryptonite.power.logic;

import net.alkanphel.kryptonite.power.logic.context.BiContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum BiLogicEnvironment {

    BOTH,
    SERVER,
    CLIENT,
    SERVER_OR_LOCAL_PLAYER;

    private static final Set<Identifier> WARNED = ConcurrentHashMap.newKeySet();

    public boolean matches(BiContext context) {
        if (this == BOTH) {
            return true;
        }

        var level = getLevel(context);

        if (level == null) {
            return true;
        }

        return switch (this) {
            case BOTH -> true;
            case SERVER -> !level.isClientSide();
            case CLIENT -> level.isClientSide();
            case SERVER_OR_LOCAL_PLAYER -> {
                if (!level.isClientSide()) {
                    yield true;
                }

                var actor = context.actor();
                var target = context.target();

                yield actor == Palladium.PROXY.getClientPlayer() || target == Palladium.PROXY.getClientPlayer();
            }
        };
    }

    public void warnMismatch(Identifier typeId) {
        if (this == BOTH || !WARNED.add(typeId)) {
            return;
        }

        var message = switch (this) {
            case BOTH -> null;
            case SERVER -> "'{}' only works on the server, but was used on the client. It will not return correct results there.";
            case CLIENT -> "'{}' only works on the client, but was used on the server. It will not return correct results there.";
            case SERVER_OR_LOCAL_PLAYER -> "'{}' only works on the server or for the local player on the client, but was used for another entity on the client. It will not return correct results there.";
        };

        AddonPackLog.warning(message, typeId);
    }

    @Nullable
    private static Level getLevel(BiContext context) {
        var actor = context.actor();

        if (actor != null) {
            return actor.level();
        }

        var target = context.target();
        return target != null ? target.level() : null;
    }

}