package com.ganqiang.swift.util;

public final class ModelNormalizer {

	public static Double getYearRateNorm(Double d) {
		Double result = 0d;
		if (d >= 0 && d <= 5) {
			result = 0.1;
		} else if (d > 5 && d <= 8) {
			result = 0.2;
		} else if (d > 8 && d <= 10) {
			result = 0.3;
		} else if (d > 10 && d <= 12) {
			result = 0.4;
		} else if (d > 12 && d <= 14) {
			result = 0.5;
		} else if (d > 14 && d <= 16) {
			result = 0.6;
		} else if (d > 16 && d <= 18) {
			result = 0.7;
		} else if (d > 18 && d <= 20) {
			result = 0.8;
		} else if (d > 20 && d <= 30) {
			result = 0.9;
		} else{
			result = 1.0;
		}
		return result;
	}

	public static Double getRepayLimitTimeNorm(String date) {
		Double result = 0d;
		Double time = DateUtil.getNormTime(date);
		if(time > 0 && time <= 0.1){//10天内
			result = 0.1;
		}else if(time > 0.1 && time <= 1){//1个月内
			result = 0.2;
		}else if(time > 1 && time <= 3){//3个月内
			result = 0.3;
		}else if(time > 3 && time <= 5){//5个月内
			result = 0.4;
		}else if(time > 5 && time <= 7){//7个月内
			result = 0.5;
		}else if(time > 7 && time <= 9){//9个月内
			result = 0.6;
		}else if(time > 9 && time <= 12){//12个月内
			result = 0.7;
		}else if(time > 12 && time <= 24){//24个月内
			result = 0.8;
		}else if(time > 24 && time <= 36){//36个月内
			result = 0.9;
		}else {
			result = 1.0;
		}
		return result;
	}

	public static void main(String... args){
		Double tiem = 1.0d;
		System.out.println(tiem < 1);
	}

}
