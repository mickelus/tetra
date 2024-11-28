package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class TimeNumberProvider implements NumberProvider {
    TimeProperty property = TimeProperty.gameTime;

    @Override
    public float getValue(ItemEffectContext context) {
        return switch (property) {
            case gameTime -> context.getLevel().getGameTime();
            case dayTime -> context.getLevel().getDayTime();
            case moonPhase -> context.getLevel().getMoonPhase();
        };
    }

    enum TimeProperty {
        gameTime,
        dayTime,
        moonPhase,
    }
}
