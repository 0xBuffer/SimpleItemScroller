package com.simpleitemscroller.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Simple Item Scroller"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        this.addDrawableChild(ButtonWidget.builder(this.enabledText(), button -> {
            Config.enabled = !Config.enabled;
            button.setMessage(this.enabledText());
        }).dimensions(cx - 100, 60, 200, 20).build());

        this.addDrawableChild(new SliderWidget(cx - 100, 90, 200, 20, this.delayText(Config.delayMs), Config.delayMs / 1000.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(ConfigScreen.this.delayText((int) Math.round(this.value * 1000)));
            }

            @Override
            protected void applyValue() {
                Config.delayMs = (int) Math.round(this.value * 1000);
            }
        });

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close()).dimensions(cx - 100, 130, 200, 20).build());
    }

    private Text enabledText() {
        return Text.literal("Enabled: " + (Config.enabled ? "ON" : "OFF"));
    }

    private Text delayText(int ms) {
        return Text.literal("Delay: " + ms + " ms");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        Config.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
