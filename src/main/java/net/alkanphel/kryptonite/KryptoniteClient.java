package net.alkanphel.kryptonite;

import net.alkanphel.kryptonite.client.datagen.KryptoniteLangProvider;
import net.alkanphel.kryptonite.power.compat.lambdynlights.DynLightsCompatClient;
import net.alkanphel.kryptonite.proxy.KryptoniteProxyClient;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.HTMLBuilder;

@Mod(value = Kryptonite.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KryptoniteClient {

    public KryptoniteClient(ModContainer container) {
        Kryptonite.PROXY = new KryptoniteProxyClient();

        if (ModList.get().isLoaded("lambdynlights")) {
            DynLightsCompatClient.init();
        }

    }

    @SubscribeEvent
    static void onGatherDataClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        // client
        event.createProvider(KryptoniteLangProvider.English::new);

        // server
    }

    @SubscribeEvent
    static void onGenerateDocumentation(LevelEvent.Load e) {
        LevelAccessor clientLevel = e.getLevel();
        CodecDocumentationBuilder.startListening();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.BI_ACTION_SERIALIZER, KryptoniteRegistries.BI_ACTION_SERIALIZER, "Bi Actions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.BLOCK_ACTION_SERIALIZER, KryptoniteRegistries.BLOCK_ACTION_SERIALIZER, "Block Actions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.BI_CONDITION_SERIALIZER, KryptoniteRegistries.BI_CONDITION_SERIALIZER, "Bi Conditions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.BLOCK_CONDITION_SERIALIZER, KryptoniteRegistries.BLOCK_CONDITION_SERIALIZER, "Block Conditions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.DIMENSION_CONDITION_SERIALIZER, KryptoniteRegistries.DIMENSION_CONDITION_SERIALIZER, "Dimension Conditions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(KryptoniteRegistryKeys.DAMAGE_CONDITION_SERIALIZER, KryptoniteRegistries.DAMAGE_CONDITION_SERIALIZER, "Damage Conditions", clientLevel.registryAccess()).save();
        CodecDocumentationBuilder.createDocFiles();
    }

}