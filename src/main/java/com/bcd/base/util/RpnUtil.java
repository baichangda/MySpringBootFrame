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
     * 负的字符变量 --> string
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
                if (e.length() == 1) {
                    return e.charAt(0);
                } else {
                    return e;
                }
            }
        }).toArray();
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> char
     * 负的字符变量 --> string
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
                if (e.length() == 1) {
                    return e.charAt(0);
                } else {
                    return e;
                }
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
     * @param rpn  rpn表达式集合,其中正变量必须是char、负变量是string、`常量必须是int
     * @param vals 变量对应值数组,取值规则为 vals[int(char)]
     * @return
     */
    public static int calcRPN_char_int(Object[] rpn, int[] vals) {
        int stackIndex = -1;
        final int[] stack = new int[rpn.length];
        for (Object s : rpn) {
            if (s instanceof Integer) {
                stack[++stackIndex] = (int) s;
            } else if (s instanceof String) {
                stack[++stackIndex] = -vals[((String) s).charAt(1)];
            } else {
                switch ((char) s) {
                    case '+': {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] + stack[stackIndex + 1];
                        break;
                    }
                    case '-': {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] - stack[stackIndex + 1];
                        break;
                    }
                    case '*': {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] * stack[stackIndex + 1];
                        break;
                    }
                    case '/': {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] / stack[stackIndex + 1];
                        break;
                    }
                    default: {
                        stack[++stackIndex] = vals[(char) s];
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
                String s = (String) o;
                switch (s) {
                    case "+": {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] + stack[stackIndex + 1];
                        break;
                    }
                    case "-": {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] - stack[stackIndex + 1];
                        break;
                    }
                    case "*": {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] * stack[stackIndex + 1];
                        break;
                    }
                    case "/": {
                        stackIndex--;
                        stack[stackIndex] = stack[stackIndex] / stack[stackIndex + 1];
                        break;
                    }
                    default: {
                        if (s.charAt(0) == '-') {
                            stack[++stackIndex] = -map.get(s.substring(1));
                        } else {
                            stack[++stackIndex] = map.get(o);
                        }
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
                    stack[stackIndex] = stack[stackIndex] + stack[stackIndex + 1];
                    break;
                }
                case "-": {
                    stackIndex--;
                    stack[stackIndex] = stack[stackIndex] - stack[stackIndex + 1];
                    break;
                }
                case "*": {
                    stackIndex--;
                    stack[stackIndex] = stack[stackIndex] * stack[stackIndex + 1];
                    break;
                }
                case "/": {
                    stackIndex--;
                    stack[stackIndex] = stack[stackIndex] / stack[stackIndex + 1];
                    break;
                }
                default: {
                    try {
                        stack[stackIndex] = Double.parseDouble(s);
                        stackIndex++;
                    } catch (NumberFormatException ex) {
                        if (s.charAt(0) == '-') {
                            stack[++stackIndex] = -map.get(s.substring(1));
                        } else {
                            stack[++stackIndex] = map.get(s);
                        }
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
        int stackIndex = -1;
        char[] stack = new char[str.length()];
        char[] arr = str.toCharArray();
        StringBuilder temp = new StringBuilder();
        boolean nextNegative = false;
        for (int i = 0; i <= arr.length - 1; i++) {
            char cur = arr[i];
            if (cur == '+' || cur == '-' || cur == '*' || cur == '/') {
                /**
                 * 判断是否有num或者var
                 */
                if (temp.length() > 0) {
                    output.add(temp.toString());
                    temp.delete(0, temp.length());
                    /**
                     * 开始回溯符号栈、将优先级高于当前符号的符号弹出、视为优先运算
                     */
                    if (stackIndex >= 0) {
                        while (stack[stackIndex] != '(' && getSymbolPriority(stack[stackIndex]) >= getSymbolPriority(cur)) {
                            output.add(String.valueOf(stack[stackIndex--]));
                            if (stackIndex == -1) {
                                break;
                            }
                        }
                    }
                    stack[++stackIndex] = cur;
                } else {
                    /**
                     * 没有num和var、说明运算符左边没有变量、此时有两种情况
                     * 1: 左边的变量是一个()包住的表达式
                     * 2: 这个符号为-、是对下一个值取负
                     * 其他情况说明异常
                     */
                    if (i > 0 && arr[i - 1] == ')') {
                        /**
                         * 没有num和var且当前符号不为负号、则说明可能刚结束了一个()导致变量被清空
                         * 开始回溯符号栈、将优先级高于当前符号的符号弹出、视为优先运算
                         */
                        if (stackIndex >= 0) {
                            while (stack[stackIndex] != '(' && getSymbolPriority(stack[stackIndex]) >= getSymbolPriority(cur)) {
                                output.add(String.valueOf(stack[stackIndex--]));
                                if (stackIndex == -1) {
                                    break;
                                }
                            }
                        }
                        stack[++stackIndex] = cur;
                    } else {
                        if (cur == '-') {
                            /**
                             * 没有num和var且当前符号为负号、说明此时是对下一个数取负
                             */
                            if (nextNegative) {
                                //负负得正
                                nextNegative = false;
                            } else {
                                nextNegative = true;
                            }
                        } else {
                            throw BaseRuntimeException.getException("parse error on index[{}]、operator[{}] left has no var or value", i, cur);
                        }
                    }

                }
            } else if (cur == '(') {
                /**
                 * 如果遇到()、则将其中的表达式视为独立
                 * 截取出其中的字符串、递归调用
                 * 最后添加到结果中
                 */
                int count = 0;
                int end = -1;
                for (int j = i + 1; j < arr.length; j++) {
                    if (arr[j] == '(') {
                        count++;
                    } else {
                        if (arr[j] == ')') {
                            if (count == 0) {
                                end = j;
                                break;
                            } else {
                                count--;
                            }
                        }
                    }
                }
                String[] curRes = parseArithmeticToRPN(new String(arr, i + 1, end - i - 1));
                /**
                 * 如果括号外面为负号则
                 * -num -> num
                 * num -> -num
                 * -var -> var
                 * var -> -var
                 */
                if (nextNegative) {
                    for (String s : curRes) {
                        switch (s) {
                            case "+":
                            case "-":
                            case "*":
                            case "/": {
                                output.add(s);
                                break;
                            }
                            default: {
                                if (s.charAt(0) == '-') {
                                    output.add(s.substring(1));
                                } else {
                                    output.add("-" + s);
                                }
                                break;
                            }
                        }
                    }
                } else {
                    output.addAll(Arrays.asList(curRes));
                }
                i = end;
            } else {
                /**
                 * 如果有负数标识、则先打上负号
                 */
                if (nextNegative) {
                    temp.append("-");
                    nextNegative = false;
                }
                temp.append(cur);
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
            case '+':
            case '-': {
                return 1;
            }
            case '*':
            case '/': {
                return 2;
            }
            default: {
                throw BaseRuntimeException.getException("symbol[" + c + "] not support");
            }
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
                    if ((p2 != -1 && p2 < curSymbolPriority) ||
                            s.equals("-") && s2.charAt(0) != '(' && (s2.contains("+") || s2.contains("-"))
                    ) {
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
     * 解析如下表达式
     * y=a*x+b
     * y=a/x+b
     * 其中
     * a、b没有限制
     * <p>
     * 例如:
     * y=3*x+2
     * y=2-x*4
     * y=-3*-x-1
     * y=-x
     * y=1
     *
     * @param expr
     * @return [a, b]
     */
    public static double[] parseSimpleExpr(String expr) {
        double a = 0;
        double b = 0;
        String[] rpn = parseArithmeticToRPN(expr);
        int index1 = -1;
        int index2 = -1;
        for (int i = 0; i < rpn.length; i++) {
            switch (rpn[i]) {
                case "*":
                case "/": {
                    index1 = i;
                    break;
                }
                case "+":
                case "-": {
                    index2 = i;
                    break;
                }
            }
        }

        if (index1 == -1 && index2 == -1) {
            try {
                b = Double.parseDouble(rpn[0]);
            } catch (NumberFormatException ex) {
                if (rpn[0].charAt(0) == '-') {
                    a = -1;
                } else {
                    a = 1;
                }
            }
        } else if (index1 != -1 && index2 != -1) {
            //求a
            String s1 = rpn[index1 - 2];
            String s2 = rpn[index1 - 1];
            try {
                double d1 = Double.parseDouble(s1);
                if (s2.charAt(0) == '-') {
                    a = -d1;
                } else {
                    a = d1;
                }
            } catch (NumberFormatException ex) {
                double d2 = Double.parseDouble(s2);
                if (s1.charAt(0) == '-') {
                    a = -d2;
                } else {
                    a = d2;
                }
            }
            //求b
            if (index2 == index1 + 1) {
                b = Double.parseDouble(rpn[0]);
                if (rpn[index2].equals("-")) {
                    a = -a;
                }
            } else {
                b = Double.parseDouble(rpn[index2 - 1]);
                if (rpn[index2].equals("-")) {
                    b = -b;
                }
            }

        } else if (index1 != -1 && index2 == -1) {
            String s1 = rpn[index1 - 2];
            String s2 = rpn[index1 - 1];
            try {
                double d1 = Double.parseDouble(s1);
                if (s2.charAt(0) == '-') {
                    a = -d1;
                } else {
                    a = d1;
                }
            } catch (NumberFormatException ex) {
                double d2 = Double.parseDouble(s2);
                if (s1.charAt(0) == '-') {
                    a = -d2;
                } else {
                    a = d2;
                }
            }
        } else {
            String s1 = rpn[index2 - 2];
            String s2 = rpn[index2 - 1];
            try {
                b = Double.parseDouble(s1);
                if (s2.charAt(0) == '-') {
                    a = -1;
                } else {
                    a = 1;
                }
                if (rpn[index2].equals("-")) {
                    a = -a;
                }
            } catch (NumberFormatException ex) {
                b = Double.parseDouble(s2);
                if (s1.charAt(0) == '-') {
                    a = -1;
                } else {
                    a = 1;
                }
                if (rpn[index2].equals("-")) {
                    b = -b;
                }
            }
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
        System.out.println(calcArithmetic("a+(a+a*b-a)*(a-b)",map));
    }
}
