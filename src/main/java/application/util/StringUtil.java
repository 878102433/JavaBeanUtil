package application.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
	/**
	 * 转换首字母为大写
	 *
	 * @param text
	 * @return
	 */
	public static String upperCaseFirstLetter(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		char[] chars = text.toCharArray();
		if (chars[0] >= 'a' && chars[0] <= 'z') {
			chars[0] = (char) (chars[0] + 'A' - 'a');
		}
		return String.valueOf(chars);
	}

	/**
	 * 转换首字母为小写
	 *
	 * @param text
	 * @return
	 */
	public static String lowerCaseFirstLetter(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		char[] chars = text.toCharArray();
		if (chars[0] >= 'A' && chars[0] <= 'Z') {
			chars[0] = (char) (chars[0] + 'a' - 'A');
		}
		return String.valueOf(chars);
	}

	/**
	 * 帕斯卡命名法转驼峰命名法
	 *
	 * @param text
	 * @return
	 */
	public static String pascal2Camel(String text) {
		return lowerCaseFirstLetter(text);
	}

	/**
	 * 驼峰命名法帕斯卡命名法
	 *
	 * @param text
	 * @return
	 */
	public static String camel2Pascal(String text) {
		return upperCaseFirstLetter(text);
	}

	/**
	 * 下划线命名法转帕斯卡命名法
	 *
	 * @param text
	 * @return
	 */
	public static String underline2Pascal(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		String[] array = text.toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();
		for (String str : array) {
			sb.append(upperCaseFirstLetter(str));
		}
		return sb.toString();
	}

	/**
	 * 下划线命名法转帕斯卡命名法
	 *
	 * @param text
	 * @param prefixs
	 * @return
	 */
	public static String underline2Pascal(String text, List<String> prefixs) {
		if (prefixs != null) {
			prefixs.sort((h1, h2) -> h2.compareTo(h1));
			String str = text;
			Optional<String> optPrefix = prefixs.stream().filter(x -> str.startsWith(x)).findFirst();
			if (optPrefix.isPresent()) {
				text = text.replaceFirst(optPrefix.get(), "");
			}
		}

		if (text == null || text.length() == 0) {
			return text;
		}
		String[] array = text.toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();
		for (String str : array) {
			sb.append(upperCaseFirstLetter(str));
		}
		return sb.toString();

	}

	/**
	 * 下划线命名法转驼峰命名法
	 *
	 * @param text
	 * @return
	 */
	public static String underline2Camel(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		String[] array = text.toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i == 0) {
				sb.append(array[i]);
			} else {
				sb.append(upperCaseFirstLetter(array[i]));
			}
		}
		return sb.toString();
	}

	/**
	 * 下划线命名法转点号命名法
	 *
	 * @param text
	 * @return
	 */
	public static String underline2Point(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		return text.toLowerCase().replace("_", ".");
	}

	/**
	 * 驼峰命名法 转下划线命名法
	 *
	 * @param text
	 * @return
	 */
	public static String pascal2Underline(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		StringBuilder sb = new StringBuilder();
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i > 0 && chars[i] >= 'A' && chars[i] <= 'Z') {
				sb.append("_");
			}
			sb.append(chars[i]);
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * 驼峰命名法 转下划线命名法
	 *
	 * @param text
	 * @return
	 */
	public static String camel2Underline(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		StringBuilder sb = new StringBuilder();
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] >= 'A' && chars[i] <= 'Z') {
				sb.append("_");
			}
			sb.append(chars[i]);
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * object转String
	 *
	 * @param object
	 * @return
	 */
	public static String toString(Object object) {
		if (object != null) {
			return object.toString();
		}
		return null;

	}

	/**
	 * 空转null
	 */
	public static String empty2Null(String str) {
		if ("".equals(str)) {
			return null;
		}
		return str;
	}

	/**
	 * null转空
	 */
	public static String null2Empty(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}

	/**
	 * 判断是否为空或null
	 */
	public static boolean isEmptyOrNull(String str) {
		if (str == null || str.length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 字符串转list
	 */
	public static List<String> str2List(String str, String split) {
		if (!isEmptyOrNull(str)) {
			List<String> list = new ArrayList<String>();
			String[] array = str.split(split);
			for (String s : array) {
				if (!isEmptyOrNull(s)) {
					list.add(s);
				}
			}
			if (list.size() > 0) {
				return list;
			}
		}
		return null;

	}

	/**
	 * list转String
	 */
	public static String list2Str(List<String> list, String join) {
		if (list != null) {
			return StringUtils.join(list.toArray(), join);
		}
		return null;

	}

	/**
	 * 判断Boolean是否为正
	 */
	public static boolean isBooleanTrue(Boolean boo) {
		if (boo != null && boo) {
			return true;
		} else {
			return false;
		}
	}
}
