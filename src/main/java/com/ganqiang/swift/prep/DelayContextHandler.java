package com.ganqiang.swift.prep;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.seed.Seed;

public class DelayContextHandler implements Visitable
{

  public void init(Instance instance)
  {
    if (!Constants.inside_delay_map.isEmpty() && 
        Constants.inside_delay_map.get(instance.getId()) != 0) {
      String[] inseeds = instance.getInSeeds();
      if(inseeds != null){
        for(String site : inseeds){
          String key = Seed.getKey(instance.getId(), site);
          Constants.all_delay_map.put(key, 0l);
        }
      }
    }
    if (!Constants.outside_delay_map.isEmpty() && 
        Constants.outside_delay_map.get(instance.getId()) != 0) {
      String[] outseeds = instance.getOutSeeds();
      if(outseeds != null){
        for(String site : outseeds){
          String key = Seed.getKey(instance.getId(), site);
          Constants.all_delay_map.put(key, 0l);
        }
      }
    }
  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitDelayAccessContext(this);
  }

}
