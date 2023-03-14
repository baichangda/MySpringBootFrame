package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RpnUtil {

    public static void main(String[] args) {
//        Map<String, Double> dataMap = new HashMap<>();
//        dataMap.put("a", 1d);
//        dataMap.put("b", 3.4d);
//        dataMap.put("x", 0.5d);
//        dataMap.put("y", 2d);
//        dataMap.put("hello", 2d);
//        System.out.println(calc("(a+1)*(b+2)*(y+3)/(x-1)", dataMap));
//        System.out.println(toExpr(toRpn("(a+1)*(b+2)*(y+3)/(x-1)")));
//        System.out.println(toExpr(toRpn("--(a/-3.11-4.2)--d")));
//        System.out.println(toExpr(toRpn("(-1+3)*(-10.4-x)/2-1.5")));
//        System.out.println(calc("x", dataMap));
//        System.out.println(calc("x-0", dataMap));
//        System.out.println(calc("2*-x-3", dataMap));
//        System.out.println(calc("-((x+3)/2)", dataMap));
//        System.out.println(calc("--(a/-3.11-4.2)--b", dataMap));
//        System.out.println(calc("-(-x+3)/5", dataMap));
//        System.out.println(calc("-(-hello+3)/5*hello", dataMap));
//        System.out.println(Arrays.toString(toRpn("(a+1)*(b+2)*(y+3)/(x-1)")));
//        System.out.println(Arrays.toString(toRpn("x")));
//        System.out.println(Arrays.toString(toRpn("-y")));
//        System.out.println(Arrays.toString(toRpn("2*-x-3")));
//        System.out.println(Arrays.toString(toRpn("-((x+3)/2)")));
//        System.out.println(Arrays.toString(toRpn("2/(-x*4-3)")));
//        System.out.println(Arrays.toString(toRpn("-(a-(b+(c)))")));
//        System.out.println(Arrays.toString(toRpn("-2*x+3")));
//        System.out.println(Arrays.toString(toRpn("(y-3)/-2")));
//        System.out.println(Arrays.toString(toRpn("1-4.232")));
//        System.out.println(Arrays.toString(toRpn("---4")));
//        System.out.println(Arrays.toString(toRpn("--4")));
//        System.out.println(Arrays.toString(toRpn("--(a/-3.11-4.2)--d")));
//        System.out.println(Arrays.toString(toRpn("(a-(b+(c)))")));
//        System.out.println(Arrays.toString(toRpn("(a-(b+(c)))")));
//        System.out.println(Arrays.toString(toRpn("-3+-x-1")));
//        System.out.println(Arrays.toString(toRpn("-3*-x-1")));
//        System.out.println(Arrays.toString(toRpn("-(-x+3)/5")));
//        System.out.println(Arrays.toString(toRpn("(-x+3)/5")));
//        System.out.println(Arrays.toString(toRpn("--(a/-3-4)--d")));

//        System.out.println(reverseExpr("x/10-1000"));
//        System.out.println(reverseExpr("-x"));
//        System.out.println(reverseExpr("(y-3)/-2"));
//        System.out.println(reverseExpr("2/(-x*4-3)"));
//        System.out.println(reverseExpr("-2*x+3"));
//        System.out.println(reverseExpr("--(x/-3-4)"));
//        System.out.println(reverseExpr("(-x+3)/5"));
//        System.out.println(reverseExpr("(-1+3)*(-10.4-(-x))/2-1.5*(2-1.3)"));
    }

    /**
     * 对单变量算术表达式推导求解、例如
     * y=a*x+b、求x对y的推导
     * 结果位 y=(x-b)/a
     *
     * @param str
     */
    public static String reverseExpr(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("param[str] must not be null or empty");
        }
        List<String> operateList = new ArrayList<>();
        final Ele[] eles = toRpn(str);
        //将其中的变量替换为x
        for (int i = 0; i < eles.length; i++) {
            final Ele ele = eles[i];
            if (ele.s != null && !("+").equals(ele.s) && !("-").equals(ele.s) && !("*").equals(ele.s) && !("/").equals(ele.s) && !("!").equals(ele.s)) {
                eles[i] = new Ele("x");
            }
        }
        final String[] stack = new String[eles.length];
        int stackIndex = -1;
        for (Ele ele : eles) {
            if (ele.s == null) {
                if (ele.d == (int) ele.d) {
                    stack[++stackIndex] = ((int) ele.d) + "";
                } else {
                    stack[++stackIndex] = ele.d + "";
                }
            } else {
                switch (ele.s) {
                    case "!": {
                        if (stack[stackIndex].contains("x")) {
                            operateList.add("x,!");
                        }
                        stack[stackIndex] = "(-" + stack[stackIndex] + ")";
                        break;
                    }
                    case "+": {
                        if (stack[stackIndex - 1].contains("x")) {
                            operateList.add("x," + stack[stackIndex] + ",+");
                        } else if (stack[stackIndex].contains("x")) {
                            operateList.add("x," + stack[stackIndex - 1] + ",+");
                        }
                        stack[stackIndex - 1] = "(" + stack[stackIndex - 1] + "+" + stack[stackIndex] + ")";
                        stackIndex--;

                        break;
                    }
                    case "-": {
                        if (stack[stackIndex - 1].contains("x")) {
                            operateList.add("x," + stack[stackIndex] + ",-");
                        } else if (stack[stackIndex].contains("x")) {
                            operateList.add(stack[stackIndex - 1] + ",x,-");
                        }
                        stack[stackIndex - 1] = "(" + stack[stackIndex - 1] + "-" + stack[stackIndex] + ")";
                        stackIndex--;

                        break;
                    }
                    case "*": {
                        if (stack[stackIndex - 1].contains("x")) {
                            operateList.add("x," + stack[stackIndex] + ",*");
                        } else if (stack[stackIndex].contains("x")) {
                            operateList.add("x," + stack[stackIndex - 1] + ",*");
                        }
                        stack[stackIndex - 1] = stack[stackIndex - 1] + "*" + stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    case "/": {
                        if (stack[stackIndex - 1].contains("x")) {
                            operateList.add("x," + stack[stackIndex] + ",/");
                        } else if (stack[stackIndex].contains("x")) {
                            operateList.add(stack[stackIndex - 1] + ",x,/");
                        }
                        stack[stackIndex - 1] = stack[stackIndex - 1] + "/" + stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    default: {
                        stack[++stackIndex] = ele.s;
                    }
                }
            }
        }

//        System.out.println(operateList.stream().reduce((e1, e2) -> e1 + "\n" + e2).get());

        StringBuilder sb = new StringBuilder("x");
        for (int i = operateList.size() - 1; i >= 0; i--) {
            final String op = operateList.get(i);
            final String[] split = op.split(",");
            if (split.length == 2) {
                sb.insert(0, "-");
            } else {
                final String s0 = split[0];
                final String s1 = split[1];
                final String s2 = split[2];
                switch (s2) {
                    case "+": {
                        sb.insert(0, "(").append("-");
                        if ("x".equals(s0)) {
                            //y=x+expr
                            sb.append(s1);
                        } else {
                            //y=expr+x
                            sb.append(s0);
                        }
                        sb.append(")");
                        break;
                    }
                    case "-": {
                        if ("x".equals(s0)) {
                            //y=x-expr
                            sb.insert(0, "(").append("+").append(s1).append(")");
                        } else {
                            //y=expr-x
                            sb.insert(0, "(" + s0 + "-").append(")");
                        }
                        break;
                    }
                    case "*": {
                        if ("x".equals(s0)) {
                            //y=x*expr
                            sb.append("/").append(s1);
                        } else {
                            //y=expr*x
                            sb.append("/").append(s0);
                        }
                        break;
                    }
                    case "/": {
                        if ("x".equals(s0)) {
                            //y=x/expr
                            sb.append("*").append(s1);
                        } else {
                            //y=expr/x
                            sb.insert(0, s0 + "/");
                        }
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String toExpr(Ele[] eles) {
        if (eles == null || eles.length==0) {
            throw new IllegalArgumentException("param[eles] must not be null or empty");
        }
        final String[] stack = new String[eles.length];
        int stackIndex = -1;
        for (Ele ele : eles) {
            if (ele.s == null) {
                if (ele.d == (int) ele.d) {
                    stack[++stackIndex] = ((int) ele.d) + "";
                } else {
                    stack[++stackIndex] = ele.d + "";
                }

            } else {
                switch (ele.s) {
                    case "!": {
                        stack[stackIndex] = "(-" + stack[stackIndex] + ")";
                        break;
                    }
                    case "+": {
                        stack[stackIndex - 1] = "(" + stack[stackIndex - 1] + "+" + stack[stackIndex] + ")";
                        stackIndex--;
                        break;
                    }
                    case "-": {
                        stack[stackIndex - 1] = "(" + stack[stackIndex - 1] + "-" + stack[stackIndex] + ")";
                        stackIndex--;
                        break;
                    }
                    case "*": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] + "*" + stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    case "/": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] + "/" + stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    default: {
                        stack[++stackIndex] = ele.s;
                    }
                }
            }
        }
        return stack[0];
    }


    static class Ele {
        //存储变量名称、null代表是数字
        final String s;
        //存储数字值
        final double d;

        public Ele(String s) {
            this.s = s;
            this.d = 0d;
        }

        public Ele(double d) {
            this.s = null;
            this.d = d;
        }

        @Override
        public String toString() {
            return s == null ? ("num[" + d + "]") : "str[" + s + "]";
        }
    }

    public static double calc(String str, Map<String, Double> dataMap) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("param[str] must not be null or empty");
        }
        return calc(toRpn(str), dataMap);
    }

    public static double calc(Ele[] eles, Map<String, Double> dataMap) {
        if (eles == null || eles.length==0) {
            throw new IllegalArgumentException("param[eles] must not be null or empty");
        }
        final double[] stack = new double[eles.length];
        int stackIndex = -1;
        for (Ele ele : eles) {
            if (ele.s == null) {
                stack[++stackIndex] = ele.d;
            } else {
                switch (ele.s) {
                    case "!": {
                        stack[stackIndex] = -stack[stackIndex];
                        break;
                    }
                    case "+": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] + stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    case "-": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] - stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    case "*": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] * stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    case "/": {
                        stack[stackIndex - 1] = stack[stackIndex - 1] / stack[stackIndex];
                        stackIndex--;
                        break;
                    }
                    default: {
                        final Double d = dataMap.get(ele.s);
                        if (d == null) {
                            throw BaseRuntimeException.getException("var[{}] not exist", ele.s);
                        }
                        stack[++stackIndex] = d;
                    }
                }
            }
        }
        return stack[0];
    }

    private static Ele[] toRpn(String[] rpn) {
        final Ele[] eles = new Ele[rpn.length];
        for (int i = 0; i < rpn.length; i++) {
            try {
                final double v = Double.parseDouble(rpn[i]);
                eles[i] = new Ele(v);
            } catch (NumberFormatException ex) {
                eles[i] = new Ele(rpn[i]);
            }
        }
        return eles;
    }

    /**
     * 算术表达式转换为 逆波兰表达式形式
     * 支持 ( + - * / ) 和 负号
     * 支持变量
     * 结果中
     * 负号将会被翻译成!
     * 所有的数字没有负数、因为负号被提出来为!后置
     *
     * @param str
     * @return
     */
    public static Ele[] toRpn(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("param[str] must not be null or empty");
        }
        final char[] chars = str.toCharArray();
        //存储rpn结果
        final List<String> output = new ArrayList<>();
        //存储操作符的栈
        final char[] operatorStack = new char[chars.length];
        int operatorStackIndex = -1;
        //用于收集一个个字符、整理成一个变量
        final StringBuilder temp = new StringBuilder();

        for (int i = 0; i <= chars.length - 1; i++) {
            final char c = chars[i];
            switch (c) {
                case '+':
                case '*':
                case '/': {
                    if (temp.length() > 0) {
                        //当遇到符号、收集变量
                        output.add(temp.toString());
                        temp.delete(0, temp.length());
                    }
                    //开始回溯符号栈、将优先级高于当前符号的符号弹出、视为优先运算
                    if (operatorStackIndex >= 0) {
                        while (operatorStackIndex >= 0 && operatorStack[operatorStackIndex] != '(' &&
                                getSymbolPriority(operatorStack[operatorStackIndex]) >= getSymbolPriority(c)) {
                            output.add(String.valueOf(operatorStack[operatorStackIndex--]));
                        }
                    }
                    //符号入栈
                    operatorStack[++operatorStackIndex] = c;
                    break;
                }
                case '-': {
                    //两种情况下表示不是取负
                    //1、temp里面有数据、代表有变量
                    //2、上一个字符是)、代表刚处理完一个()表达式
                    if (temp.length() == 0 && (i == 0 || chars[i - 1] != ')')) {
                        //此时取负号
                        operatorStack[++operatorStackIndex] = '!';
                    } else {
                        //当遇到符号、收集变量
                        if (temp.length() > 0) {
                            output.add(temp.toString());
                            temp.delete(0, temp.length());
                        }
                        //开始回溯符号栈、将优先级高于当前符号的符号弹出、视为优先运算
                        if (operatorStackIndex >= 0) {
                            while (operatorStackIndex >= 0 && operatorStack[operatorStackIndex] != '(' &&
                                    getSymbolPriority(operatorStack[operatorStackIndex]) >= getSymbolPriority(c)) {
                                output.add(String.valueOf(operatorStack[operatorStackIndex--]));
                            }
                        }
                        //符号入栈
                        operatorStack[++operatorStackIndex] = c;
                    }
                    break;
                }
                case '(': {
                    //符号入栈
                    operatorStack[++operatorStackIndex] = c;
                    break;
                }
                case ')': {
                    if (temp.length() > 0) {
                        //当遇到)、收集变量
                        output.add(temp.toString());
                        temp.delete(0, temp.length());
                    }
                    //处理括号内
                    //开始回溯符号栈、将优先级高于当前符号的符号弹出、视为优先运算
                    if (operatorStackIndex >= 0) {
                        while (operatorStackIndex >= 0 && operatorStack[operatorStackIndex] != '(') {
                            output.add(String.valueOf(operatorStack[operatorStackIndex--]));
                        }
                        //弹出(
                        if (operatorStackIndex != -1) {
                            operatorStackIndex--;
                        }
                    }
                    break;
                }

                default: {
                    temp.append(c);
                }
            }
        }

        if (temp.length() > 0) {
            output.add(temp.toString());
        }

        for (int i = operatorStackIndex; i >= 0; i--) {
            output.add(String.valueOf(operatorStack[i]));
        }

        //去除重复的!、负负得正
        final List<String> newOutput = new ArrayList<>();
        int count = 0;
        for (final String s : output) {
            if ("!".equals(s)) {
                count++;
            } else {
                if (count % 2 == 1) {
                    newOutput.add("!");
                }
                count = 0;
                newOutput.add(s);
            }
        }
        if (count % 2 == 1) {
            newOutput.add("!");
        }

        return toRpn(newOutput.toArray(new String[0]));
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
            case '!': {
                return 3;
            }
            default: {
                throw BaseRuntimeException.getException("symbol[" + c + "] not support");
            }
        }
    }


}
