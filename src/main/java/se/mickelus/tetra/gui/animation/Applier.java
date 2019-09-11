package se.mickelus.tetra.gui.animation;

import se.mickelus.tetra.gui.GuiElement;

public abstract class Applier {

    protected GuiElement element;

    protected boolean relativeStart;
    protected boolean relativeTarget;
    protected float startOffset = 0;
    protected float targetOffset = 0;


    protected float startValue;
    protected float targetValue;

    protected float currentValue;

    public Applier(float targetValue) {
        this(0, targetValue, true, false);
    }

    public Applier(float startValue, float targetValue) {
        this(startValue, targetValue, false, false);
    }

    public Applier(float startValue, float targetValue, boolean relativeStart, boolean relativeTarget) {
        this.targetValue = targetValue;
        this.startValue = startValue;

        this.relativeStart = relativeStart;
        this.relativeTarget = relativeTarget;

        this.startOffset = startValue;
        this.targetOffset = targetValue;
    }

    public void setElement(GuiElement element) {
        this.element = element;
    }

    public void start(int duration) {
        if (relativeStart) {
            startValue = getRelativeValue() + startOffset;
        }

        if (relativeTarget) {
            targetValue = getRelativeValue() + targetOffset;
        }

        currentValue = startValue;
    }

    protected abstract float getRelativeValue();

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

        public TranslateX(float startValue, float targetValue, boolean relative) {
            super(startValue, targetValue, relative, relative);
        }

        public TranslateX(float startValue, float targetValue, boolean relativeStart, boolean relativeTarget) {
            super(startValue, targetValue, relativeStart, relativeTarget);
        }

        @Override
        public void start(int duration) {
            super.start(duration);
            if (!relativeStart) {
                element.setX((int) startValue);
            }
        }

        @Override
        protected float getRelativeValue() {
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

        public TranslateY(float startValue, float targetValue, boolean relative) {
            super(startValue, targetValue, relative, relative);
        }

        public TranslateY(float startValue, float targetValue, boolean relativeStart, boolean relativeTarget) {
            super(startValue, targetValue, relativeStart, relativeTarget);
        }

        @Override
        public void start(int duration) {
            super.start(duration);
            if (!relativeStart) {
                element.setY((int) startValue);
            }
        }

        @Override
        protected float getRelativeValue() {
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

        public Opacity(float startValue, float targetValue, boolean relative) {
            super(startValue, targetValue, relative, relative);
        }

        public Opacity(float startValue, float targetValue, boolean relativeStart, boolean relativeTarget) {
            super(startValue, targetValue, relativeStart, relativeTarget);
        }

        @Override
        public void start(int duration) {
            super.start(duration);
            if (!relativeStart) {
                element.setOpacity((int) startValue);
            }
        }

        @Override
        protected float getRelativeValue() {
            return element.getOpacity();
        }

        @Override
        public void preDraw(float progress) {
            super.preDraw(progress);
            element.setOpacity(currentValue);
        }
    }

    public static class Width extends Applier {

        public Width(float targetValue) {
            super(targetValue);
        }

        public Width(float startValue, float targetValue) {
            super(startValue, targetValue);
        }

        public Width(float startValue, float targetValue, boolean relative) {
            super(startValue, targetValue, relative, relative);
        }

        public Width(float startValue, float targetValue, boolean relativeStart, boolean relativeTarget) {
            super(startValue, targetValue, relativeStart, relativeTarget);
        }

        @Override
        public void start(int duration) {
            super.start(duration);
            if (!relativeStart) {
                element.setWidth((int) startValue);
            }
        }

        @Override
        protected float getRelativeValue() {
            return element.getWidth();
        }

        @Override
        public void preDraw(float progress) {
            super.preDraw(progress);
            element.setWidth((int) currentValue);
        }
    }
}
