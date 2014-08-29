package com.ganqiang.swift.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ganqiang.swift.util.StringUtil;

public final class IniHelper
{
  private static final Logger logger = Logger.getLogger(IniHelper.class);
  // section item value
  private static Map<String, HashMap<String, String>> sectionsMap = new HashMap<String, HashMap<String, String>>();
  // item value
  private static HashMap<String, String> itemsMap = new HashMap<String, String>();

  private static String currentSection = "";

  public static void loadIni(String filename)
  {
    File file = new File(filename);
    if (!file.exists()) {
      logger.error("file "+ filename +" is not exist.");
      System.exit(1);
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (StringUtil.isNullOrBlank(line))
          continue;
        if (line.startsWith("[") && line.endsWith("]")) {
          // Ends last section
          if (itemsMap.size() > 0 && !"".equals(currentSection.trim())) {
            sectionsMap.put(currentSection, itemsMap);
          }
          currentSection = "";
          itemsMap = null;

          // Start new section initial
          currentSection = line.substring(1, line.length() - 1);
          itemsMap = new HashMap<String, String>();
        } else {
          int index = line.indexOf("=");
          if (index != -1) {
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1, line.length()).trim();
            if (value.contains(";")) {
              value = value.split(";")[0].trim();
            }
            if (value.contains("#")) {
              value = value.split("#")[0].trim();
            }
            itemsMap.put(key, value);
          }
        }
        // System.out.println(line);
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  public static String getValue(String section, String item)
  {

    HashMap<String, String> map = sectionsMap.get(section);
    if (map == null) {
      return "No such section:" + section;
    }
    String value = map.get(item);
    if (value == null) {
      return "No such item:" + item;
    }
    return value;
  }

  public static List<String> getSectionNames(File file)
  {
    List<String> list = new ArrayList<String>();
    Set<String> key = sectionsMap.keySet();
    for (Iterator<String> it = key.iterator(); it.hasNext();) {
      list.add(it.next());
    }
    return list;
  }

  public static Map<String, String> getItemsBySectionName(String section)
  {
    return sectionsMap.get(section);
  }

}
