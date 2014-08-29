package com.ganqiang.swift.prep;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.CalculateUtil;

public final class DelayHelper
{
  private final static Logger logger = Logger.getLogger(DelayHelper.class);

  public static void delay(String key, SiteType type, String instanceid){
    Long caltime = Constants.all_delay_map.get(key);
    if (caltime != null) {
      Long current = System.currentTimeMillis();
      if(caltime == 0) {
        Constants.all_delay_map.put(key, current);
      } else {
        Long confdelay = 0l;
        if (type.equals(SiteType.OUTSIDE)) {
          confdelay = Constants.outside_delay_map.get(instanceid);
        } else {
          confdelay = Constants.inside_delay_map.get(instanceid);
        }
        Double currentDelay = CalculateUtil.sub(current, caltime) / 1000;
        Double waittime = CalculateUtil.sub(confdelay, currentDelay);
        if (waittime > 0) {
          try {
            logger.info("Grab ["+key+"] need to delay "+waittime+" seconds.");
            Double wtsecond = CalculateUtil.mul(waittime, 1000);
            Thread.sleep(wtsecond.longValue());
            Constants.all_delay_map.put(key, System.currentTimeMillis());
          } catch (InterruptedException e) {
            logger.error("Delay ["+key+"] occur failed.", e);
          }
        }
      }
    }
  }

}
