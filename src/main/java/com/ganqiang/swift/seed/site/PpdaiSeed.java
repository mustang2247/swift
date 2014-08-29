package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.PpdaiLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.PpdaiParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class PpdaiSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(PpdaiSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.ppdai.com/lend");
    Document doc = Jsoup.parse(html);
    Elements elements = doc.getElementsByClass("fen_ye_nav");
    String[] pageinfo = elements.text().split(" ");
    String totalpage = pageinfo[pageinfo.length - 1].replaceAll("共", "").replaceAll("页", "");
    logger.info("Loading [ppdai] list total page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.ppdai.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.ppdai.com/lend/12_s0_p";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.ppdai.com/list";
  }

  @Override
  public String getLogo()
  {
    return "http://static.niwodai.com/Public/Static/201310/images/img/logo.gif";
  }

  @Override
  public String getPlatform()
  {
    return "拍拍贷";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new PpdaiLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new PpdaiParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "list/";
  }

}
