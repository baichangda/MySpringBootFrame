package com.bcd.base.util;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class StringUtil {

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
        char[] arr= str.toCharArray();
        boolean nextIsUpper = false;
        result.append(Character.toLowerCase(arr[0]));
        for (int i = 1; i <= arr.length-1; i++) {
            char c = arr[i];
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
        char[] arr= str.toCharArray();
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i=1;i<=arr.length-1;i++) {
            char c=arr[i];
            if (Character.isUpperCase(c)) {
                result.append(splitStr);
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
    public static Set<String> findStrInParenthesis(String str, String leftStr, String rightStr) {
        Pattern pattern = Pattern.compile(escapeExprSpecialWord(leftStr) + "(.*?)" + escapeExprSpecialWord(rightStr));
        Matcher matcher = pattern.matcher(str);
        Set<String> res = new HashSet<>();
        while (matcher.find()) {
            res.add(matcher.group(1));
        }
        return res;
    }

}
