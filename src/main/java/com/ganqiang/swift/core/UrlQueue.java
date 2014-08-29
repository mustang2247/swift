package com.ganqiang.swift.core;

import java.util.concurrent.ConcurrentHashMap;

import com.ganqiang.swift.util.ConcurrentQueue;

public final class UrlQueue
{
  //key: instanceid+site  value: urls
  private static ConcurrentHashMap<String, ConcurrentQueue<String>> visitedUrl =  new ConcurrentHashMap<String, ConcurrentQueue<String>>();
  private static ConcurrentHashMap<String, ConcurrentQueue<String>> unVisitedUrl =  new ConcurrentHashMap<String, ConcurrentQueue<String>>();
  
  public static void addUnVisitedUrl(String key, String link){
    ConcurrentQueue<String> queue = UrlQueue.unVisitedUrl.get(key);
    if(queue == null){
      queue = new ConcurrentQueue<String>();
      UrlQueue.unVisitedUrl.put(key, queue);
    }
    queue.add(link);
  }
  
  public static void addVisitedUrl(String key, String link){
    ConcurrentQueue<String> queue = UrlQueue.visitedUrl.get(key);
    if(queue == null){
      queue = new ConcurrentQueue<String>();
      UrlQueue.visitedUrl.put(key, queue);
    }
    queue.add(link);
  }
  
  public static ConcurrentQueue<String> getUnVisitedUrl(String key){
    return unVisitedUrl.get(key);
  }
  
  public static ConcurrentQueue<String> getVisitedUrl(String key){
    return visitedUrl.get(key);
  }
  
}
