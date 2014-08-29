package com.ganqiang.swift.util;

public enum Charset
{
  UTF8("UTF-8"),
  UTF16("UTF-16"),
  ISO88591("ISO-8859-1");
  
  private String encoding;
  
  Charset(String encoding){
    this.encoding = encoding;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }
  
  public static String getUtf8Encoding()
  {
    return UTF8.getEncoding();
  }
  
}
