package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.RoyidaiLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.RoyidaiParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class RoyidaiSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(RoyidaiSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.royidai.com/financelist.do");
    Document doc = Jsoup.parse(html);  
    Elements elements = doc.select(".pageDivClass");
    String pageinfo = elements.text().split("共")[1].split("页")[0];
    int totalpage = Integer.valueOf(pageinfo);
    logger.info("Loading [royidai] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.royidai.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.royidai.com/financelist.do?curPage=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.royidai.com/financeDetail.do?id=";
  }

  @Override
  public String getLogo()
  {
    return "http://www.royidai.com/images/logo.jpg";
  }

  @Override
  public String getPlatform()
  {
    return "讯贷网";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new RoyidaiLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new RoyidaiParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "financeDetail.do?id=";
  }

}
