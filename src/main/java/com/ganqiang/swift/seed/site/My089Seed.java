package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.My089LinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.My089Parser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class My089Seed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(My089Seed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.my089.com/Loan/default.aspx");
    Document doc = Jsoup.parse(html);  
    Elements elements = doc.select("span[class=z_page]");
    int totalpage = 0;
    if (!elements.isEmpty()) {
      String pageinfo = elements.text();
      totalpage = Integer.valueOf(pageinfo.replaceAll("共", "").replaceAll("页", ""));
    }
    logger.info("Loading [my089] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.my089.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.my089.com/Loan/default.aspx?pid=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.my089.com/Loan/Detail.aspx?sid=";
  }

  @Override
  public String getLogo()
  {
    return "https://www.my089.com/2013/img/new_nav_bg.png";
  }

  @Override
  public String getPlatform()
  {
    return "红岭创投";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new My089LinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new My089Parser();
  }

  @Override
  public String getDetailLinkMark()
  {
    // TODO Auto-generated method stub
    return "Loan/Detail.aspx?sid=";
  }

}
