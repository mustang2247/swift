package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.PpmoneyLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.PpmoneyParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class PpmoneySeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(PpmoneySeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.ppmoney.com/project");
    Document doc = Jsoup.parse(html);
    Elements elements = doc.select("span[class*=fc_dc0]");
    int totalpage = 0;
    if (!elements.isEmpty()) {
      String pageinfo = elements.get(1).text();
      totalpage = Integer.valueOf(pageinfo);
    }
    logger.info("Loading [ppmoney] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.ppmoney.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.ppmoney.com/project?page=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.ppmoney.com/Project/Detail/";
  }

  @Override
  public String getLogo()
  {
    return "http://www.ppmoney.com/images/basic/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "万惠投资";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new PpmoneyLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new PpmoneyParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "Project/Detail/";
  }


}
