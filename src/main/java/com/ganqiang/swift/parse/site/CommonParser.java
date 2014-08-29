package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.storage.Result;

public class CommonParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(CommonParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    List<Result> results = new ArrayList<Result>();
    try {
      for(String pageid : resultMap.keySet()){
        Result result = resultMap.get(pageid);
        if (result == null) {
          continue;
        }
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [CommonParser] begin parse from ["+result.getUrl()+"].");
        results.add(result);      
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [CommonParser] end parse from ["+result.getUrl()+"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [CommonParser] execute failure. ",e);
    }

    return results; 
  }

}
