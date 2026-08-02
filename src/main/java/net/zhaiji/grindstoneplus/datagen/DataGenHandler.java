package net.zhaiji.grindstoneplus.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenHandler {
    public static void handlerGatherDataEvent(GatherDataEvent.Client event) {
        event.createProvider(output -> new LanguageProvider(output, LanguageProvider.EN_US));
        event.createProvider(output -> new LanguageProvider(output, LanguageProvider.ZH_CN));
    }
}
