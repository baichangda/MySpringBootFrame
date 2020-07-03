package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RpnUtil {

    /**
     * 计算字符串公式
     * 支持 = - * / ( )
     * @param str
     * @param map
     * @return
     */
    public static Double calcArithmetic(String str, Map<String,Double> map){
        return calcRPN_string_string(parseArithmeticToRPN(str),map);
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> string
     * 将数字字符串 --> double
     * @param rpnList rpn表达式集合
     * @return
     */
    public static List doWithRpnList_string_double(List<String> rpnList){
        return rpnList.stream().map(e->{
            try {
                return Double.parseDouble(e);
            }catch (NumberFormatException ex){
                return e;
            }
        }).collect(Collectors.toList());
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> char
     * 将数字字符串 --> double
     * @param rpnList rpn表达式集合
     * @return
     */
    public static List doWithRpnList_char_double(List<String> rpnList){
        return rpnList.stream().map(e->{
            try {
                return Double.parseDouble(e);
            }catch (NumberFormatException ex){
                return e.charAt(0);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 处理rpn表达式集合
     * 字符串变量 --> char
     * 将数字字符串 --> int
     * @param rpnList rpn表达式集合
     * @return
     */
    public static List doWithRpnList_char_int(List<String> rpnList){
        return rpnList.stream().map(e->{
            try {
                return Integer.parseInt(e);
            }catch (NumberFormatException ex){
                return e.charAt(0);
            }
        }).collect(Collectors.toList());
    }

    /**
     * list中变量定义必须是char 支持 a-z A-Z
     *
     * A-Z --> 65-90
     * a-z --> 97-122
     * 所以char数组长度为 65-122 长度为58
     * 同时需要进行偏移量计算也就是 字符-65
     *
     * @param list rpn表达式集合,其中变量必须是char,常量必须是int
     * @param vals 变量对应值数组,取值规则为 vals[int(char)-offset]
     * @param offset 代表char对应的数字在vals的偏移量
     * @return
     */
    public static int calcRPN_char_int(List list, int[] vals,int offset){
        int stackIndex=-1;
        int[] stack=new int[list.size()];
        for (Object s : list) {
            if(s instanceof Integer){
                stack[++stackIndex]=(int)s;
            }else {
                switch ((char)s) {
                    case '+': {
                        int num2 = stack[stackIndex--];
                        int num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 + num2;
                        break;
                    }
                    case '-': {
                        int num2 = stack[stackIndex--];
                        int num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 - num2;
                        break;
                    }
                    case '*': {
                        int num2 = stack[stackIndex--];
                        int num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 * num2;
                        break;
                    }
                    case '/': {
                        int num2 = stack[stackIndex--];
                        int num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 / num2;
                        break;
                    }
                    default: {
                        int val = vals[(char)s-offset];
                        stack[++stackIndex] = val;
                        break;
                    }
                }
            }
        }
        return stack[0];
    }

    /**
     * 计算逆波兰表达式
     * @param list 逆波兰表达式集合,其中变量必须是string,常量必须是double
     * @param map 字段和值对应map
     * @return
     */
    public static double calcRPN_string_double(List list, Map<String,Double> map){
        int stackIndex=-1;
        double[] stack=new double[list.size()];
        for (Object o : list) {
            if(o instanceof Double){
                stack[++stackIndex] = (double)o;
            }else {
                switch ((String)o) {
                    case "+": {
                        double num2 = stack[stackIndex--];
                        double num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 + num2;
                        break;
                    }
                    case "-": {
                        double num2 = stack[stackIndex--];
                        double num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 - num2;
                        break;
                    }
                    case "*": {
                        double num2 = stack[stackIndex--];
                        double num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 * num2;
                        break;
                    }
                    case "/": {
                        double num2 = stack[stackIndex--];
                        double num1 = stack[stackIndex--];
                        stack[++stackIndex] = num1 / num2;
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
     * @param list 逆波兰表达式集合,其中变量必须是string,常量也是string
     * @param map 字段和值对应map
     * @return
     */
    public static double calcRPN_string_string(List<String> list, Map<String,Double> map){
        int stackIndex=-1;
        double[] stack=new double[list.size()];
        for (String s : list) {
            switch (s) {
                case "+": {
                    double num2 = stack[stackIndex--];
                    double num1 = stack[stackIndex--];
                    stack[++stackIndex] = num1 + num2;
                    break;
                }
                case "-": {
                    double num2 = stack[stackIndex--];
                    double num1 = stack[stackIndex--];
                    stack[++stackIndex] = num1 - num2;
                    break;
                }
                case "*": {
                    double num2 = stack[stackIndex--];
                    double num1 = stack[stackIndex--];
                    stack[++stackIndex] = num1 * num2;
                    break;
                }
                case "/": {
                    double num2 = stack[stackIndex--];
                    double num1 = stack[stackIndex--];
                    stack[++stackIndex] = num1 / num2;
                    break;
                }
                default: {
                    Number val = map.get(s);
                    if(val==null){
                        stack[++stackIndex] = Double.parseDouble(s);
                    }else{
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
     * @return
     */
    public static List<String> parseArithmeticToRPN(String str){
        List<String> output=new ArrayList<>();
        int stackIndex=-1;
        char[] stack=new char[str.length()];
        char[] arr= str.toCharArray();
        StringBuilder temp=new StringBuilder();
        for(int i=0;i<=arr.length-1;i++){
            if(arr[i]=='+'||arr[i]=='-'||arr[i]=='*'||arr[i]=='/'){
                if(temp.length()>0) {
                    output.add(temp.toString());
                    temp.delete(0, temp.length());
                }
                if(stackIndex>=0){
                    while(stack[stackIndex]!='('&&get(stack[stackIndex])>=get(arr[i])){
                        output.add(String.valueOf(stack[stackIndex--]));
                        if(stackIndex==-1){
                            break;
                        }
                    }
                }
                stack[++stackIndex]=arr[i];
            }else if(arr[i]=='('){
                stack[++stackIndex]=arr[i];
            }else if(arr[i]==')'){
                if(temp.length()>0) {
                    output.add(temp.toString());
                    temp.delete(0, temp.length());
                }
                while(stackIndex>=0){
                    char c= stack[stackIndex--];
                    if(c=='('){
                        break;
                    }
                    output.add(String.valueOf(c));
                }
            }else{
                temp.append(arr[i]);
            }
        }

        if(temp.length()>0){
            output.add(temp.toString());
        }

        for(int i=stackIndex;i>=0;i--){
            output.add(String.valueOf(stack[i]));
        }

        return output;
    }

    /**
     * 获取字符优先级
     * @param c
     * @return
     */
    private static int get(char c){
        switch (c){
            case '+':{
                return 1;
            }
            case '-':{
                return 1;
            }
            case '*':{
                return 2;
            }
            case '/':{
                return 2;
            }
            default:{
                throw BaseRuntimeException.getException("symbol["+c+"] not support");
            }
        }
    }
}
