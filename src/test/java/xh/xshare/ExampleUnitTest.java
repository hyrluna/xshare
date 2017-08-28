package xh.xshare;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
//        calculator("12*56-23+4/2");
        for (String s : infixToPostfix("12/8-9+56")) {
            System.out.println(s);
        }
        System.out.println(12.0/8.0-9+56);
        System.out.println(calPostfixExp(infixToPostfix("12/8-9+56")) + "");
//        System.out.println(priorOrEqual('-', '/'));
    }

    public double calPostfixExp(List<String> exp) {
        Stack<Double> numStack = new Stack<>();
        for (String s : exp) {
            if (s.matches("[0-9]+")) {
                numStack.push(Double.parseDouble(s));
            } else if (s.matches("[+\\-*/]")) {
                double num1 = numStack.pop();
                double num2 = numStack.pop();
                numStack.push(op(num1, num2, s.charAt(0)));
            }
        }
        return numStack.pop();
    }

    public double op(double num1, double num2, char op) {
        double result = 0;
        switch (op) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num2 - num1;
                break;
            case '*':
                result = num1 * num2;
                break;
            case '/':
                result = num2 / num1;
                break;
        }
        return result;
    }

    public List<String> infixToPostfix(String inputExp) {
        List<String> exp = convertInputToExp(inputExp);
        Stack<Character> opStack = new Stack<>();
        List<String> outputStream = new ArrayList<>();
        for (String s : exp) {
            if (s.matches("[0-9]+")) {
                outputStream.add(s);
            } else if (s.matches("[+\\-]")) {
                if (opStack.empty()) {
                    opStack.push(s.charAt(0));
                } else {
                    while (!opStack.empty()) {
                        //剛剛遇到的運算符比優先級比nextOp運算符高，那麼nextOp不用出棧
                        if (!priorOrEqual(s.charAt(0), opStack.peek())) {
                            outputStream.add(String.valueOf(opStack.pop()));
                        } else {
                            break;
                        }
                    }
                    opStack.push(s.charAt(0));

//                    if (opStack.peek() == '*' || opStack.peek() == '/') {
//                        while (!opStack.empty() && priorOrEqual(s.charAt(0), opStack.peek())) {
//                            outputStream.add(String.valueOf(opStack.pop()));
//                        }
//                        opStack.push(s.charAt(0));
//
//                    } else {
//                        opStack.push(s.charAt(0));
//                    }
                }
            } else if (s.matches("[*/]")) {
                if (opStack.empty()) {
                    opStack.push(s.charAt(0));
                } else {
                    while (!opStack.empty()) {
                        //剛剛遇到的運算符比優先級比nextOp運算符高，那麼nextOp不用出棧
                        if (!priorOrEqual(s.charAt(0), opStack.peek())) {
                            outputStream.add(String.valueOf(opStack.pop()));
                        } else {
                            break;
                        }
                    }
                    opStack.push(s.charAt(0));
                }

            }
        }
        while (!opStack.empty()) {
            outputStream.add(String.valueOf(opStack.pop()));
        }
        return outputStream;
    }

    public boolean priorOrEqual(char opA, char opB) {
        int numA = 0;
        int numB = 0;
        if (opA == '+' || opA == '-') {
            numA = 1;
        } else if (opA == '*' || opA == '/') {
            numA = 2;
        }
        if (opB == '+' || opB == '-') {
            numB = 1;
        } else if (opB == '*' || opB == '/') {
            numB = 2;
        }
        return numA - numB >= 0;
    }

    public List<String> convertInputToExp(String exp) {
        Pattern patternOperatrion = Pattern.compile("[+\\-*/]");
        Pattern patternNumber = Pattern.compile("[0-9]+");
        Matcher matcherOp = patternOperatrion.matcher(exp);
        Matcher matcherNumber = patternNumber.matcher(exp);
        List<String> result = new ArrayList<>();
        while (matcherNumber.find()) {
            result.add(matcherNumber.group());
            if (matcherOp.find()) {
                result.add(matcherOp.group());
            }
        }
        return result;
    }

    public Stack<String> generateSuffixExp(String exp) {
        Pattern patternOperatrion = Pattern.compile("[+\\-*/]");
        Pattern patternNumber = Pattern.compile("[0-9]+");
        Matcher matcherOp = patternOperatrion.matcher(exp);
        Matcher matcherNumber = patternNumber.matcher(exp);
        List<String> result = new ArrayList<>();
        while (matcherNumber.find()) {
            result.add(matcherNumber.group());
            if (matcherOp.find()) {
                result.add(matcherOp.group());
            }
        }

        Stack<String> expStack = new Stack<>();
        String tmpOp1 = null;
        String tmpOp2 = null;
        for (String r : result) {
            if (r.matches("[0-9]+")) {
                expStack.push(r);
                if (tmpOp2 != null) {
                    expStack.push(tmpOp2);
                    tmpOp2 = null;
                }
                if (tmpOp1 != null) {
                    expStack.push(tmpOp1);
                    tmpOp1 = null;
                }
            } else if (r.matches("[+\\-]")) {
                tmpOp1 = r;
            } else if (r.matches("[*/]")) {
                tmpOp1 = expStack.pop();
                tmpOp2 = r;
            }
        }
        return expStack;
    }

    public int calculator(Stack<String> expStack, String e1, String e2) {
        String exp = e1;
        if (exp == null) {
            exp = expStack.pop();
        }
        if (exp.matches("[0-9]+")) {
            return Integer.parseInt(exp);
        } else if (exp.matches("[+\\-*/]")) {
            if (exp.matches("[*/]") || e2 != null) {
                expStack.push(e2);
            }
            String exp1 = expStack.pop();
            String exp2 = expStack.pop();
            int result = 0;
            switch (exp) {
                case "+":
                    result = calculator(expStack, exp1, exp2) + calculator(expStack, exp2, exp1);
                    break;
                case "-":
                    result = calculator(expStack, exp1, exp2) - calculator(expStack, exp2, exp1);
                    break;
                case "*":
                    result = calculator(expStack, exp1, exp2) * calculator(expStack, exp2, exp1);
                    break;
                case "/":
                    result = calculator(expStack, exp1, exp2) / calculator(expStack, exp2, exp1);
                    break;
            }
//            expStack.push(result+"");
            return result;
        }
        return 0;
    }
}