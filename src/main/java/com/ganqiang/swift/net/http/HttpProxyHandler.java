package com.ganqiang.swift.net.http;

import java.util.List;

import com.ganqiang.swift.conf.LocalConfig;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;

public class HttpProxyHandler extends AbstractHttpProxyHandler implements Visitable
{

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitHttpProxy(this);
  }

  public void localCheck()
  {
    LocalConfig conf = Constants.local_config;
    List<Instance> list = conf.getInstances();
    for(Instance instance : list){
      remoteCheck(instance);
    }
  }

  public void remoteCheck(Instance instance){
    String id = instance.getId();
    List<HttpProxy> proxys = instance.getHttpProxys();
    String[] inseeds = instance.getInSeeds();
    String[] outseeds = instance.getOutSeeds();
    if(instance.isInUseProxy()){
      if (inseeds != null) {
        String[] urls = new String[inseeds.length];
        String[] keys = new String[inseeds.length];
        for (int i=0; i<inseeds.length; i++) {
          String seed = inseeds[i];
          String key = Seed.getKey(id, seed);
          SiteType type = SiteType.getType(seed);
          urls[i] = Constants.seed_map.get(type).getHomePage();
          keys[i] = key;
        }
        insideCheck(keys, urls, proxys);
      }
    }
    if(instance.isOutUseProxy()){
      if (outseeds != null) {
        String[] sites = new String[outseeds.length];
        for (int i=0; i<outseeds.length; i++) {
          String seed = outseeds[i];
          sites[i] = seed;
        }
        outsideCheck(id, outseeds, proxys);
      }
    }
  }

}
