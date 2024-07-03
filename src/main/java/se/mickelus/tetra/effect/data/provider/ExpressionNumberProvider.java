package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Map;
import java.util.Stack;

public class ExpressionNumberProvider {
    private String expression;
    private NumberProvider rootProvider;

    public ExpressionNumberProvider(String expression) {
        this.expression = expression;
    }

    public float getValue(ItemEffectContext context) {
        return rootProvider.getValue(context);
    }

    public int getIntegerValue(ItemEffectContext context) {
        return Math.round(getValue(context));
    }

    static public class Parser {

        public static void main(String[] args) {
            Parser parser = new Parser();
            NumberProvider numberProvider = parser.parse("(1+2)*3");
            System.out.println(numberProvider.getValue(null));
        }

        private static final Map<Character, Integer> OPERATOR_PRECEDENCE = Map.of(
                '+', 1,
                '-', 1,
                '*', 2,
                '/', 2
        );

        public NumberProvider parse(String expressionIn) {
            Stack<NumberProvider> operandStack = new Stack<>();
            Stack<Character> operatorStack = new Stack<>();

            String expression = expressionIn.replace(" ", "");

            while (!expression.isEmpty()) {
                int nextOperatorIndex = getNextOperatorIndex(expression);
                if (nextOperatorIndex == 0) {
                    char token = expression.charAt(0);
                    if (OPERATOR_PRECEDENCE.containsKey(token)) {
                        while (!operatorStack.isEmpty()
                                && OPERATOR_PRECEDENCE.containsKey(operatorStack.peek())
                                && OPERATOR_PRECEDENCE.get(token) <= OPERATOR_PRECEDENCE.get(operatorStack.peek())) {
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
                    expression = expression.substring(1);
                } else {
                    String valueString = nextOperatorIndex != -1
                            ? expression.substring(0, nextOperatorIndex)
                            : expression;
                    expression = nextOperatorIndex != -1
                            ? expression.substring(nextOperatorIndex)
                            : "";

                    if (isNumber(valueString)) {
                        operandStack.push(new FixedNumberProvider(Float.parseFloat(valueString)));
                    } else if (isReference(valueString)) {
                        operandStack.push(new ContextNumberProvider(valueString));
                    } else {
                        throw new IllegalArgumentException("Encountered invalid value '" + valueString + "' in expression: " + expressionIn);
                    }
                }
            }

            while (!operatorStack.isEmpty()) {
                createNumberProvider(operandStack, operatorStack.pop());
            }

            return operandStack.pop();
        }

        private int getNextOperatorIndex(String expression) {
            for (int i = 0; i < expression.length(); i++) {
                if (isOperator(expression.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }

        private boolean isOperator(char token) {
            return token == '(' || token == ')' || OPERATOR_PRECEDENCE.containsKey(token);
        }

        private boolean isNumber(String valueString) {
            return valueString.matches("^[0-9]*\\.?[0-9]+$");
        }

        private boolean isReference(String valueString) {
            return valueString.matches("^[a-zA-Z_]+$");
        }

        private void createNumberProvider(Stack<NumberProvider> operandStack, char operator) {
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
}
