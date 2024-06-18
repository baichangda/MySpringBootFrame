package com.bcd.base.util;

import org.slf4j.helpers.MessageFormatter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * 将一串包含特殊字符串的换成驼峰模式
     * example:
     * a_b_c会成为aBC
     *
     * @param str
     * @param splitChar
     * @return
     */
    public static String splitCharToCamelCase(String str, char splitChar) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        char[] arr = str.toCharArray();
        boolean nextIsUpper = false;
        result.append(Character.toLowerCase(arr[0]));
        for (int i = 1; i <= arr.length - 1; i++) {
            char c = arr[i];
            if (c == splitChar) {
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

    /**
     * 将驼峰格式字符串转换为分隔符格式
     *
     * @param str
     * @param splitChar
     * @return
     */
    public static String camelCaseToSplitChar(String str, char splitChar) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        char[] arr = str.toCharArray();
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i <= arr.length - 1; i++) {
            char c = arr[i];
            if (Character.isUpperCase(c)) {
                result.append(splitChar);
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    /**
     * 转义正则特殊字符 $()*+.[]?\^{},|
     *
     * @param str
     * @return
     */
    public static String escapeExprSpecialWord(String str) {
        if (str != null && !str.isEmpty()) {
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
    public static Set<String> findStrInParenthesis(String str, String leftStr, String rightStr) {
        Pattern pattern = Pattern.compile(escapeExprSpecialWord(leftStr) + "(.*?)" + escapeExprSpecialWord(rightStr));
        Matcher matcher = pattern.matcher(str);
        Set<String> res = new HashSet<>();
        while (matcher.find()) {
            res.add(matcher.group(1));
        }
        return res;
    }

    /**
     * 将信息转换为格式化
     * 使用方式和sl4j log一样、例如
     * {@link org.slf4j.Logger#info(String, Object...)}
     * 如果需要转义、则\\{}
     *
     * @param message
     * @param params
     * @return
     */
    public static String format(String message, Object... params) {
        return MessageFormatter.arrayFormat(message, params, null).getMessage();
    }

    public static String format(String message, Object arg) {
        return MessageFormatter.format(message, arg).getMessage();
    }

    public static String format(String message, Object arg1, Object arg2) {
        return MessageFormatter.format(message, arg1, arg2).getMessage();
    }

    public static void main(String[] args) {
        System.out.println(format("{}-{}", 123, null));
        System.out.println(format("{}-\\{}-{}", 123, null, "abc"));
    }

}
