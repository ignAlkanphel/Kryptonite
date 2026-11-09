package net.alkanphel.kryptonite.power.logic;

import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum BlockLogicEnvironment {

    BOTH,
    SERVER,
    CLIENT;

    private static final Set<Identifier> WARNED = ConcurrentHashMap.newKeySet();

    public boolean matches(BlockConditionContext context) {
        return matches(context.level());
    }

    public boolean matches(Level level) {
        return switch (this) {
            case BOTH -> true;
            case SERVER -> !level.isClientSide();
            case CLIENT -> level.isClientSide();
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
        };

        AddonPackLog.warning(message, typeId);
    }

}