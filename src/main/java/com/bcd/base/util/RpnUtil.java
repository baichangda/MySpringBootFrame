package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;

import java.util.*;

public class RpnUtil {

    /**
     * 计算字符串公式
     * 支持 = - * / ( )
     *
     * @param str
     * @param map
     * @return
     */
    public static Double calcArithmetic(String str, Map<String, Double> map) {
        return calcRPN_string_string(parseArithmeticToRPN(str), map);
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> string
     * 将数字字符串 --> double
     *
     * @param rpn rpn表达式集合
     * @return
     */
    public static Object[] doWithRpnList_string_double(String[] rpn) {
        return Arrays.stream(rpn).map(e -> {
            try {
                return Double.parseDouble(e);
            } catch (NumberFormatException ex) {
                return e;
            }
        }).toArray();
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> char
     * 将数字字符串 --> double
     *
     * @param rpn rpn表达式集合
     * @return
     */
    public static Object[] doWithRpnList_char_double(String[] rpn) {
        return Arrays.stream(rpn).map(e -> {
            try {
                return Double.parseDouble(e);
            } catch (NumberFormatException ex) {
                return e.charAt(0);
            }
        }).toArray();
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> char
     * 将数字字符串 --> int
     *
     * @param rpn rpn表达式集合
     * @return
     */
    public static Object[] doWithRpnList_char_int(String[] rpn) {
        return Arrays.stream(rpn).map(e -> {
            try {
                return Integer.parseInt(e);
            } catch (NumberFormatException ex) {
                return e.charAt(0);
            }
        }).toArray();
    }

    /**
     * list中变量定义必须是char 支持 a-z A-Z
     * <p>
     * A-Z --> 65-90
     * a-z --> 97-122
     * 所以char数组长度为 65-122 长度为58
     * 同时需要进行偏移量计算也就是 字符-65
     *
     * @param rpn    rpn表达式集合,其中变量必须是char,常量必须是int
     * @param vals   变量对应值数组,取值规则为 vals[int(char)]
     * @return
     */
    public static int calcRPN_char_int(Object[] rpn, int[] vals) {
        int stackIndex = -1;
        int[] stack = new int[rpn.length];
        for (Object s : rpn) {
            if (s instanceof Integer) {
                stack[++stackIndex] = (int) s;
            } else {
                switch ((char) s) {
                    case '+': {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]+stack[stackIndex+1];
                        break;
                    }
                    case '-': {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]-stack[stackIndex+1];
                        break;
                    }
                    case '*': {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]*stack[stackIndex+1];
                        break;
                    }
                    case '/': {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]/stack[stackIndex+1];
                        break;
                    }
                    default: {
                        stack[++stackIndex] = vals[(char)s];
                        break;
                    }
                }
            }
        }
        return stack[0];
    }

    /**
     * 计算逆波兰表达式
     *
     * @param rpn 逆波兰表达式集合,其中变量必须是string,常量必须是double
     * @param map 字段和值对应map
     * @return
     */
    public static double calcRPN_string_double(Object[] rpn, Map<String, Double> map) {
        int stackIndex = -1;
        double[] stack = new double[rpn.length];
        for (Object o : rpn) {
            if (o instanceof Double) {
                stack[++stackIndex] = (double) o;
            } else {
                switch ((String) o) {
                    case "+": {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]+stack[stackIndex+1];
                        break;
                    }
                    case "-": {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]-stack[stackIndex+1];
                        break;
                    }
                    case "*": {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]*stack[stackIndex+1];
                        break;
                    }
                    case "/": {
                        stackIndex--;
                        stack[stackIndex]=stack[stackIndex]/stack[stackIndex+1];
                        break;
                    }
                    default: {
                        stack[++stackIndex] = map.get(o);
                        break;
                    }
                }
            }
        }
        return stack[0];
    }

    /**
     * 计算逆波兰表达式
     *
     * @param rpn 逆波兰表达式集合,其中变量必须是string,常量也是string
     * @param map 字段和值对应map
     * @return
     */
    public static double calcRPN_string_string(String[] rpn, Map<String, Double> map) {
        int stackIndex = -1;
        double[] stack = new double[rpn.length];
        for (String s : rpn) {
            switch (s) {
                case "+": {
                    stackIndex--;
                    stack[stackIndex]=stack[stackIndex]+stack[stackIndex+1];
                    break;
                }
                case "-": {
                    stackIndex--;
                    stack[stackIndex]=stack[stackIndex]-stack[stackIndex+1];
                    break;
                }
                case "*": {
                    stackIndex--;
                    stack[stackIndex]=stack[stackIndex]*stack[stackIndex+1];
                    break;
                }
                case "/": {
                    stackIndex--;
                    stack[stackIndex]=stack[stackIndex]/stack[stackIndex+1];
                    break;
                }
                default: {
                    Number val = map.get(s);
                    if (val == null) {
                        stack[++stackIndex] = Double.parseDouble(s);
                    } else {
                        stack[++stackIndex] = val.doubleValue();
                    }
                    break;
                }
            }
        }
        return stack[0];
    }

    /**
     * 将算数字符串转换成逆波兰表达式
     * 算数支持 + - * / ( ) 符号
     *
     * @return
     */
    public static String[] parseArithmeticToRPN(String str) {
        List<String> output = new ArrayList<>();
        char[] arr = str.toCharArray();
        //记录符号位置
        int stackIndex = -1;
        //存储符号
        char[] stack = new char[str.length()];
        //存储非符号
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i <= arr.length - 1; i++) {
            if (arr[i] == '+' || arr[i] == '-' || arr[i] == '*' || arr[i] == '/') {
                if (temp.length() > 0) {
                    output.add(temp.toString());
                    temp.delete(0, temp.length());
                }
                if (stackIndex >= 0) {
                    while (stack[stackIndex] != '(' && getSymbolPriority(stack[stackIndex]) >= getSymbolPriority(arr[i])) {
                        output.add(String.valueOf(stack[stackIndex--]));
                        if (stackIndex == -1) {
                            break;
                        }
                    }
                }
                stack[++stackIndex] = arr[i];
            } else if (arr[i] == '(') {
                stack[++stackIndex] = arr[i];
            } else if (arr[i] == ')') {
                if (temp.length() > 0) {
                    output.add(temp.toString());
                    temp.delete(0, temp.length());
                }
                while (stackIndex >= 0) {
                    char c = stack[stackIndex--];
                    if (c == '(') {
                        break;
                    }
                    output.add(String.valueOf(c));
                }
            } else {
                temp.append(arr[i]);
            }
        }

        if (temp.length() > 0) {
            output.add(temp.toString());
        }

        for (int i = stackIndex; i >= 0; i--) {
            output.add(String.valueOf(stack[i]));
        }

        return output.toArray(new String[0]);
    }

    /**
     * 获取字符优先级
     *
     * @param c
     * @return
     */
    private static int getSymbolPriority(char c) {
        switch (c) {
            case '+': {
                return 1;
            }
            case '-': {
                return 1;
            }
            case '*': {
                return 2;
            }
            case '/': {
                return 2;
            }
            default: {
                throw BaseRuntimeException.getException("symbol[" + c + "] not support");
            }
        }
    }

    public static String parseRPNToArithmetic(String[] rpn) {
        if (rpn.length == 1) {
            return rpn[0];
        } else {
            String[] stack = new String[rpn.length];
            int[] symbolPriority = new int[rpn.length];
            int index = -1;
            for (String s : rpn) {
                if (s.equals("+") ||
                        s.equals("-") ||
                        s.equals("*") ||
                        s.equals("/")) {
                    int index2 = index--;
                    int index1 = index--;
                    String s1 = stack[index1];
                    String s2 = stack[index2];
                    int p1 = symbolPriority[index1];
                    int p2 = symbolPriority[index2];
                    int curSymbolPriority = getSymbolPriority(s.charAt(0));
                    if (p1 != -1 && p1 < curSymbolPriority) {
                        s1 = "(" + s1 + ")";
                    }
                    if (p2 != -1 && p2 < curSymbolPriority) {
                        s2 = "(" + s2 + ")";
                    }
                    int curIndex = ++index;
                    stack[curIndex] = s1 + s + s2;
                    symbolPriority[curIndex] = curSymbolPriority;
                } else {
                    int curIndex = ++index;
                    stack[curIndex] = s;
                    symbolPriority[curIndex] = -1;
                }
            }
            return stack[0];
        }
    }

    public static String parseRPNToArithmetic(Object[] rpn) {
        if (rpn.length == 1) {
            return rpn[0].toString();
        } else {
            String[] stack = new String[rpn.length];
            int[] symbolPriority = new int[rpn.length];
            int index = -1;
            for (Object o : rpn) {
                String s = o.toString();
                if (s.equals("+") ||
                        s.equals("-") ||
                        s.equals("*") ||
                        s.equals("/")) {
                    int index2 = index--;
                    int index1 = index--;
                    String s1 = stack[index1];
                    String s2 = stack[index2];
                    int p1 = symbolPriority[index1];
                    int p2 = symbolPriority[index2];
                    int curSymbolPriority = getSymbolPriority(s.charAt(0));
                    if (p1 != -1 && p1 < curSymbolPriority) {
                        s1 = "(" + s1 + ")";
                    }
                    if (p2 != -1 && p2 < curSymbolPriority) {
                        s2 = "(" + s2 + ")";
                    }
                    int curIndex = ++index;
                    stack[curIndex] = s1 + s + s2;
                    symbolPriority[curIndex] = curSymbolPriority;
                } else {
                    int curIndex = ++index;
                    stack[curIndex] = s;
                    symbolPriority[curIndex] = -1;
                }
            }
            return stack[0];
        }
    }

    /**
     * 解析表达式 y=ax+b 中的a、b
     * 顺序可以调整、可以加上括号
     *
     * @param expr
     * @return [a,b]
     */
    public static double[] parseSimpleExpr(String expr) {
        double a = 1;
        double b = 0;
        String[] rpn = parseArithmeticToRPN(expr);
        int stackIndex = -1;
        final Object[] stack = new Object[expr.length()];
        for (String s : rpn) {
            try {
                double v = Double.parseDouble(s);
                stack[++stackIndex] = v;
            } catch (NumberFormatException ex) {
                switch (s) {
                    case "+": {
                        stackIndex--;
                        if (stack[stackIndex] instanceof String) {
                            b = (double) stack[stackIndex + 1];
                            stack[stackIndex] = stack[stackIndex] + "+" + stack[stackIndex + 1];
                        } else if (stack[stackIndex + 1] instanceof String) {
                            b = (double) stack[stackIndex];
                            stack[stackIndex] = stack[stackIndex] + "+" + stack[stackIndex + 1];
                        } else {
                            stack[stackIndex] = (double) stack[stackIndex] + (double) stack[stackIndex + 1];
                        }
                        break;
                    }
                    case "-": {
                        stackIndex--;
                        if (stackIndex == -1) {
                            if (stack[stackIndex + 1] instanceof String) {
                                a = -a;
                                stack[0] = "-" + stack[stackIndex + 1];
                            } else {
                                stack[0] = -(double) stack[stackIndex + 1];
                            }
                            stackIndex = 0;
                        } else {
                            if (stack[stackIndex] instanceof String) {
                                b = -(double) stack[stackIndex + 1];
                                stack[stackIndex] = stack[stackIndex] + "-" + stack[stackIndex + 1];
                            } else if (stack[stackIndex + 1] instanceof String) {
                                b = (double) stack[stackIndex];
                                a = -a;
                                stack[stackIndex] = stack[stackIndex] + "-" + stack[stackIndex + 1];
                            } else {
                                stack[stackIndex] = (double) stack[stackIndex] - (double) stack[stackIndex + 1];
                            }
                        }
                        break;
                    }
                    case "*": {
                        stackIndex--;
                        if (stack[stackIndex] instanceof String) {
                            a = (double) stack[stackIndex + 1];
                            stack[stackIndex] = stack[stackIndex] + "*" + stack[stackIndex + 1];
                        } else if (stack[stackIndex + 1] instanceof String) {
                            a = (double) stack[stackIndex];
                            stack[stackIndex] = stack[stackIndex] + "*" + stack[stackIndex + 1];
                        } else {
                            stack[stackIndex] = (double) stack[stackIndex] * (double) stack[stackIndex + 1];
                        }
                        break;
                    }
                    default: {
                        stack[++stackIndex] = s;
                        break;
                    }
                }
            }
        }

        //处理特殊情况、y=b时候
        if (stack[0] instanceof Double) {
            a = 0;
            b = (double) stack[0];
        }

        return new double[]{a, b};
    }

    public static void main(String[] args) {
        String[] rpn = parseArithmeticToRPN("a+(a+a*b-a)*(a-b)");
        System.out.println(Arrays.toString(rpn));
        System.out.println("a+(a+a*b-a)*(a-b)");
        System.out.println(parseRPNToArithmetic(rpn));
        Map<String, Double> map = new HashMap<>();
        map.put("a", 1d);
        map.put("b", 2d);
        double res = calcRPN_string_double(doWithRpnList_string_double(rpn), map);
        System.out.println(res);
    }
}
