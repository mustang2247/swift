package com.ganqiang.swift.seed;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed.SiteType;

public class PageSizeSettingHandler implements Visitable
{

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitInsideSeedPageSize(this); 
  }

  public void init(Instance instance){
    String[] inseeds = instance.getInSeeds();
    if (inseeds != null) {
      for (String inseed : inseeds) {
        SiteType type = SiteType.getType(inseed);
        InsideSeed insideSeed = Constants.seed_map.get(type);
        insideSeed.setPageSize();
      }
    }
  }

}
