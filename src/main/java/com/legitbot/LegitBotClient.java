package com.legitbot;
import com.legitbot.render.EspRenderer;
import net.fabricmc.api.ClientModInitializer;
public class LegitBotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("LegitBot Loaded!");
        EspRenderer.register();
    }
}