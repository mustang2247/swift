package com.ganqiang.swift.core;

import java.io.Serializable;
import java.util.HashMap;

public class Event extends HashMap<Byte,Object> implements Serializable
{

  private static final long serialVersionUID = -1231909451017897866L;

  public static final byte seed_key = 0x11;
  public static final byte unVisitedLinks_key = 0x12;
  public static final byte results_key = 0x13;
  public static final byte fetchedPages_key = 0x14;

}
