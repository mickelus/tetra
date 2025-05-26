package se.mickelus.tetra.craftingeffect;

import java.util.function.BiFunction;

public enum StackMode {
    add(Integer::sum),
    stackTo((currentLevel, newLevel) -> newLevel > currentLevel
            ? Math.max(currentLevel, 0) + 1
            : newLevel),
    stackFrom((currentLevel, newLevel) -> newLevel < currentLevel
            ? currentLevel + 1
            : newLevel),
    stack((currentLevel, newLevel) -> Math.max(currentLevel, 0) + newLevel),
    max(Math::max),
    replace((currentLevel, newLevel) -> newLevel);

    private final BiFunction<Integer, Integer, Integer> behaviour;

    StackMode(BiFunction<Integer, Integer, Integer> behaviour) {
        this.behaviour = behaviour;
    }

    public int evaluate(int currentValue, int newValue) {
        return behaviour.apply(currentValue, newValue);
    }
}
