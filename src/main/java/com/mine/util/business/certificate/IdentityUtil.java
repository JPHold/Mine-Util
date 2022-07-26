package com.mine.util.business.certificate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentityUtil {

    public final int CHINA_ID_MIN_LENGTH = 15;
    public final int CHINA_ID_MAX_LENGTH = 18;

    /**
     * 南顺 hjp 2022年7月26日 09:17:41 封装校验类型结果
     *
     * @param
     * @return
     */
    public static class ValidatedResult {
        private ValidatedResultType validatedResultType;//结果类型
        private String addDesc;
        private String validatedValue;//校验的值

        public void addDesc(String addDescFormat, Object... params) {
            addDesc = validatedResultType.name + String.format(addDescFormat, params);
        }

        public ValidatedResultType getResultType() {
            return validatedResultType;
        }

        public void setResultType(ValidatedResultType validatedResultType) {
            this.validatedResultType = validatedResultType;
        }

        public String getAddDesc() {
            return addDesc;
        }

        public String getValidatedValue() {
            return validatedValue;
        }

        public void setValidatedValue(String validatedValue) {
            this.validatedValue = validatedValue;
        }

        @Override
        public String toString() {
            return "ValidatedResult{" +
                    "validatedResultType=" + validatedResultType +
                    ", addDesc='" + addDesc + '\'' +
                    ", validatedValue='" + validatedValue + '\'' +
                    '}';
        }
    }

    public enum ValidatedResultType {
        SUCCESS(0, "通过校验"), FAILURE_NULL(1, "身份证为空"), FAILURE_15_NON_FULL_LENGTH(2, "不是15位身份证"), FAILURE_18_NON_FULL_LENGTH(3, "不是18位身份证"), FAILURE_15_NON_FULL_NUMERIC(4, "不是全数字"), FAILURE_18_NON_FULL_17_BIT_NUMERIC(5, "前17位不是全数字"), FAILURE_NON_PROVINCE(6, "省份不正确"), FAILURE_NON_BIRTHDAY(7, "出生日期不正确"), FAILURE_18_NULL_CHECK_BIT(8, "计算出来的第18位校验码为空"), FAILURE_18_NON_CHECK_BIT(9, "第18位校验码不正确"), FAILURE_EXCEPTION(99, "校验出现异常");

        /**
         * 错误编码
         */
        private Integer code;
        /**
         * 错误描述
         */
        private String name;

        ValidatedResultType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static Date getBirthDay(String idCard) {
        int year = Integer.parseInt(idCard.substring(6, 10));
        int month = Integer.parseInt(idCard.substring(10, 12)) - 1;
        int day = Integer.parseInt(idCard.substring(12, 14));

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String getGenderNameByIdCard(String idCard) {
        String sGender;
        String sCardNum = idCard.substring(16, 17);
        if (Integer.parseInt(sCardNum) % 2 != 0) {
            sGender = "男性";                                    //男
        } else {
            sGender = "女性";                                    //女
        }
        return sGender;
    }

    public static String getGenderCodeByIdCard(String idCard) {
        String sGender;
        String sCardNum = idCard.substring(16, 17);
        if (Integer.parseInt(sCardNum) % 2 != 0) {
            sGender = "1";                                    //男
        } else {
            sGender = "2";                                    //女
        }
        return sGender;
    }

    /* 
	    身份证号码的结构和表示形式<br> 
	    1、号码的结构<br> 
	       公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。<br> 
	    2、地址码<br> 
	       表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。<br> 
	    3、出生日期码<br> 
	       表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。<br> 
	    4、顺序码<br> 
	       表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配给女性。<br> 
	    5、校验码<br> 
	     （1）十七位数字本体码加权求和公式<br> 
	          S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和<br> 
	          Ai:表示第i位置上的身份证号码数字值<br> 
	          Wi:表示第i位置上的加权因子<br> 
	          Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2<br> 
	     （2）计算模<br> 
	          Y = mod(S, 11)<br> 
	     （3）通过模得到对应的校验码<br> 
	          Y: 0 1 2 3 4 5 6 7 8 9 10<br> 
	             校验码: 1 0 X 9 8 7 6 5 4 3 2<br> 
    */

    /**
     * 将15位身份证号转化为18位返回，非15位身份证号原值返回
     *
     * @author InJavaWeTrust
     */
    public static String get18Ic(String identityCard) {
        String retId = "";
        String id17 = "";
        int sum = 0;
        int y = 0;
        // 定义数组存放加权因子（weight factor）  
        int[] wf = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        // 定义数组存放校验码（check code）  
        String[] cc = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        if (!is15B(identityCard)) {
            return identityCard;
        }
        // 加上两位年19  
        id17 = identityCard.substring(0, 6) + "19" + identityCard.substring(6);
        // 十七位数字本体码加权求和  
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.valueOf(id17.substring(i, i + 1)) * wf[i];
        }
        // 计算模  
        y = sum % 11;
        // 通过模得到对应的校验码 cc[y]  
        retId = id17 + cc[y];
        return retId;
    }

    public static boolean is15B(String cardNo) {
        return cardNo.length() == 15;
    }

    //校验特殊字符规则
    public static boolean isValidatedSpecialChar(String str) {

        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return false;
        } else {
            char[] chars = str.toCharArray();
            int count = 0;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == chars[0]) {
                    count += 1;
                }
            }
            if (count == chars.length) {
                return false;
            }
            return true;
        }

    }

    /**
     * <pre>
     * 省、直辖市代码表：
     *     11 : 北京  12 : 天津  13 : 河北       14 : 山西  15 : 内蒙古
     *     21 : 辽宁  22 : 吉林  23 : 黑龙江  31 : 上海  32 : 江苏
     *     33 : 浙江  34 : 安徽  35 : 福建       36 : 江西  37 : 山东
     *     41 : 河南  42 : 湖北  43 : 湖南       44 : 广东  45 : 广西      46 : 海南
     *     50 : 重庆  51 : 四川  52 : 贵州       53 : 云南  54 : 西藏
     *     61 : 陕西  62 : 甘肃  63 : 青海       64 : 宁夏  65 : 新疆
     *     71 : 台湾
     *     81 : 香港  82 : 澳门
     *     91 : 国外
     * </pre>
     */
    private static String[] cityCode = {"11", "12", "13", "14", "15", "21",
            "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42",
            "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62",
            "63", "64", "65", "71", "81", "82", "91"};

    /**
     * 每位加权因子
     */
    private static int power[] = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
            8, 4, 2};

    /**
     * 验证所有的身份证的合法性
     *
     * @param idcard 身份证
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidatedAllIdcard(String idcard) {
        if (idcard == null || "".equals(idcard)) {
            return false;
        }
        int s = 15;
        if (idcard.length() == s) {
            ValidatedResult validated15Result = validate15IDCard(idcard);
            return validated15Result.equals(ValidatedResultType.SUCCESS);
        }
        int s1 = 18;
        if (idcard.length() == s1) {
            ValidatedResult validated18Result = validate18Idcard(idcard);
            return validated18Result.equals(ValidatedResultType.SUCCESS);
        }
        return false;

    }

    /**
     * <p>
     * 判断18位身份证的合法性
     * </p>
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     * <p>
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性。
     * </p>
     * <p>
     * 1.前1、2位数字表示：所在省份的代码； 2.第3、4位数字表示：所在城市的代码； 3.第5、6位数字表示：所在区县的代码；
     * 4.第7~14位数字表示：出生年、月、日； 5.第15、16位数字表示：所在地的派出所的代码；
     * 6.第17位数字表示性别：奇数表示男性，偶数表示女性；
     * 7.第18位数字是校检码：也有的说是个人信息码，一般是随计算机的随机产生，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示。
     * </p>
     * <p>
     * 第十八位数字(校验码)的计算方法为： 1.将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4
     * 2 1 6 3 7 9 10 5 8 4 2
     * </p>
     * <p>
     * 2.将这17位数字和系数相乘的结果相加。
     * </p>
     * <p>
     * 3.用加出来和除以11，看余数是多少
     * </p>
     * 4.余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3
     * 2。
     * <p>
     * 5.通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2。
     * </p>
     *
     * @param idcard
     * @return
     */


    /**
     * 数字验证
     *
     * @param str
     * @return
     */
    private static boolean isDigital(String str) {
        return str.matches("^[0-9]*$");
    }

    /**
     * 校验省份
     *
     * @param provinceid
     * @return 合法返回TRUE，否则返回FALSE
     */
    private static boolean checkProvinceid(String provinceid) {
        for (String id : cityCode) {
            if (id.equals(provinceid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符数组转为整型数组
     *
     * @param c
     * @return
     * @throws NumberFormatException
     */
    private static int[] converCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit
     * @return
     */
    private static int getPowerSum(int[] bit) {

        int sum = 0;

        if (power.length != bit.length) {
            return sum;
        }

        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }
        return sum;
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param
     * @param sum17
     * @return 校验位
     */
    private static String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "x";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
            default:
        }
        return checkCode;
    }

    public static ValidatedResult validate18Idcard(String idcard) {
        ValidatedResult validatedResult = new ValidatedResult();
        if (idcard == null) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_NULL);
            return validatedResult;
        }

        // 非18位为假
        int s = 18;
        if (idcard.length() != s) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_18_NON_FULL_LENGTH);
            validatedResult.addDesc(", 当前身份证的长度是%s", idcard.length());
            validatedResult.setValidatedValue(idcard);
            return validatedResult;
        }
        // 获取前17位
        String idcard17 = idcard.substring(0, 17);

        // 前17位全部为数字
        if (!isDigital(idcard17)) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_18_NON_FULL_17_BIT_NUMERIC);
            validatedResult.setValidatedValue(idcard17);
            return validatedResult;
        }

        String provinceid = idcard.substring(0, 2);
        // 校验省份
        if (!checkProvinceid(provinceid)) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_NON_PROVINCE);
            validatedResult.addDesc(", 当前省份是%s", provinceid);
            validatedResult.setValidatedValue(provinceid);
            return validatedResult;
        }

        // 校验出生日期
        String birthday = idcard.substring(6, 14);
        validatedResult.setValidatedValue(birthday);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        try {
            Date birthDate = sdf.parse(birthday);
            String tmpDate = sdf.format(birthDate);
            // 出生年月日不正确
            if (!tmpDate.equals(birthday)) {
                validatedResult.setResultType(ValidatedResultType.FAILURE_NON_BIRTHDAY);
                validatedResult.addDesc(", 当前出生日期是%s, 转换后的出生日期是%s", birthday, tmpDate);
                return validatedResult;
            }

        } catch (Exception e1) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_EXCEPTION);
            validatedResult.addDesc(e1.getMessage());
            return validatedResult;
        }

        // 获取第18位
        String idcard18Code = idcard.substring(17, 18);

        char c[] = idcard17.toCharArray();

        int bit[] = converCharToInt(c);

        int sum17 = 0;

        sum17 = getPowerSum(bit);

        // 将和值与11取模得到余数进行校验码判断
        String checkCode = getCheckCodeBySum(sum17);
        if (null == checkCode) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_18_NULL_CHECK_BIT);
            validatedResult.addDesc(", 前17位身份证是%s, 与对应系数相乘并累加后的数是%s", idcard17, sum17);
            validatedResult.setValidatedValue(idcard17);
            return validatedResult;
        }
        // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
        if (!idcard18Code.equalsIgnoreCase(checkCode)) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_18_NON_CHECK_BIT);
            validatedResult.addDesc(", 当前校验码是%s, 计算出来的校验码是%s, 两者不一致", idcard18Code, checkCode);
            validatedResult.setValidatedValue(checkCode);
            return validatedResult;
        }
        validatedResult.setResultType(ValidatedResultType.SUCCESS);
        validatedResult.setValidatedValue(idcard);
        return validatedResult;
    }

    /**
     * 校验15位身份证
     *
     * <pre>
     * 只校验省份和出生年月日
     * </pre>
     *
     * @param idcard
     * @return
     */
    public static ValidatedResult validate15IDCard(String idcard) {
        ValidatedResult validatedResult = new ValidatedResult();

        validatedResult.setResultType(ValidatedResultType.FAILURE_18_NON_FULL_LENGTH);
        validatedResult.addDesc(", 当前身份证的长度是%s", idcard.length());
        validatedResult.setValidatedValue(idcard);

        if (idcard == null) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_NULL);
            return validatedResult;
        }
        // 非15位为假
        int s = 15;
        if (idcard.length() != s) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_15_NON_FULL_LENGTH);
            validatedResult.addDesc(", 当前身份证的长度是%s", idcard.length());
            validatedResult.setValidatedValue(idcard);
            return validatedResult;
        }

        // 15全部为数字
        if (!isDigital(idcard)) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_15_NON_FULL_NUMERIC);
            validatedResult.setValidatedValue(idcard);
            return validatedResult;
        }

        String provinceid = idcard.substring(0, 2);
        // 校验省份
        if (!checkProvinceid(provinceid)) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_NON_PROVINCE);
            validatedResult.addDesc(", 当前省份是%s", provinceid);
            validatedResult.setValidatedValue(provinceid);
            return validatedResult;
        }

        String birthday = idcard.substring(6, 12);
        validatedResult.setValidatedValue(birthday);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

        try {
            Date birthDate = sdf.parse(birthday);
            String tmpDate = sdf.format(birthDate);
            // 身份证日期错误
            if (!tmpDate.equals(birthday)) {
                validatedResult.setResultType(ValidatedResultType.FAILURE_NON_BIRTHDAY);
                validatedResult.addDesc(", 当前出生日期是%s, 转换后的出生日期是%s", birthday, tmpDate);
                return validatedResult;
            }

        } catch (Exception e1) {
            validatedResult.setResultType(ValidatedResultType.FAILURE_EXCEPTION);
            validatedResult.addDesc(e1.getMessage());
            return validatedResult;
        }

        validatedResult.setResultType(ValidatedResultType.SUCCESS);
        validatedResult.setValidatedValue(idcard);
        return validatedResult;
    }
}
