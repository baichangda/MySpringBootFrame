package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class StringUtil {

    static final Pattern NUMBER_PATTERN=Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");

    /**
     * 将一串包含特殊字符串的换成驼峰模式
     * example:
     * a_b_c会成为aBC
     *
     * @param str
     * @param splitStr
     * @return
     */
    public static String toFirstUpperCaseWithSplit(String str, char splitStr) {
        if (str == null || str.length() == 0) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == splitStr) {
                nextIsUpper = true;
            } else {
                if (nextIsUpper) {
                    result.append(Character.toUpperCase(c));
                    nextIsUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    public static String toFirstSplitWithUpperCase(String str, char splitStr) {
        if (str == null || str.length() == 0) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(splitStr);
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    /**
     * 替换字符串中的${*}格式的变量
     *
     * @param str
     * @param dataMap
     * @return
     */
    public static String replaceStrs(String str, Map<String, String> dataMap) {
        String[] newStr = new String[]{str};
        dataMap.forEach((k, v) ->
                newStr[0] = newStr[0].replaceAll("\\$\\{" + escapeExprSpecialWord(k) + "\\}", escapeExprSpecialWord(v))
        );
        return newStr[0];
    }

    /**
     * 替换字符串中{*}格式变量
     * 模拟I18n的替换规则
     * 从{0}开始...
     *
     * @param str
     * @param params
     * @return
     */
    public static String replaceLikeI18N(String str, Object... params) {
        if (params == null || params.length == 0) {
            return str;
        }
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i <= params.length - 1; i++) {
            paramMap.put("\\{" + i + "\\}", params[i]);
        }
        String[] newStr = new String[]{str};
        paramMap.forEach((k, v) -> newStr[0] = newStr[0].replaceAll(k, v.toString()));
        return newStr[0];
    }

    /**
     * 转义正则特殊字符 $()*+.[]?\^{},|
     *
     * @param str
     * @return
     */
    public static String escapeExprSpecialWord(String str) {
        if (str != null && !"".equals(str)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (str.contains(key)) {
                    str = str.replace(key, "\\" + key);
                }
            }
        }
        return str;
    }

    /**
     * 找出字符串1中字符串2的个数
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int count(String str1, String str2) {
        Pattern pattern = Pattern.compile(escapeExprSpecialWord(str2));
        Matcher matcher = pattern.matcher(str1);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 从给定的字符串中寻找括号包括的字符串
     * 例如:
     * findStrInParenthesis("abcd${xy}e${mn}{aa}","{","}") 寻找出 xy mn aa
     *
     * @param str 字符串值
     * @return
     */
    public static List<String> findStrInParenthesis(String str, String leftStr, String rightStr) {
        Pattern pattern = Pattern.compile(escapeExprSpecialWord(leftStr) + "(.*?)" + escapeExprSpecialWord(rightStr));
        Matcher matcher = pattern.matcher(str);
        List<String> returnList = new ArrayList();
        while (matcher.find()) {
            returnList.add(matcher.group(1));
        }
        return returnList;
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

    /**
     * 计算逆波兰表达式
     * @param list 逆波兰表达式集合
     * @param map 字段和值对应map
     * @return
     */
    public static Double calcRPN(List<String> list,Map<String,Double> map){
        int stackIndex=-1;
        Double[] stack=new Double[list.size()];
        for (String s : list) {
            switch (s){
                case "+":{
                    double num2=stack[stackIndex--];
                    double num1=stack[stackIndex--];
                    stack[++stackIndex]=num1+num2;
                    break;
                }
                case "-":{
                    double num2=stack[stackIndex--];
                    double num1=stack[stackIndex--];
                    stack[++stackIndex]=num1-num2;
                    break;
                }
                case "*":{
                    double num2=stack[stackIndex--];
                    double num1=stack[stackIndex--];
                    stack[++stackIndex]=num1*num2;
                    break;
                }
                case "/":{
                    double num2=stack[stackIndex--];
                    double num1=stack[stackIndex--];
                    stack[++stackIndex]=num1/num2;
                    break;
                }
                default:{
                    //如果是数字
                    if(NUMBER_PATTERN.matcher(s).matches()){
                        stack[++stackIndex]=Double.parseDouble(s);
                    }else{
                        Double val=map.get(s);
                        if(val==null){
                            throw BaseRuntimeException.getException("map val["+s+"] not exists");
                        }
                        stack[++stackIndex]=val;
                    }
                    break;
                }
            }
        }
        return stack[0];
    }

    public static void main(String[] args) {
        List<String> res= parseArithmeticToRPN("n1");
    }
}
