package classes.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public class CharacterParser
{
    public static String getInitLetter(String inputString)
    {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        try
        {
            if (input.length > 0 && Character.toString(input[0]).matches("[\\u4E00-\\u9FA5]+"))
            {
                String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[0], format);
                return temp[0].substring(0, 1).toUpperCase();
            }
            else if (input.length > 0 && Character.toString(input[0]).matches("[a-zA-Z]"))
            {
                return String.valueOf(input[0]).toUpperCase();
            }
            else
            {
                return "#";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "#";
        }
    }
}