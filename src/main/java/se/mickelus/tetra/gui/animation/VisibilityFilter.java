package se.mickelus.tetra.gui.animation;

public class VisibilityFilter {

    private final float OPACITY_STEP = 0.1f;
    private final int DECREASE_DELAY = 100;

    private final float min;
    private final float max;

    private float input;
    private float output;

    private int delay = DECREASE_DELAY;

    public VisibilityFilter(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float apply(float value) {
        input = value;
        updateOutput();

        return output;
    }

    public float get() {
        return output;
    }

    private void updateOutput() {
        if (min < input && input < max) {
            if (output + OPACITY_STEP < 1) {
                output += OPACITY_STEP;
            } else {
                output = 1;
            }

            delay = DECREASE_DELAY;
        } else {
            if (delay == 0) {
                if (output - OPACITY_STEP > 0) {
                    output -= OPACITY_STEP;
                } else {
                    output = 0;
                }
            } else {
                delay--;
            }
        }
    }

}
