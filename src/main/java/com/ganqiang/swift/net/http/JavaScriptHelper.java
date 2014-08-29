package com.ganqiang.swift.net.http;

import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.UrlQueue;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class JavaScriptHelper
{
  private static final Logger logger = Logger.getLogger(JavaScriptHelper.class);

  private static final WebClient webClient = new WebClient();

  static{
//    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
//    java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit.javascript").setLevel(Level.OFF); 
    java.util.logging.Logger.getLogger("net.sourceforge.htmlunit.corejs.javascript").setLevel(Level.OFF);  
    WebClientOptions option = webClient.getOptions();
    option.setJavaScriptEnabled(true);
    option.setCssEnabled(false);
    option.setActiveXNative(false);
    option.setAppletEnabled(false);
    option.setDoNotTrackEnabled(false);
    option.setGeolocationEnabled(false);
    option.setPopupBlockerEnabled(false);
    option.setPrintContentOnFailingStatusCode(false);
    option.setRedirectEnabled(false);
    option.setThrowExceptionOnFailingStatusCode(false);
    option.setThrowExceptionOnScriptError(false);
    option.setTimeout(Integer.MAX_VALUE); 
    option.setUseInsecureSSL(false);
//    webClient.setCssEnabled(false);
//    webClient.setThrowExceptionOnFailingStatusCode(false);
//    webClient.setThrowExceptionOnScriptError(false);
//    webClient.setJavaScriptEnabled(true);
    webClient.getCookieManager().setCookiesEnabled(true);
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.setJavaScriptTimeout(20000);
  }
  
  public static FetchedPage getFetchedPage(String key, String instanceid, SiteType type, String url){
    String inpath = Constants.inside_page_path_map.get(key);
    int statusCode = 200;
    String content = null;
    try {
      HtmlPage page = webClient.getPage(url);
      content = page.asXml();//如有js，则将其转换为html
      if (!StringUtil.isNullOrBlank(inpath)) {
        String filename = inpath + Constants.listpage_filename;
        String coding = "UTF-8";
//        if (type.equals(SiteType.YIRENDAI)) {
//          String num = url.split("=")[1];
//          filename +=  num.substring(0, num.length() - 1);
//        } else if (type.equals(SiteType.LUFAX)){
//          filename += url.split("listing/")[1].split("\\?")[0];
//        } else if (type.equals(SiteType.ELOANCN)){
//          filename += url.split("page=")[1];
//        } else if (type.equals(SiteType.YOOLI)){
//          filename += url.split("page/")[1].split("\\.")[0];
//        } else if (type.equals(SiteType.NIWODAI)){
//          filename += url.split("pageNo=")[1];
//        } else {
          filename += url.split("=")[1];
//        }
        if (type.equals(SiteType.ROYIDAI)){
          coding = "GBK";
        }

        FileUtil.writePage(instanceid, filename + ".html", content, coding);
      }
    } catch (Exception e) {
      statusCode = 500;
      UrlQueue.addUnVisitedUrl(key, url);
      logger.error("HTTP request list page error, url: [" + url + "] will be back on the request queue. ", e);
    }
    return new FetchedPage(url, content, statusCode);
  }
  
  
  public static FetchedPage getPage(String key, String instanceid, SiteType type, String link)
  {
    int statusCode = 200;
    String content = null;
    try { 
      HtmlPage page = webClient.getPage(link);
      content = page.asXml();//如有js，则将其转换为html
      String inpath = Constants.inside_page_path_map.get(key);
      if (!StringUtil.isNullOrBlank(inpath)) {
        String coding = "UTF-8";
        String filename = Constants.detailpage_filename;
        if (type.equals(SiteType.NIWODAI)) {
          filename += link.split("xiangmu/v-")[1].split("=.html")[0];
        } else {
          if (link.contains("=")) {
            filename += link.split("=")[1];
          } else if(link.contains("/")){
            String[] spliturl = link.split("/");
            filename += spliturl[spliturl.length - 1];
          }
        }
        if (type.equals(SiteType.ROYIDAI)) {
          coding = "GBK";
        }
        filename = inpath + filename + ".html";
        
        FileUtil.writePage(instanceid, filename, content, coding);
      }
    } catch (Exception e) {
      statusCode = 500;
      logger.error("HTTP request detail page error, url: [" + link + "] will be back on the request queue. ", e);
    }
    return new FetchedPage(link, content, statusCode);
  }

  public static void main(String... args) throws Exception {

  }
  
 
  
}
