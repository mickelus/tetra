package se.mickelus.tetra.effect.data.provider.number;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.ItemEffectData;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Stack;

public class ExpressionNumberProvider implements NumberProvider {
    private static final Type dataType = new TypeToken<Map<String, NumberProvider>>() {
    }.getType();

    private static final Map<Character, Integer> operatorPrecedence = Map.of(
            '+', 1,
            '-', 1,
            '*', 2,
            '/', 2
    );

    private final NumberProvider rootProvider;

    private final Map<String, NumberProvider> data;

    public ExpressionNumberProvider(NumberProvider rootProvider, Map<String, NumberProvider> data) {
        this.rootProvider = rootProvider;
        this.data = data;
    }

    public float getValue(ItemEffectContext context) {
        if (data != null) {
            return rootProvider.getValue(context.withMergedData(ItemEffectData.calculateData(data, context)));
        }
        return rootProvider.getValue(context);
    }

    public int getIntegerValue(ItemEffectContext context) {
        if (data != null) {
            return rootProvider.getIntegerValue(context.withMergedData(ItemEffectData.calculateData(data, context)));
        }
        return rootProvider.getIntegerValue(context);
    }

    public static ExpressionNumberProvider deserialize(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ExpressionNumberProvider(parseExpression(
                jsonObject.get("expression").getAsString()),
                DataManager.gson.fromJson(jsonObject.get("data"), dataType));
    }

    public static NumberProvider parseExpression(String expression) {
        Stack<NumberProvider> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        String trimmedExpression = expression.replace(" ", "");

        while (!trimmedExpression.isEmpty()) {
            int nextOperatorIndex = getNextOperatorIndex(trimmedExpression);
            if (nextOperatorIndex == 0) {
                char token = trimmedExpression.charAt(0);
                if (operatorPrecedence.containsKey(token)) {
                    while (!operatorStack.isEmpty()
                            && operatorPrecedence.containsKey(operatorStack.peek())
                            && operatorPrecedence.get(token) <= operatorPrecedence.get(operatorStack.peek())) {
                        createNumberProvider(operandStack, operatorStack.pop());
                    }
                    operatorStack.push(token);
                } else if (token == '(') {
                    operatorStack.push(token);
                } else if (token == ')') {
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                        createNumberProvider(operandStack, operatorStack.pop());
                    }
                    operatorStack.pop();
                }
                trimmedExpression = trimmedExpression.substring(1);
            } else {
                String valueString = nextOperatorIndex != -1
                        ? trimmedExpression.substring(0, nextOperatorIndex)
                        : trimmedExpression;
                trimmedExpression = nextOperatorIndex != -1
                        ? trimmedExpression.substring(nextOperatorIndex)
                        : "";

                if (isNumber(valueString)) {
                    operandStack.push(new FixedNumberProvider(Float.parseFloat(valueString)));
                } else if (isReference(valueString)) {
                    operandStack.push(new ContextNumberProvider(valueString));
                } else {
                    throw new IllegalArgumentException("Encountered invalid value '" + valueString + "' in expression: " + expression);
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            createNumberProvider(operandStack, operatorStack.pop());
        }

        return operandStack.pop();
    }

    private static int getNextOperatorIndex(String expression) {
        for (int i = 0; i < expression.length(); i++) {
            if (isOperator(expression.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isOperator(char token) {
        return token == '(' || token == ')' || operatorPrecedence.containsKey(token);
    }

    private static boolean isNumber(String valueString) {
        return valueString.matches("^[0-9]*\\.?[0-9]+$");
    }

    private static boolean isReference(String valueString) {
        return valueString.matches("^[a-zA-Z_]+$");
    }

    private static void createNumberProvider(Stack<NumberProvider> operandStack, char operator) {
        NumberProvider right = operandStack.pop();
        NumberProvider left = operandStack.pop();

        switch (operator) {
            case '+':
                operandStack.push(new SumNumberProvider(left, right));
                break;
            case '-':
                operandStack.push(new SubtractNumberProvider(left, right));
                break;
            case '*':
                operandStack.push(new MultiplyNumberProvider(left, right));
                break;
            case '/':
                operandStack.push(new DivideNumberProvider(left, right));
                break;
        }
    }
}
