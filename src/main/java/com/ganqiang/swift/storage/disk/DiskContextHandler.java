package com.ganqiang.swift.storage.disk;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.util.FileUtil;

public class DiskContextHandler implements Visitable
{

  public void init(Instance instance){
    boolean indownload = instance.isInIsdownload();
    boolean outdownload = instance.isOutIsdownload();
    String[] inseeds = instance.getInSeeds();
    if(inseeds != null){
      for(String site : inseeds){
        String key = Seed.getKey(instance.getId(), site);
        String insideAvatarPath = FileUtil.getInsideAvatarPath(instance.getDisk(), site);        
        FileUtil.makeDir(insideAvatarPath);
        Constants.inside_avatar_path_map.put(key, insideAvatarPath);
        if (indownload) {
          String insidePagePath = FileUtil.getInsidePagePath(instance.getDisk(), site);
          FileUtil.makeDir(insidePagePath);
          Constants.inside_page_path_map.put(key, insidePagePath);
        }
      }
    }
    if (outdownload) {
      String[] outseeds = instance.getOutSeeds();
      if(outseeds != null){
        for(String site : outseeds){
          String key = Seed.getKey(instance.getId(), site);
          String outsidePagePath = FileUtil.getOutsidePagePath(instance.getDisk(), site);
          FileUtil.makeDir(outsidePagePath);
          Constants.outside_page_path_map.put(key, outsidePagePath);
        }
      }
    }

  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitDiskContext(this);
  }

}
