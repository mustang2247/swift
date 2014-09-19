package com.ganqiang.swift.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateUtil {

	public static final String yyyyMMdd = "yyyy-MM-dd";
	public static final String yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";

	public static String getCurrentDay() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(yyyyMMdd);
		return format.format(date);
	}

	public static String getYear() {
		Calendar cal = Calendar.getInstance();
		String year = new SimpleDateFormat("yyyy").format(cal.getTime());
		return year;
	}

	/**
	 * start 开始投标时间
	 */
	public static String getRemainTime(Date end) {
		Date current = new Date();
		long tm = (end.getTime() - current.getTime()); // 共计秒数
		int ms = (int) (tm % 1000);
		tm /= 1000;
		int sc = (int) (tm % 60);
		tm /= 60;
		int mn = (int) (tm % 60);
		tm /= 60;
		int hr = (int) (tm % 24);
		long dy = tm / 24;
		// return dy+"天"+hr+"小时"+mn+"分"+sc+"."+ms+"秒";
		return dy + "天" + hr + "小时";
	}

	public static Date parse(String date) {
		SimpleDateFormat format = new SimpleDateFormat(yyyyMMddHHmmss);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date strToDate(String date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	// public static String parseUS(String date) {
	// SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",
	// Locale.US);
	// try {
	// return parse(fmt.parse(date));
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	/** Feb 21, 2014 5:01:31 PM **/
	public static String renrendai_format = "MMM dd, yyyy HH:mm:ss a";
	/** Thu Apr 03 17:31:07 CST 2014 **/
	public static String yooli_format = "EEE MMM dd HH:mm:ss z yyyy";

	public static String parse(String date, String format) {
		SimpleDateFormat fmt = new SimpleDateFormat(format, Locale.US);
		try {
			Date datetime = fmt.parse(date);
			return dateToStr(datetime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String dateToStr(Date date) {
		SimpleDateFormat format = new SimpleDateFormat(yyyyMMddHHmmss);
		return format.format(date);
	}

	public static String dateToStr(Date date, String formatstr) {
		SimpleDateFormat format = new SimpleDateFormat(formatstr);
		return format.format(date);
	}

	public static boolean compare(Date end, Date start) {
		if (end.compareTo(start) == 1) {
			return true;
		}
		return false;
	}

	public static boolean checkDate(String date) {
		String regex = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?"
				+ "((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))"
				+ "|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))"
				+ "|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])"
				+ "|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]"
				+ "?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]"
				+ "?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]"
				+ "?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])"
				+ "|([1][0-9])|([2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(date);
		boolean b = m.matches();
		return b;
	}

	// public static Date parseStartTime(String time){
	// Date date = null;
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// try {
	// date = sdf.parse(time);
	// } catch (ParseException e) {
	// logger.error("configuration time occur error ",e);
	// }
	// Calendar calendar = Calendar.getInstance();
	// int year = calendar.get(date.getYear());
	// int month = calendar.get(Calendar.MONTH);
	// int day = calendar.get(Calendar.DAY_OF_MONTH);
	// calendar.set(year, month, day, date.getHours(), date.getMinutes(),
	// date.getSeconds());
	// return calendar.getTime();
	// }

	public static Double getNormTime(String date) {
		int day = 0;
		int month = 0;
		String daystr = "";
		if (!date.contains("月") && date.contains("天")) {
			day = Integer.valueOf(date.replaceAll("天", "").replaceAll(" ", ""));
			if (day >= 30) {
				Integer jinwei = ((Double)CalculateUtil.div(day, 30)).intValue();
				month = jinwei + month;
				day = day - jinwei * 30 ;
			}
			if (String.valueOf(day).length() == 1) {
				daystr = "0" + day;
			}else{
				daystr = "" + day;
			}
		} else if (date.contains("月") && date.contains("天")) {
			String[] splits = null;
			if(date.contains("月")){
				splits = date.split("月");
			} else if(date.contains("个月")){
				splits = date.split("个月");
			}
			month = Integer.valueOf(splits[0]);
			day = Integer.valueOf(splits[0].replaceAll("天", "").replaceAll(" ", ""));
			if (String.valueOf(day).length() == 1) {
				daystr = "0" + day;
			}else{
				daystr = "" + day;
			}
		} else if (date.contains("月") && !date.contains("天")){
			month = Integer.valueOf(date.replaceAll("月", "").replaceAll("个", "").replaceAll(" ", ""));
		}
		String str = month + "." + daystr;
		return Double.valueOf(str);
	}
	
	public static String getEndTime(String remainTime) {
		StringTokenizer st = new StringTokenizer(remainTime, "月天小时分秒");
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int seconds = 0;
		int i = 0;
		while (st.hasMoreElements()) {
			if (i == 0) {
				month = Integer.valueOf(st.nextElement().toString());
			} else if (i == 1) {
				day = Integer.valueOf(st.nextElement().toString());
			} else if (i == 2) {
				hour = Integer.valueOf(st.nextElement().toString());
			} else if (i == 3) {
				minute = Integer.valueOf(st.nextElement().toString());
			} else if (i == 4) {
				seconds = Integer.valueOf(st.nextElement().toString());
			}
			i++;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, month);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		calendar.add(Calendar.HOUR_OF_DAY, hour);
		calendar.add(Calendar.MINUTE, minute);
		calendar.add(Calendar.SECOND, seconds);
		return DateUtil.dateToStr(calendar.getTime());
	}

	public static String getTomorrow() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 分别取得当前日期的年、月、日
		return df.format(cal.getTime());
	}

	public static String getNextMonthFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
		cal.set(Calendar.DAY_OF_MONTH, 1);// 每周从周一开始
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 分别取得当前日期的年、月、日
		return df.format(cal.getTime());
	}

	public static String getNextWeekFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 每周从周一开始
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 分别取得当前日期的年、月、日
		return df.format(cal.getTime());
	}

	public static Date addSeconds(Date date, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, num);
		return cal.getTime();
	}

	public static String getNextDate(Date starttime, Long interval) {
		String intervalstr = interval.toString();
		Date d = DateUtil.addSeconds(starttime, Integer.valueOf(intervalstr));
		String str = DateUtil.dateToStr(d);
		return str;
	}

	public static void main(String... args) {
		String str = "04-12";
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		str = year + "-" + str;
		System.out.println("=====" + getCurrentDay());
	}
}
