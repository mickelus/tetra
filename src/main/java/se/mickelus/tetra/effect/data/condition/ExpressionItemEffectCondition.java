package se.mickelus.tetra.effect.data.condition;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.ExpressionNumberProvider;
import se.mickelus.tetra.effect.data.provider.NumberProvider;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

public class ExpressionItemEffectCondition extends ItemEffectCondition {
    private static final Type dataType = new TypeToken<Map<String, ItemTransforms>>() {
    }.getType();
    NumberProvider left;
    NumberProvider right;
    Operator operator;

    @Nullable
    Map<String, Float> data;

    public ExpressionItemEffectCondition(NumberProvider left, NumberProvider right, Operator operator, @Nullable Map<String, Float> data) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean test(ItemEffectContext context) {
        if (data != null) {
            context = context.withMergedData(data);
        }
        return operator.comparator.apply(left.getValue(context), right.getValue(context));
    }

    public static ItemEffectCondition deserialize(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String fullExpression = jsonObject.get("expression").getAsString();
        for (Operator operator : Operator.values()) {
            if (fullExpression.contains(operator.key)) {
                String[] expressionParts = fullExpression.split(operator.key);
                if (expressionParts.length == 2) {
                    NumberProvider left = ExpressionNumberProvider.parseExpression(expressionParts[0]);
                    NumberProvider right = ExpressionNumberProvider.parseExpression(expressionParts[1]);
                    Map<String, Float> data = DataManager.gson.fromJson(jsonObject.get("data"), dataType);

                    return new ExpressionItemEffectCondition(left, right, operator, data);
                }
            }
        }

        throw new JsonParseException("Could not parse expression condition '" + fullExpression + "', missing operator");
    }

    public enum Operator {
        equals("==", Float::equals),
        notEquals("!=", (a, b) -> !a.equals(b)),
        lessThan("<", (a, b) -> a < b),
        lessThanOrEquals("<=", (a, b) -> a <= b),
        greaterThan(">", (a, b) -> a > b),
        greaterThanOrEquals(">=", (a, b) -> a >= b);

        final String key;
        final BiFunction<Float, Float, Boolean> comparator;

        Operator(String key, BiFunction<Float, Float, Boolean> comparator) {
            this.key = key;
            this.comparator = comparator;
        }
    }
}
