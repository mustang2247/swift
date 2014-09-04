package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ganqiang.swift.fetch.site.WzdaiLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.CommonParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class WzdaiSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(WzdaiSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.wzdai.com/invest/index.html");
    Document doc = Jsoup.parse(html);   
    Element element = doc.select("div[align=center] span[style*=float]").get(0);
    String pageinfo = element.text().split("条")[1].split("页")[0];
    int totalpage = Integer.valueOf(pageinfo);
    logger.info("Loading [wzdai] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.wzdai.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "https://www.wzdai.com/invest/index.html?status=1&order=-1&page=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "https://www.wzdai.com/invest/detail.html?borrowid=";
  }

  @Override
  public String getLogo()
  {
    return "http://www.wzdai.com/themes/soonmes_wzd/images/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "温州贷";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new WzdaiLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new CommonParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
