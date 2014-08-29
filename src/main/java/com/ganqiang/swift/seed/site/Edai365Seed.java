package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.Edai365LinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.CommonParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class Edai365Seed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(Edai365Seed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.edai365.cn/Lend/Cloanlist.aspx");
    Document doc = Jsoup.parse(html);  
    Elements elements = doc.select(".paginator");
    String pageinfo = elements.text().split("/")[1].split("页")[0];
    int totalpage = Integer.valueOf(pageinfo);
    logger.info("Loading [edai365] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.edai365.cn/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.edai365.cn/Lend/Cloanlist.aspx?page=";
  }

  @Override
  public String getPreDetailLink()
  {
    return null;
  }

  @Override
  public String getLogo()
  {
    return "http://www.edai365.cn/Images/logo.gif";
  }

  @Override
  public String getPlatform()
  {
    return "365易贷";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new Edai365LinkFetcher();
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
