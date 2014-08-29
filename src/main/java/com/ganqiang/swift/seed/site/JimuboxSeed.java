package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;

import com.ganqiang.swift.fetch.site.CommonLinkFetcher;
import com.ganqiang.swift.parse.site.JimuboxParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class JimuboxSeed extends AbstractSeed implements InsideSeed
{

  private static final Logger logger = Logger.getLogger(JimuboxSeed.class);

  /**
   * 没总页数显示，需要爬取多次获取，暂时只爬取前20页
   */
  @Override
  public int loadPageSize()
  {
    int totalpage = 10;
    logger.info("Loading [jimubox] list page number is ["+ totalpage +"]");
    return totalpage;
  }

  @Override
  public String getHomePage()
  {
    return "http://www.jimubox.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.jimubox.com/Project/List?page=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.jimubox.com/Project/Index/";
  }

  @Override
  public String getLogo()
  {
    return "http://www.jimubox.com/Content/img/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "积木盒子";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new CommonLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new JimuboxParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "Project/Index/";
  }


}
