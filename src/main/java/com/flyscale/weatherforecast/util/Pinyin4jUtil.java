package com.flyscale.weatherforecast.util;

import android.text.TextUtils;
import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by bian on 2018/9/18.
 */

public class Pinyin4jUtil {
    private static final String TAG = "Pinyin4jUtil";

    //需要重新转化的多音字，格式:{汉字,误读,正确读音}
    private static String[][] CONVERT_LIST = new String[][]{
            {"重", "zhong", "chong",}
    };

    /**
     * 获得汉语拼音首字母
     *
     * @param chines 汉字
     * @return
     */
    public static String getAlpha(String chines) {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char aNameChar : nameChar) {
            if (aNameChar > 128) {
                try {
                    pinyinName.append(PinyinHelper.toHanyuPinyinStringArray(
                            aNameChar, defaultFormat)[0].charAt(0));
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(aNameChar);
            }
        }
        return pinyinName.toString();
    }

    /**
     * 将字符串中的中文转化为拼音,并将每个拼音的首字母大写,英文字符不变
     *
     * @param inputString 汉字
     * @return
     */
    public static String getPingYin(String inputString) {
        Log.d(TAG, "getPingYin,inputString=" + inputString);
        if (TextUtils.isEmpty(inputString)) {
            return "*";
        }
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        StringBuilder output = new StringBuilder();
        if (inputString.length() > 0) {
            char[] inputCharArr = inputString.trim().toCharArray();
            try {
                for (char c : inputCharArr) {
                    //判断是否为汉字字符
                    if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(c, format);
                        //如果是多音字，选取第一个拼音
                        String py = temp[0];
                        if (!TextUtils.isEmpty(py)) {
                            py = reConvert(c, py);
                            //拼音首字母大写
                            char[] cs = py.toCharArray();
                            cs[0] -= 32;//小写转大写
                            output.append(String.valueOf(cs));
                        } else {
                            output.append('*');
                        }
                    } else
                        output.append(Character.toString(c));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return output.toString();
    }

    private static String reConvert(char c, String py) {
        Log.d(TAG, "reConvert,py=" + py);
        for (String[] con : CONVERT_LIST) {
            if (TextUtils.equals(con[0], String.valueOf(c)) && TextUtils.equals(py, con[1])) {
                return con[2];
            }
        }
        return py;
    }

    /**
     * 汉字转换为汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char aNameChar : nameChar) {
            if (aNameChar > 128) {
                try {
                    pinyinName.append(PinyinHelper.toHanyuPinyinStringArray(
                            aNameChar, defaultFormat)[0].charAt(0));
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(aNameChar);
            }
        }
        return pinyinName.toString();
    }

}
