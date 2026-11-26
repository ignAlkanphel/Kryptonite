package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.Comparator;
import java.util.List;

public class PreventSleepingAbility extends Ability {

    public static final MapCodec<PreventSleepingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(ab -> ab.blockFilter),
            Codec.BOOL.optionalFieldOf("set_spawn", false).forGetter(ab -> ab.setSpawn),
            Codec.STRING.optionalFieldOf("message", "Sleep is being prevented via ability!").forGetter(ab -> ab.message),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("priority", 0).forGetter(ab -> ab.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventSleepingAbility::new));

    public final BlockFilter blockFilter;
    public final boolean setSpawn;
    public final String message;
    public final int priority;

    public PreventSleepingAbility(BlockFilter blockFilter, boolean setSpawn, String message, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockFilter = blockFilter;
        this.setSpawn = setSpawn;
        this.message = message;
        this.priority = priority;
    }

    public boolean doesPrevent(Level level, BlockPos pos) {
        return blockFilter.test(level, pos);
    }

    private static List<PreventSleepingAbility> getSortedAbilities(ServerPlayer player) {
        return AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLEEPING.get())
                .stream()
                .map(AbilityInstance::getAbility)
                .sorted(Comparator.comparingInt((PreventSleepingAbility ab) -> ab.priority).reversed().thenComparing(Comparator.comparingInt((PreventSleepingAbility ab) -> ab.setSpawn ? 1 : 0).reversed()))
                .toList();
    }

    public static boolean isSleepPrevented(ServerPlayer player, BlockPos bedPos) {
        return getSortedAbilities(player).stream().anyMatch(ab -> ab.doesPrevent(player.level(), bedPos));
    }

    public static boolean isSpawnPrevented(ServerPlayer player, BlockPos bedPos) {
        return getSortedAbilities(player).stream()
                .filter(ab -> ab.doesPrevent(player.level(), bedPos))
                .findFirst()
                .map(ab -> !ab.setSpawn).orElse(false);
    }

    public static String getSleepMessage(ServerPlayer player, BlockPos bedPos) {
        return getSortedAbilities(player).stream()
                .filter(ab -> ab.doesPrevent(player.level(), bedPos))
                .map(ab -> ab.message)
                .findFirst().orElse("Sleep is being prevented via ability!");
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_SLEEPING.get();
    }

    public static class Serializer extends AbilitySerializer<PreventSleepingAbility> {

        @Override
        public MapCodec<PreventSleepingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventSleepingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows preventing the player from sleeping.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filters(s) the block must pass for sleep to be prevented.")
                    .addOptional("set_spawn", TYPE_BOOLEAN, "If the spawn point of the player is set upon using a bed while being prevented.", false)
                    .addOptional("message", TYPE_STRING, "The message shown upon sleep being prevented.", "Sleep is being prevented via ability!")
                    .addOptional("priority", TYPE_INT, "The priority of which this ability will prevent the player to sleep, set their spawn and display a message. Abilities of higher priority & \"set_spawn\" true, are prioritized.", 0)
                    .addExampleObject(new PreventSleepingAbility(BlockFilter.ofBlock((new PalladiumHolderSet.Direct<>(provider.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.BEDS)))), false, "You cannot sleep as an insomniac!", 5, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}