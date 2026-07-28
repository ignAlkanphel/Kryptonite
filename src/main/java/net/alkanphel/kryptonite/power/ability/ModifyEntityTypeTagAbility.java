package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.alkanphel.kryptonite.network.p2c.S2CSyncEntityTypeTagCache;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyEntityTypeTagAbility extends Ability {

    private static final Map<Identifier, Collection<Identifier>> ENTITY_TYPE_SUB_TAGS = new ConcurrentHashMap<>();
    private static final String ENTITY_TYPE_TAG_PATH = Registries.tagsDirPath(Registries.ENTITY_TYPE);

    public static final MapCodec<ModifyEntityTypeTagAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(a -> a.tag),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyEntityTypeTagAbility::new));

    public final TagKey<EntityType<?>> tag;

    public ModifyEntityTypeTagAbility(TagKey<EntityType<?>> tag, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.tag = tag;
    }

    public boolean doesApply(TagKey<EntityType<?>> typeTag) {
        return Objects.equals(typeTag, this.tag) || ENTITY_TYPE_SUB_TAGS.getOrDefault(typeTag.location(), Set.of())
                .stream()
                .map(id -> TagKey.create(Registries.ENTITY_TYPE, id))
                .anyMatch(this::doesApply);
    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> typeTag) {
        if (!(entity instanceof LivingEntity living)) return false;

        return AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.MODIFY_ENTITY_TYPE_TAG.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().doesApply(typeTag));
    }

    public static boolean doesApply(Entity entity, HolderSet<EntityType<?>> entries) {
        return entries.unwrapKey()
                .map(tagKey -> doesApply(entity, tagKey))
                .orElse(false);
    }

    @ApiStatus.Internal
    public static void setTagCache(String directory, Map<Identifier, List<TagLoader.EntryWithSource>> builders) {
        if (ENTITY_TYPE_TAG_PATH.equals(directory)) {
            resetTagCache();

            builders.forEach((id, entries) -> entries
                .stream()
                .map(TagLoader.EntryWithSource::entry)
                .filter(TagEntry::isTag)
                .forEach(entry -> ENTITY_TYPE_SUB_TAGS
                        .computeIfAbsent(id, k -> new ObjectArraySet<>())
                        .add(entry.getId())));
        }
    }

    @ApiStatus.Internal
    public static void resetTagCache() {
        ENTITY_TYPE_SUB_TAGS.clear();
    }

    @ApiStatus.Internal
    public static void receiveTagCache(S2CSyncEntityTypeTagCache packet) {
        ENTITY_TYPE_SUB_TAGS.clear();
        ENTITY_TYPE_SUB_TAGS.putAll(packet.subTags());
    }

    @ApiStatus.Internal
    public static void sendTagCache(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new S2CSyncEntityTypeTagCache(ENTITY_TYPE_SUB_TAGS));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_ENTITY_TYPE_TAG.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyEntityTypeTagAbility> {

        @Override
        public MapCodec<ModifyEntityTypeTagAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyEntityTypeTagAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Entity Type Tag")
                    .setDescription("Makes the entity be apart of the specified entity type tag (including sub-tags).")
                    .add("tag", TYPE_ENTITY_TYPE_HOLDER_SET, "The specified entity type tag.")
                    .addExampleObject(new ModifyEntityTypeTagAbility(TagKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("powder_snow_walkable_mobs")), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}