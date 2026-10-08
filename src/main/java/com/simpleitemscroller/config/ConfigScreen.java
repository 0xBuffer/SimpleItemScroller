package com.simpleitemscroller.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Simple Item Scroller"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        this.addRenderableWidget(Button.builder(this.enabledText(), button -> {
            Config.enabled = !Config.enabled;
            button.setMessage(this.enabledText());
        }).bounds(cx - 100, 60, 200, 20).build());

        this.addRenderableWidget(new AbstractSliderButton(cx - 100, 90, 200, 20, this.delayText(Config.delayMs), Config.delayMs / 1000.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(ConfigScreen.this.delayText((int) Math.round(this.value * 1000)));
            }

            @Override
            protected void applyValue() {
                Config.delayMs = (int) Math.round(this.value * 1000);
            }
        });

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(cx - 100, 130, 200, 20).build());
    }

    private Component enabledText() {
        return Component.literal("Enabled: " + (Config.enabled ? "ON" : "OFF"));
    }

    private Component delayText(int ms) {
        return Component.literal("Delay: " + ms + " ms");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);
        extractor.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        Config.save();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
