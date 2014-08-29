package com.ganqiang.swift.seed;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.site.Edai365Seed;
import com.ganqiang.swift.seed.site.EloancnSeed;
import com.ganqiang.swift.seed.site.Firstp2pSeed;
import com.ganqiang.swift.seed.site.JimuboxSeed;
import com.ganqiang.swift.seed.site.LufaxSeed;
import com.ganqiang.swift.seed.site.My089Seed;
import com.ganqiang.swift.seed.site.NiwodaiSeed;
import com.ganqiang.swift.seed.site.OutsideSeed;
import com.ganqiang.swift.seed.site.PpdaiSeed;
import com.ganqiang.swift.seed.site.PpmoneySeed;
import com.ganqiang.swift.seed.site.RenrendaiSeed;
import com.ganqiang.swift.seed.site.RoyidaiSeed;
import com.ganqiang.swift.seed.site.WzdaiSeed;
import com.ganqiang.swift.seed.site.YirendaiSeed;
import com.ganqiang.swift.seed.site.YooliSeed;

public class SeedContextHandler implements Visitable
{
  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitInsideSeed(this);
  }

  public void register(){
    InsideSeed edai365 = new Edai365Seed();
    InsideSeed eloancn = new EloancnSeed();
    InsideSeed jimubox = new JimuboxSeed();
    InsideSeed lufax = new LufaxSeed();
    InsideSeed my089 = new My089Seed();
    InsideSeed niwodai = new NiwodaiSeed();
    InsideSeed outside = new OutsideSeed();
    InsideSeed ppdai = new PpdaiSeed();
    InsideSeed ppmoney = new PpmoneySeed();
    InsideSeed renrendai = new RenrendaiSeed();
    InsideSeed royidai = new RoyidaiSeed();
    InsideSeed wzdai = new WzdaiSeed();
    InsideSeed yirendai = new YirendaiSeed();
    InsideSeed yooli = new YooliSeed();
    InsideSeed firstp2p = new Firstp2pSeed();
    
    Constants.seed_map.put(SiteType.EDAI365, edai365);
    Constants.seed_map.put(SiteType.ELOANCN, eloancn);
    Constants.seed_map.put(SiteType.JIMUBOX, jimubox);
    Constants.seed_map.put(SiteType.LUFAX, lufax);
    Constants.seed_map.put(SiteType.MY089, my089);
    Constants.seed_map.put(SiteType.NIWODAI, niwodai);
    Constants.seed_map.put(SiteType.OUTSIDE, outside);
    Constants.seed_map.put(SiteType.PPDAI, ppdai);
    Constants.seed_map.put(SiteType.PPMONEY, ppmoney);
    Constants.seed_map.put(SiteType.RENRENDAI, renrendai);
    Constants.seed_map.put(SiteType.ROYIDAI, royidai);
    Constants.seed_map.put(SiteType.WZDAI, wzdai);
    Constants.seed_map.put(SiteType.YIRENDAI, yirendai);
    Constants.seed_map.put(SiteType.YOOLI, yooli);
    Constants.seed_map.put(SiteType.FIRSTP2P, firstp2p);
  }

}
