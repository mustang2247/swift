package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.CommonLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.NiwodaiParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class NiwodaiSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(NiwodaiSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.niwodai.com/xiangmu/");
    Document doc = Jsoup.parse(html);
    Elements page = doc.select(".pageout");
    if (page.isEmpty()) {
      return 0;
    }
    Elements elements = page.first().children();
    int num = elements.size() - 2;
    int totalpage = Integer.valueOf(elements.get(num).text());
//    String pageinfo = elements.text().split("/")[1].split("页")[0];
//    int totalpage = Integer.valueOf(pageinfo);
    logger.info("Loading [niwodai] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.niwodai.com/";
  }

  @Override
  public String getPreListLink()
  {
//    return "http://www.niwodai.com/loan/loan.do?slist=-1&totalCount=200&pageNo=";
    return "http://www.niwodai.com/loan/loan.do?totalCount=2000&pageNo=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.niwodai.com/xiangmu/v-";
  }

  @Override
  public String getLogo()
  {
    return "http://static.niwodai.com/Public/Static/201310/images/img/logo.gif";
  }

  @Override
  public String getPlatform()
  {
    return "你我贷";
  }

  @Override
  public void setFetcher()
  {
    this.fetcher = new CommonLinkFetcher();
  }

  @Override
  public void setParser()
  {
    this.parser = new NiwodaiParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "xiangmu/v-";
  }


}
