package com.simpleitemscroller;

import com.mojang.blaze3d.platform.InputConstants;
import com.simpleitemscroller.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class ItemScroller {
    private static final Logger LOGGER = LoggerFactory.getLogger("simple-item-scroller");
    private static final ArrayDeque<Slot> pendingSlots = new ArrayDeque<>();
    private static AbstractContainerMenu activeHandler;
    private static boolean active;
    private static long nextClickTime;
    private static Slot observedSlot;
    private static Slot lastTriggeredSlot;
    private static int triggeredSlots;

    private ItemScroller() {
    }

    public static void onMouseClicked(Minecraft client, AbstractContainerMenu handler, Slot slot, int button, boolean shift) {
        if (!Config.enabled || button != InputConstants.MOUSE_BUTTON_LEFT || !shift) {
            reset();
            return;
        }
        start(handler);
        LOGGER.debug("SIS screen: type={}, creative={}", client.gui.screen() == null ? "none" : client.gui.screen().getClass().getSimpleName(), client.player != null && client.player.isCreative());
        if (slot != null && slot.index >= 0) {
            lastTriggeredSlot = slot;
            triggeredSlots = 1;
            nextClickTime = Util.getMillis() + Config.delayMs;
        }
    }

    public static void onMouseDragged(Minecraft client, AbstractContainerMenu handler, Slot slot, int button, boolean shift, Consumer<Slot> quickMove) {
        if (!canUse(client) || button != InputConstants.MOUSE_BUTTON_LEFT || !shift) {
            reset();
            return;
        }
        if (!active || activeHandler != handler) {
            start(handler);
        }
        observe(slot, "drag");
        enqueue(slot);
        process(handler, quickMove);
    }

    public static void onFrame(Minecraft client, AbstractContainerMenu handler, Slot slot, Consumer<Slot> quickMove) {
        if (!active) {
            return;
        }
        if (!canUse(client) || activeHandler != handler) {
            reset();
            return;
        }
        observe(slot, "frame");
        enqueue(slot);
        process(handler, quickMove);
    }

    public static void onMouseReleased(Consumer<Slot> quickMove) {
        if (active && activeHandler != null) {
            process(activeHandler, quickMove, true);
        }
        reset();
    }

    private static boolean canUse(Minecraft client) {
        return Config.enabled && client.player != null && client.gameMode != null;
    }

    private static void start(AbstractContainerMenu handler) {
        pendingSlots.clear();
        activeHandler = handler;
        active = true;
        nextClickTime = 0L;
        observedSlot = null;
        lastTriggeredSlot = null;
        triggeredSlots = 0;
        LOGGER.debug("SIS gesture started: syncId={}", handler.containerId);
    }

    private static void observe(Slot slot, String source) {
        if (slot != observedSlot) {
            observedSlot = slot;
            LOGGER.debug("SIS hover: source={}, slot={}, hasStack={}", source, slot == null ? -1 : slot.index, slot != null && slot.hasItem());
        }
    }

    private static void enqueue(Slot slot) {
        if (slot == null || slot.index < 0 || slot == lastTriggeredSlot) {
            return;
        }
        lastTriggeredSlot = slot;
        if (slot.hasItem()) {
            pendingSlots.addLast(slot);
            triggeredSlots++;
            LOGGER.debug("SIS slot queued: id={}, pending={}", slot.index, pendingSlots.size());
        }
    }

    private static void process(AbstractContainerMenu handler, Consumer<Slot> quickMove) {
        process(handler, quickMove, false);
    }

    private static void process(AbstractContainerMenu handler, Consumer<Slot> quickMove, boolean flush) {
        long now = Util.getMillis();
        int delay = Math.max(0, Config.delayMs);
        if (!flush && now < nextClickTime) {
            return;
        }
        int allowance;
        if (flush || delay == 0) {
            allowance = Integer.MAX_VALUE;
        } else {
            allowance = nextClickTime == 0L ? 1 : 1 + (int) Math.min(Integer.MAX_VALUE - 1L, (now - nextClickTime) / delay);
        }
        while (allowance > 0 && !pendingSlots.isEmpty()) {
            Slot slot = pendingSlots.removeFirst();
            if (slot.index < 0 || !slot.hasItem()) {
                continue;
            }
            LOGGER.debug("SIS quick move: syncId={}, slot={}", handler.containerId, slot.index);
            quickMove.accept(slot);
            allowance--;
            nextClickTime = delay == 0 ? now : Math.max(nextClickTime, now) + delay;
        }
    }

    private static void reset() {
        if (active) {
            LOGGER.debug("SIS gesture ended: pending={}, triggered={}", pendingSlots.size(), triggeredSlots);
        }
        pendingSlots.clear();
        activeHandler = null;
        active = false;
        nextClickTime = 0L;
        observedSlot = null;
        lastTriggeredSlot = null;
        triggeredSlots = 0;
    }
}
