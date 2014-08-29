package com.ganqiang.swift.seed.site;

import com.ganqiang.swift.parse.site.OutsideParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public class OutsideSeed extends AbstractSeed implements InsideSeed
{

  @Override
  public String getHomePage()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPreListLink()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPreDetailLink()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDetailLinkMark()
  {
    return null;
  }

  @Override
  public String getLogo()
  {
    return null;
  }

  @Override
  public String getPlatform()
  {
    return null;
  }

  @Override
  protected void setFetcher()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void setParser()
  {
    this.parser = new OutsideParser();
  }

  @Override
  protected int loadPageSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }

}
