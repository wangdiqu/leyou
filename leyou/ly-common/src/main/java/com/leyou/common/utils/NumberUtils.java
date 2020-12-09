package com.leyou.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-25 09:13
 **/
public class NumberUtils {

    public static boolean isInt(Double num) {
        return num.intValue() == num;
    }

    /**
     * 判断字符串是否是数值格式
     *
     * @param str
     * @return
     */
    public static boolean isDigit(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        return str.matches("^\\d+$");
    }

    /**
     * 将一个小数精确到指定位数
     *
     * @param num
     * @param scale
     * @return
     */
    public static double scale(double num, int scale) {
        BigDecimal bd = new BigDecimal(num);
        return bd.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    // 从字符串中根据正则表达式寻找，返回找到的数字数组
    public static Double[] searchNumber(String value, String regex) {
        List<Double> doubles = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            MatchResult result = matcher.toMatchResult();
            for (int i = 1; i <= result.groupCount(); i++) {
                doubles.add(Double.valueOf(result.group(i)));
            }
        }
        return doubles.toArray(new Double[doubles.size()]);
    }

    /**
     * 生成指定位数的随机数字
     *
     * @param len(1-10)
     * @return
     */
    public static String generateCode(int len) {
        len = Math.min(len, 8);
        int min = Double.valueOf(Math.pow(10, len - 1)).intValue();
        int num = new Random().nextInt(Double.valueOf(Math.pow(10, len + 1)).intValue() - 1) + min;
        return String.valueOf(num).substring(0, len);
    }

    /**
     * 将String类型转换成double类型
     *
     * @param str
     * @return
     */
    public static double toDouble(String str) {
//        double noBeforeDot = 0.0, noAfterDot = 0.0;
//        int noAfterE = 0;
//        double index = 1.0;
//        boolean noGtZeroFlag = true;
//        boolean eNoGtZeroFlag = true;
//        byte posBeforeDot = 0;
//        byte posBeforeE = 0;
//        if (str == null) {
//            return 0.0;
//        }
//        int leng = str.length();
//        if (leng <= 0) {
//            return 0.0;
//        }
//        char temp[];
//        if (str.startsWith("+")) {
//            temp = str.substring(1).toCharArray();
//            leng--;
//        } else if (str.startsWith("-")) {
//            noGtZeroFlag = false;
//            temp = str.substring(1).toCharArray();
//            leng--;
//        } else {
//            temp = str.toCharArray();
//        }
//        for (int i = 0; i < leng; i++) {
//            if (temp[i] >= '0' && temp[i] <= '9') {
//                if (posBeforeE == 0) {
//                    if (posBeforeDot == 0) {
//                        noBeforeDot = noBeforeDot * 10.0 + (temp[i] - 48);
//                    } else if (posBeforeDot == 1) {
//                        index = index * 0.1;
//                        noAfterDot = noAfterDot + (temp[i] - 48) * index;
//                    }
//                } else if (posBeforeE == 1) {
//                    noAfterE = noAfterE * 10 + (temp[i] - 48);
//                }
//            } else if (temp[i] == '.') {
//                posBeforeDot++;
//                if (posBeforeDot > 1) {
//                    return 0.0;
//                }
//            } else if (temp[i] == 'e' || temp[i] == 'E') {
//                posBeforeE++;
//                if (posBeforeE > 1) {
//                    return 0.0;
//                }
//            } else if (temp[i] == '+' || temp[i] == '-') {
//                if (posBeforeE == 1 && temp[i] == '-') {
//                    eNoGtZeroFlag = false;
//                } else {
//                    return 0.0;
//                }
//            } else {
//                return 0.0;
//            }
//        }// end for
//        if (eNoGtZeroFlag == false) {
//            noAfterE = noAfterE * (-1);
//        }
//        if (noGtZeroFlag == false) {
//            return (noBeforeDot + noAfterDot) * Math.pow(10, noAfterE * 1.0) * (-1.0);
//        } else {
//            return (noBeforeDot + noAfterDot) * Math.pow(10, noAfterE * 1.0);
//        }
        double v=0.0;
        try {
            v = Double.parseDouble(str);
        }catch (Exception e){
            e.getStackTrace();
        }
        return v;
    }

}
