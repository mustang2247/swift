package com.ganqiang.swift.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;


public class StringUtil
{
  public static List<String> StringToList(String str){
    List<String> list = new ArrayList<String>();
    if (StringUtil.isNullOrBlank(str)) {
      return list;
    } else {
      String[] proxys = str.split(",");
      for (int i=0; i < proxys.length; i ++) {
        list.add(proxys[i]);
      }
      return list;
    }

  }
  
  public static String ListToString(List<String> list){
    if (list == null || list.isEmpty()) {
      return "";
    }
    String ns = "";
    for (int i=0; i<list.size(); i++) {
      String str = list.get(i);
      ns += str;
      if (i < list.size() -1) {
        ns += ",";
      }
    }
    return ns;
  }
  
  public static String strikeBlankString(String str){
    return str.replaceAll("\\s{1,}", "");
  }
  
  public static boolean isContainChinese(String strname){
    Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
    Matcher m = p.matcher(strname);
    if (m.find()) {
        return true;
    }
    return false;
  }
  
  public static boolean isChinese(String strName) {
    return strName.matches("[\\u4E00-\\u9FA5]+");
  }
  
  /**
   *  /list/39485 ==> list/39485
   */
  public static String getAbsolutePath(String str){
    str = str.replaceAll("\\.\\.", "");
    return str.startsWith("/") ? str.substring(1, str.length()) : str;
  }
  
  public static void main(String... args){
    String str = "http://www.jimubox.com/Project/Index/1163";
    System.out.println(generateID(str));
  }

	public static boolean isNullOrBlank(String str)
	{
		if (null == str || "".equals(str.trim())){
			return true;
		}
		return false;
	}
	
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",  
    "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
    "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",  
    "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",  
    "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",  
    "W", "X", "Y", "Z" };
	
	private static String byteToHexString(byte b){  
    int n = b;  
    if(n < 0)  
        n = 256 + n;  
    int d1 = n / 16;  
    int d2 = n % 16;  
    return chars[d1] + chars[d2];  
}  
	
	public static String byteArrayToHexString(byte[] b){  
    StringBuffer resultSb = new StringBuffer();  
    for(int i = 0;i < b.length;i++){  
        resultSb.append(byteToHexString(b[i]));  
    }  
    return resultSb.toString().substring(0,8);  
}  
  
	
	public synchronized static String generateID(String origin){ 
	  java.security.MessageDigest md;
	  String pwd = null;
    try {
      md = MessageDigest.getInstance("MD5");
      byte[] b = origin.getBytes("UTF-8");  
      byte[] hash = md.digest(b);
      String str =byteArrayToHexString(hash);
      return str;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }  
    
    return pwd;
}  

	
	public synchronized static String generateID(){
	  StringBuffer shortBuffer = new StringBuffer();
    String uuid = UUID.randomUUID().toString().replace("-", "");
    for (int i = 0; i < 8; i++) {
      String str = uuid.substring(i * 4, i * 4 + 4);
      int x = Integer.parseInt(str, 16);
      shortBuffer.append(chars[x % 0x3E]);
    }
    return shortBuffer.toString();
	}
	
	public static String getOriginStr(String str1,List<Integer> array){
    if(StringUtil.isNullOrBlank(str1) && (array == null || array.size() == 0)){
      return null;
    }
    for(int i=0;i<array.size();i++){
      str1 = str1.substring(0, array.get(i)+i) + " " + str1.substring(array.get(i)+i, str1.length());
    }
    return str1;
  }
	
}

