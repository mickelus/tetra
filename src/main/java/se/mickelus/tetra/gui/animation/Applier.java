package se.mickelus.tetra.gui.animation;

import se.mickelus.tetra.gui.GuiElement;

public abstract class Applier {

    protected GuiElement element;

    protected boolean relativeStart = true;
    protected float startValue;
    protected float targetValue;
    protected float currentValue;
    protected float stepSize;

    public Applier(float targetValue) {
        this.targetValue = targetValue;
    }

    public Applier(float startValue, float targetValue) {
        this.targetValue = targetValue;
        this.startValue = startValue;

        this.relativeStart = false;
    }

    public void setElement(GuiElement element) {
        this.element = element;
    }

    public void start(int duration) {
        if (relativeStart) {
            startValue = getRelativeStartValue();
        }
        currentValue = startValue;
    }

    protected abstract float getRelativeStartValue();

    public void preDraw(float progress) {
        currentValue = startValue + progress * (targetValue - startValue);
    }


    public static class TranslateX extends Applier {

        public TranslateX(float targetValue) {
            super(targetValue);
        }

        public TranslateX(float startValue, float targetValue) {
            super(startValue, targetValue);
        }

        @Override
        public void start(int duration) {
            super.start(duration);
            if (!relativeStart) {
                element.setX((int) startValue);
            }
        }

        @Override
        protected float getRelativeStartValue() {
            return element.getX();
        }

        @Override
        public void preDraw(float progress) {
            super.preDraw(progress);
            element.setX((int) currentValue);
        }
    }

    public static class TranslateY extends Applier {

        public TranslateY(float targetValue) {
            super(targetValue);
        }

        public TranslateY(float startValue, float targetValue) {
            super(startValue, targetValue);
        }

        @Override
        protected float getRelativeStartValue() {
            return element.getY();
        }

        @Override
        public void preDraw(float progress) {
            super.preDraw(progress);
            element.setY((int) currentValue);
        }
    }

    public static class Opacity extends Applier {

        public Opacity(float targetValue) {
            super(targetValue);
        }

        public Opacity(float startValue, float targetValue) {
            super(startValue, targetValue);
        }

        @Override
        protected float getRelativeStartValue() {
            return element.getOpacity();
        }

        @Override
        public void preDraw(float progress) {
            super.preDraw(progress);
            element.setOpacity(currentValue);
        }
    }
}
