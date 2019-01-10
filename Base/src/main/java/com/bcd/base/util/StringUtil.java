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
     * @param str
     * @param splitStr
     * @return
     */
    public static String toFirstUpperCaseWithSplit(String str,char splitStr){
        if(str==null||str.length()==0){
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == splitStr) {
                nextIsUpper = true;
            }
            else {
                if (nextIsUpper) {
                    result.append(Character.toUpperCase(c));
                    nextIsUpper = false;
                }
                else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 替换字符串中的${*}格式的变量
     * @param str
     * @param dataMap
     * @return
     */
    public static String replaceStrs(String str, Map<String,String> dataMap){
        String[] newStr=new String[]{str};
        dataMap.forEach((k,v)->
            newStr[0]=newStr[0].replaceAll("\\$\\{"+escapeExprSpecialWord(k)+"\\}",escapeExprSpecialWord(v))
        );
        return newStr[0];
    }

    /**
     * 替换字符串中{*}格式变量
     * 模拟I18n的替换规则
     * 从{0}开始...
     * @param str
     * @param params
     * @return
     */
    public static String replaceLikeI18N(String str,Object...params){
        if(params==null||params.length==0){
            return str;
        }
        Map<String,Object> paramMap=new HashMap<>();
        for(int i=0;i<=params.length-1;i++){
            paramMap.put("\\{"+i+"\\}",params[i]);
        }
        String[] newStr=new String[]{str};
        paramMap.forEach((k,v)->
                newStr[0]=newStr[0].replaceAll(k,v.toString())
        );
        return newStr[0];
    }

    /**
     * 转义正则特殊字符 $()*+.[]?\^{},|
     *
     * @param str
     * @return
     */
    public static String escapeExprSpecialWord(String str) {
        if (str!=null&&!"".equals(str)) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
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
     * @param str1
     * @param str2
     * @return
     */
    public static int count(String str1,String str2){
        Pattern pattern=Pattern.compile(escapeExprSpecialWord(str2));
        Matcher matcher= pattern.matcher(str1);
        int count=0;
        while (matcher.find()){
            count++;
        }
        return count;
    }

    /**
     * 从给定的字符串中寻找括号包括的字符串
     * 例如:
     * findStrInParenthesis("abcd${xy}e${mn}{aa}","{","}") 寻找出 xy mn aa
     * @param str 字符串值
     * @return
     */
    public static List<String> findStrInParenthesis(String str,String leftStr,String rightStr){
        Pattern pattern=Pattern.compile(escapeExprSpecialWord(leftStr)+"(.*?)"+escapeExprSpecialWord(rightStr));
        Matcher matcher=pattern.matcher(str);
        List<String> returnList=new ArrayList();
        while(matcher.find()){
            returnList.add(matcher.group(1));
        }
        return returnList;
    }
}
