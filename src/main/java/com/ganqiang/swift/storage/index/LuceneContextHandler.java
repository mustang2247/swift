package com.ganqiang.swift.storage.index;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public class LuceneContextHandler implements Visitable
{

  public void init(Instance instance)
  {
    if (!StringUtil.isNullOrBlank(instance.getIndex())) {
      String[] inseeds = instance.getInSeeds();
      if (inseeds != null) {
        for (String site : inseeds) {
          String key = Seed.getKey(instance.getId(), site);
          String insideIndexPath = FileUtil.getInsideIndexPath(
              instance.getIndex(), site);
          FileUtil.makeDir(insideIndexPath);
          Constants.inside_index_path_map.put(key, insideIndexPath);
        }
      }
    }
  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitLuceneContext(this);
  }

}
