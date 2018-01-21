package se.mickelus.tetra.gui.animation;

import se.mickelus.tetra.gui.GuiElement;

import java.util.Arrays;
import java.util.function.Consumer;

public class KeyframeAnimation {

    private final int duration;
    private int delay = 0;
    private final GuiElement element;
    private Consumer<Boolean> handler;


    private Applier[] appliers;

    private long startTime;
    private boolean isActive = false;

    public KeyframeAnimation(int duration, GuiElement element) {
        this.duration = duration;
        this.element = element;
    }

    public KeyframeAnimation applyTo(Applier... appliers) {
        this.appliers = appliers;
        Arrays.stream(this.appliers).forEach(applier -> applier.setElement(element));
        return this;
    }

    public KeyframeAnimation withDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public KeyframeAnimation onStop(Consumer<Boolean> handler) {
        this.handler = handler;
        return this;
    }

    public void start() {
        startTime = System.currentTimeMillis();

        Arrays.stream(this.appliers).forEach(applier -> applier.start(duration));

        isActive = true;
        element.addAnimation(this);
    }

    public void stop() {
        // todo: hacky
        if (handler != null) {
            handler.accept(!isActive);
        }
        isActive = false;
    }

    public void preDraw() {
        long currentTime = System.currentTimeMillis();
        if (startTime + delay < currentTime) {
            if (startTime + delay + duration > currentTime) {
                float progress = (currentTime - delay - startTime) * 1f / duration;
                Arrays.stream(appliers).forEach(applier -> applier.preDraw(progress));
            } else {
                Arrays.stream(appliers).forEach(applier -> applier.preDraw(1));
                isActive = false;
                stop();
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
