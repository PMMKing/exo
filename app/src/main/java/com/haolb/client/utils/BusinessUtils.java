package com.haolb.client.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusinessUtils {

	public static final String ENCRYPT_TYPE_SHA1 = "SHA-1";
	public static final String ENCRYPT_TYPE_MD5 = "MD5";

	/**
	 * 身边酒店距离显示转换
	 */
	public static String fixedDistance(float src) {
		if (src < 50f) {
			return "小于50米";
		} else if (src < 1000f) {
			return "约" + Math.round(src / 10) * 10 + "米";
		} else {
			return "约" + Math.round(src / 100) / 10f + "公里";
		}
	}

	/**
	 * 替换价格
	 * 
	 * @param beReplaceStr
	 *            被替换String
	 * @param price
	 *            原价：dprice+rprice
	 * @param totalPrice
	 *            订单总价 售卖价
	 * @param totalPrize
	 *            优惠金额
	 * @return
	 */
	public static String replacePrice(String beReplaceStr, String totalPrice,
			String totalPrize) {
		String price = "";
		try {
			double dprice = Double.parseDouble(totalPrice);
			double rprice = Double.parseDouble(totalPrize);
			price = BusinessUtils.formatDouble2String(dprice + rprice);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return beReplaceStr.replaceAll("\\{price\\}", price)
				.replaceAll("\\{dprice\\}", totalPrice)
				.replaceAll("\\{rprice\\}", totalPrize);
	}

	/**
	 * 判定输入汉字
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 用户输入的名字超过了26 先判断字符中只有汉字和英文 如果只是英文，判断是否有分隔符，有且只有一个
	 * 如果有汉字，汉字只能在一开始，汉字之后如果有英文，英文后面不应该在有汉字<br>
	 * 0 代表成功<br>
	 * 1您的姓名过长，民航系统无法输入，您输入姓名中前54个字符（包括“/”和空格）或27个汉字即可<br>
	 * 5姓名过短，请输入正确姓名<br>
	 * 6姓名中不能含有除汉字、字母、空格和“/”以外的其它字符
	 */
	public static int checkPersonName(String name) {
		if (TextUtils.isEmpty(name)) {
			return 5;
		}
		final String reg1 = "^[\u4e00-\u9fa5 A-Za-z/]+$";// 中文、英文、空格、 /
		if (name.matches(reg1)) {
			int length = name.replaceAll("[\u4e00-\u9fa5]", "aa").length();
			if (length > 54) {
				return 1;
			}
			if (length < 3) {
				return 5;
			}
		} else {
			return 6;
		}
		return 0;
	}

	/**
	 * 检查字符串是否全为数字
	 * 
	 * @param certiCode
	 * @return
	 */
	public static boolean checkFigure(String certiCode) {
		try {
			Long.parseLong(certiCode);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	// 检查手机号正确性
	public static boolean checkPhoneNumber(String number) {
		number=number.replace(" ", "");
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(number);
		return m.matches() && (number.length() == 11);
	}

	/**
	 * 去除空格和+86
	 * 
	 * @param phoneNum
	 * @return
	 */
	public static String formatPhoneNum(String phoneNum) {
		String num = phoneNum.replaceAll(" ", "");
		num = num.replace("+86", "");
		return num;
	}

	// 检查手机号和座机号正确性
	public static boolean checkTeleNumber(String number) {
		Pattern p = Pattern.compile("[0-9]{7,11}");
		Matcher m = p.matcher(number);
		return m.matches();
	}

	// 检查邮箱正确性
	public static boolean checkEmail(String email) {
		if (!checkChiness(email)) {
			Pattern p = Pattern
					.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
			Matcher m = p.matcher(email);
			return m.matches();
		} else {
			return false;
		}

	}

	// 格式化用于拨打的电话号码
	public static String formatPhoneNumber(String phoneNumber) {
		String StrTmp = "tel:" + phoneNumber.replace("-", "").replace(" ", "");
		/*
		 * String tmp[] = StrTmp.split("转"); StrTmp = tmp[0];
		 */
		StrTmp = StrTmp.replace("转", "p");
		return StrTmp;
	}
    public final static String formatTimeed(long s) {
        if (s < 0) {
            return "";
        } else if (s >= 0 && s < 60) {
            return s + "秒";
        } else if (s >= 60 && s < 60 * 60) {
            return s / 60 + "分钟";
        } else if (s >= 60 * 60 && s < 60 * 60 * 24) {
            return s / 3600 + "小时";
        } else if (s >= 60 * 60 * 24 && s < 60 * 60 * 24*7) {
            return s / 3600 / 24 + "天";
        } else if (s >= 60 * 60 * 24*7 && s < 60 * 60 * 24 * 30) {
            return s / 3600 / 24/7 + "周";
        } else if (s >= 60 * 60 * 24 * 30 && s < 60 * 60 * 24 * 30 * 365) {
            return s / 3600 / 24 / 30 + "月";
        } else if (s >= 60 * 60 * 24 * 30 * 365) {
            return s / 3600 / 24 / 30 / 365 + "年";
        }
        return "";

    }

	public static String formatInt2StringDate(int time) {

		String strTmp = time + "分钟";

		if (time >= 60) {
			int strMinute = time % 60;
			int strhour = time / 60;
			strTmp = strhour + "小时" + strMinute + "分钟";
			if (time / 60 >= 24) {
				int data = time / 60 / 24;
				strTmp = data + "日" + strhour + "小时" + strMinute + "分钟";
			}
		}

		return strTmp;
	}

	// 截字符长，超过n个字符时候用…表示
	public static String fixString(String str, int n) {
		if (str.length() > n) {
			return str.substring(0, n) + "…";
		}
		return str;
	}

	/**
	 * 判断str中是否只有汉字和英文、中文
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkEnglishAndChiness(String str) {
		final String reg = "^[\u4e00-\u9fa5A-Za-z]+$";
		if (str.matches(reg)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断str中是否有汉字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkChiness(String str) {
		final String reg = "^[\u4e00-\u9fa5]+$";
		if (str.matches(reg)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 判断是否全部是英文
	 * 
	 * @param mData
	 *            .name
	 * @return
	 */
	public static boolean checkEnglish(String str) {
		final String reg = "^[A-Za-z]+$";
		if (str.matches(reg)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户 输入了 例如 孙bin仁
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkChinessEnghlish(String str) {
		final String reg = "^[\u4e00-\u9fa5]+[A-Za-z]+[\u4e00-\u9fa5]+[\u4e00-\u9fa5a-zA-Z]*$";
		if (str.matches(reg)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户输入了例如 A李......
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkEnglishChiness(String str) {
		final String reg = "^[A-Za-z]+[\u4e00-\u9fa5]+[\u4e00-\u9fa5a-zA-Z]*$";
		if (str.matches(reg)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户输入了例如 A李A
	 * 
	 * @param str
	 * @return
	 */
	/*
	 * public static boolean checkEnglishChinessEnglish(String str){ final
	 * String reg = "^[A-Za-z]+[\u4e00-\u9fa5]+[A-Za-z]+$";
	 * if(str.matches(reg)){ return true; }else{ return false; } }
	 */

	/**
	 * 验证联系人和乘机人中输入的英文字符是否正确 (aaa/ooo)
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkPassengerAndContactEnglishName(String name) {
		final String reg = "^[A-Za-z]+[///]+[A-Za-z]+$";
		if (name.matches(reg)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 小数点后面的数字为0则省略,如10.0-->10
	 * 
	 * @param value
	 * @return
	 */
	public static String formatFloat2String(float value) {
		String valueStr = value + "";
		int index = valueStr.indexOf(".");
		if (index > -1) {
			String rest = valueStr.substring(index + 1);
			if ("0".equals(rest)) {
				valueStr = valueStr.substring(0, index);
			}
		}
		return valueStr;
	}

	/**
	 * 将double类型的数据格式化 10.000 ----> 10 10.0020 ---> 10.002
	 * 
	 * @param value
	 * @return
	 */
	public static String formatDouble2String(double value) {
		String valueStr = value + "";
		try {
			int index = valueStr.indexOf(".");
			if (index > -1) {
				String rest = valueStr.substring(index + 1);
				while (rest.length() > 0) {
					if ('0' == rest.charAt(rest.length() - 1)) {
						rest = rest.substring(0, rest.length() - 1);
					} else {
						break;
					}
				}
				if (rest.length() == 0) {
					valueStr = valueStr.substring(0, index);
				} else {
					valueStr = valueStr.substring(0, index) + "." + rest;
				}
			}
		} catch (Exception e) {
			return value + "";
		}
		return valueStr;
	}

	/**
	 * 
	 * 提供精确的减法运算。避免出现1-0.99 = 0.010000000009的情况
	 * 
	 * @param v1
	 *            被减数
	 * 
	 * @param v2
	 *            减数
	 * 
	 * @return 两个参数的差
	 */

	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	public static boolean isSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 验证信用卡号
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkCreditCardNo(String str) {
		Pattern p = Pattern.compile("[0-9]{15,16}");
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 验证证件号码
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkCardNo(String str) {
		Pattern p = Pattern.compile("^[0-9a-zA-Z]+$");
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 验证信用卡cvv2
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkCVV2(String str) {
		Pattern p = Pattern.compile("[0-9]{3,4}");
		Matcher m = p.matcher(str);
		return m.matches();
	}

	public static String formatDoublePrice(double value) {
		DecimalFormat df = new DecimalFormat("##########.##");
		return df.format(value);
	}

	public static String formatPhoneShow(String phone) {

		return phone.length() == 11 ? phone.substring(0, 3) + "-"
				+ phone.substring(3, 7) + "-"
				+ phone.substring(7, phone.length()) : phone;
	}

	public static String formatDoublePrice(String value) {
		try {
			return formatDoublePrice(Double.parseDouble(value));
		} catch (Exception e) {
			return "0";
		}
	}

	/**
	 * 将str val 转换成double类型
	 * 
	 * @param val
	 * @return
	 */
	public static double parseDouble(String val) {
		try {
			return Double.parseDouble(val);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 将str val 转换成integer类型
	 * 
	 * @param val
	 * @return
	 */
	public static double parseInteger(String val) {
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * use two-digit format strings like "01".
	 */
	public static String twoDigitFormat(int value) {
		StringBuilder mBuilder = new StringBuilder();
		java.util.Formatter mFmt = new java.util.Formatter(mBuilder,
				java.util.Locale.US);
		mBuilder.delete(0, mBuilder.length());
		mFmt.format("%02d", value);
		String str = mFmt.toString();
		mFmt.close();
		return str;
	}
	  /**
     * 转换文件大小
     * 
     * @param fileS
     * @return
     */ 
 	public static String FormetFileSize(long fileS) { 
        DecimalFormat df = new DecimalFormat("#.00"); 
        String fileSizeString = ""; 
        if (fileS < 1024) { 
            fileSizeString = df.format((double) fileS) + "B"; 
        } else if (fileS < 1048576) { 
            fileSizeString = df.format((double) fileS / 1024) + "K"; 
        } else if (fileS < 1073741824) { 
            fileSizeString = df.format((double) fileS / 1048576) + "M"; 
        } else { 
            fileSizeString = df.format((double) fileS / 1073741824) + "G"; 
        } 
        return fileSizeString; 
    } 
}
