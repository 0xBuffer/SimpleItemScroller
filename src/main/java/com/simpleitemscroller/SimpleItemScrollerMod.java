package com.simpleitemscroller;

import com.simpleitemscroller.config.Config;
import net.fabricmc.api.ClientModInitializer;

public class SimpleItemScrollerMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
    }
}
