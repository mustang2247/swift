package com.ganqiang.swift.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.UrlQueue;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class HttpHelper
{

  private static final Logger logger = Logger.getLogger(HttpHelper.class);

  /**
   * www.website.com ===> http://www.website.com
   */
  public static String formatOutSideUrl(String url){
    if (StringUtil.isNullOrBlank(url)) {
      return url;
    }
    if (!url.contains("http://") && !url.contains("https://")) {
      url = "http://" + url.trim();
    }
    return url;
  }
  
  /**
   * http://www.website.com ===> www.website.com
   */
  public static String parseOutSideUrl(String url){
    if (StringUtil.isNullOrBlank(url)) {
      return url;
    }
    return url.replaceAll("http://", "").replaceAll("https://", "");
  }

  /**
   * http://www.website.com/中文/私服 ===> http://www.website.com/%E7%94%B5%E4%BF%/%8F%E8%90.jpg
   */
  public static String encodingUrl(String url){
    String last = url.substring(url.length() - 1, url.length());
    if (last.equals("/")) {
      url = url.substring(0, url.length()- 1);
    }
    String[] array = url.split("/");
    String newurl = "";
    for (int i=0; i<array.length; i++) {
      String str = array[i];
      if (StringUtil.isContainChinese(str)) {
        try {
          str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }  
      }
      newurl += str ;
      if (i < array.length -1) {
        newurl += "/";
      }
    }
    return newurl;
  }

  public static List<String> extractOutsideLinks(String uri, String sourceurl, String path)
  {
    List<String> links = new ArrayList<String>();
    try {
      String content = getContentFromUrl(uri);
      uri = parseOutSideUrl(uri);
      FileUtil.writeOutsidePage(uri, content, sourceurl, path);
      if (sourceurl.substring(sourceurl.length() - 1, sourceurl.length()).equals("/")) {
        sourceurl = sourceurl.substring(0, sourceurl.length() - 1);
      }
      Document doc = Jsoup.parse(content);
      Elements elinks = doc.select("a[href]");
      for (Element link : elinks) {
        String href = link.attr("href");
        if (StringUtil.isNullOrBlank(href)) {
          continue;
        }
        if (href.substring(0, 1).equals("/")) {
          href = sourceurl + href;
        }
        
        if (!href.contains(sourceurl)) {
          continue;
        }
        
        if (links.contains(href)) {
          continue;
        }
        links.add(href);
      }
      content = null;
      doc = null;
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] ---  ", e);
    }
    return links;
  }

  public static FetchedPage getPage(String key, String instanceid, SiteType type, String link, boolean isProxy)
  {
    int statusCode = 500;
    String content = null;
    HttpGet getHttp = new HttpGet(link);
    HttpClientContext hcc = Constants.http_context_map.get(key);
    if (isProxy) {
      HttpProxyLooper hp = Constants.proxy_map.get(key);
      if(hp == null){
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        hp = Constants.proxy_map.get(key);
      }
      HttpProxy proxy = hp.next();
      if (proxy != null) {
        if (proxy.getHttpHost() != null) {
          HttpHost host = proxy.getHttpHost();
          RequestConfig requestConfig = RequestConfig.copy(Constants.http_request_config)
              .setProxy(host).build();
          getHttp.setConfig(requestConfig);
          logger.info("Worker [" + Thread.currentThread().getName() + "] switch ip proxy ["+host.getHostName()
              + ":" + host.getPort() + "] in order to access detail page ["+link+"].");
//          if (!StringUtil.isNullOrBlank(proxy.getUsername()) && !StringUtil.isNullOrBlank(proxy.getPassword())) {
//            hcc = new HttpClientContext();
//            CredentialsProvider credsProvider = new BasicCredentialsProvider();
//            credsProvider.setCredentials(
//                new AuthScope(host.getHostName(), host.getPort()),
//                new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
//            hcc.setCredentialsProvider(credsProvider);
//          }
        }
      }
    }

    CloseableHttpResponse response = null;
    getHttp.setHeader("User-Agent",HttpUserAgentLooper.getNext());
    try {
      if (hcc != null) {
        response = Constants.http_client.execute(getHttp,hcc);
      } else {
        response = Constants.http_client.execute(getHttp);
      }
      statusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      if(entity != null){
        content = readHtmlFromEntity(entity);
        if (type.equals(SiteType.OUTSIDE)) {
          String outpath = Constants.outside_page_path_map.get(key);
          if (!StringUtil.isNullOrBlank(outpath)) {
            URI uri = new URI(link);
            String flink = link.replace(uri.getHost() + "/", "");
            String filename = outpath + parseOutSideUrl(flink).replaceAll("/", "_");
            if (filename.substring(filename.length() - 1, filename.length()).equals("_")) {
              filename = filename.substring(0, filename.length() - 1) + ".html";
            } else {
              filename = filename + ".html";
            }
            if (filename.length() > 254) {
              filename = filename.substring(0, 250);
            }
            FileUtil.writePage(instanceid, filename, content, "UTF-8");
          }
        } else {
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
            filename = inpath + filename + ".html";
            if (type.equals(SiteType.LUFAX)){
              //将js修改，不然打开会自动跳转到lufax官方站点
              content = content.replaceAll("productDetailRaise.js", "product_DetailRaise.js").replaceAll("productDetailAuction.js", "product_DetailAuction.js");
            } else if (type.equals(SiteType.ROYIDAI)) {
              coding = "GBK";
            }
            FileUtil.writePage(instanceid, filename, content, coding);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP request detail page error. ", e);
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new FetchedPage(link, content, statusCode);
  }

  public static FetchedPage getFetchedPage(String key, String instanceid, SiteType type, String url, boolean isProxy)
  {
    String inpath = Constants.inside_page_path_map.get(key);
    int statusCode = 500;
    String content = null;
    HttpGet getHttp = new HttpGet(url);
    CloseableHttpResponse response = null;
    getHttp.setHeader("User-Agent",HttpUserAgentLooper.getNext());
    HttpClientContext hcc = Constants.http_context_map.get(key);
    if (isProxy) {
      HttpProxyLooper hp = Constants.proxy_map.get(key);
      if(hp == null){
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        hp = Constants.proxy_map.get(key);
      }else{
        HttpProxy proxy = hp.next();
        if (proxy != null) {
          if (proxy.getHttpHost() != null) {
            HttpHost host = proxy.getHttpHost();
            RequestConfig requestConfig = RequestConfig.copy(Constants.http_request_config)
                .setProxy(host).build();
            getHttp.setConfig(requestConfig);
            logger.info("Worker [" + Thread.currentThread().getName() + "] switch ip proxy ["+host.getHostName()
                + ":" + host.getPort() + "] in order to access detail page ["+url+"].");
//            if (!StringUtil.isNullOrBlank(proxy.getUsername()) && !StringUtil.isNullOrBlank(proxy.getPassword())) {
//              hcc = new HttpClientContext();
//              CredentialsProvider credsProvider = new BasicCredentialsProvider();
//              credsProvider.setCredentials(
//                  new AuthScope(host.getHostName(), host.getPort()),
//                  new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
//              hcc.setCredentialsProvider(credsProvider);
//            }
          }
        }
      }
      
      if (hp == null) {
        RequestConfig requestConfig = RequestConfig.copy(Constants.http_request_config).build();
        getHttp.setConfig(requestConfig);
        logger.warn("Worker [" + Thread.currentThread().getName() + "] have not get proxy for access detail page ["+url+"].");
      }
      
    } else {
      RequestConfig requestConfig = RequestConfig.copy(Constants.http_request_config).build();
      getHttp.setConfig(requestConfig);
    }
    try {
      if (Constants.http_client == null) {
        Thread.sleep(2000);
      }
      if (hcc != null) {
        response = Constants.http_client.execute(getHttp,hcc);
      } else {
        response = Constants.http_client.execute(getHttp);
      }
      statusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      if(entity != null){
        content = readHtmlFromEntity(entity);
        if (!StringUtil.isNullOrBlank(inpath)) {
          String filename = inpath + Constants.listpage_filename;
          String coding = "UTF-8";
          if (type.equals(SiteType.YIRENDAI)) {
            String num = url.split("=")[1];
            filename +=  num.substring(0, num.length() - 1);
          } else if (type.equals(SiteType.LUFAX)){
            filename += url.split("listing/")[1].split("\\?")[0];
          } else if (type.equals(SiteType.ELOANCN)){
            filename += url.split("page=")[1];
          } else if (type.equals(SiteType.YOOLI)){
            filename += url.split("page/")[1].split("\\.")[0];
          } else if (type.equals(SiteType.NIWODAI)){
            filename += url.split("pageNo=")[1];
          } else if (type.equals(SiteType.PPDAI)){
            filename += url.split("12_s0_p")[1];
          } else if (type.equals(SiteType.FIRSTP2P)){
            filename += url.split("offset=")[1];
          } else {
            filename += url.split("=")[1];
          }
          if (type.equals(SiteType.ELOANCN) || type.equals(SiteType.MY089) || type.equals(SiteType.ROYIDAI)){
            coding = "GBK";
          }
          FileUtil.writePage(instanceid, filename + ".html", content, coding);
        }
      }
    } catch (Exception e) {
      UrlQueue.addUnVisitedUrl(key, url);
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP request list page error, " +
          "url: [" + url + "] will be back on the request queue. ", e);
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP request list page error, " +
            "url: [" + url + "] will be back on the request queue. ", e);
      }
    }
    return new FetchedPage(url, content, statusCode);
  }

  public static String getContentFromUrl(String url)
  {
    String content = null;
    HttpGet getHttp = new HttpGet(url);
    final HttpUserAgentLooper ua = new HttpUserAgentLooper();
    getHttp.setHeader("User-Agent", ua.next().toString());
    CloseableHttpResponse response = null;
    try {
      response = Constants.http_client.execute(getHttp);
      HttpEntity entity = response.getEntity();
      content = readHtmlFromEntity(entity);
    } catch (Exception e) {
      logger.error("Url: [" + url + "] has been disconnected.", e);
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return content;
  }

  private static String readHtmlFromEntity(HttpEntity httpEntity)  {
    String html = "";
    try {
      InputStream in = httpEntity.getContent();
      html = readInStreamToString(in);
      if(in != null){
         in.close();
      }
//      if(httpEntity.getContentLength() < 2147483647L){
//        html = EntityUtils.toString(httpEntity, Consts.UTF_8);
//      } else {
//        InputStream in = httpEntity.getContent();
//        html = readInStreamToString(in);
//        if(in != null){
//           in.close();
//        }
//      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return html;
  }

  private static String readInStreamToString(InputStream in) throws IOException {
    StringBuilder str = new StringBuilder();
    String line;
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, Consts.UTF_8));
    while((line = bufferedReader.readLine()) != null){
        str.append(line);
        str.append("\n");
    }
    if(bufferedReader != null) {
        bufferedReader.close();
    }
    return str.toString();
  }

  public static void main(String... args){
    String img = "http://www.edai365.cn/Uploads/2012/7-31/2012073122391675361夕阳.jpg";
    HttpContextHandler hc = new HttpContextHandler();
    hc.init();
    downloadImage("",img,true,"/opt/abc.jpg");
  }
  
  /**
   * isdynamic = true : url为动态url，newname中不包含后缀名
   * isdynamic = false : url为静态url，newname中包含后缀名
   */
  public static String downloadImage(String instanceid, String url, boolean isdynamic, String newname)
  {
    HttpGet getHttp = new HttpGet(url);
    getHttp.setHeader("User-Agent",HttpUserAgentLooper.getNext());
    CloseableHttpResponse response = null;
    HttpEntity entity = null; 
    try {
      response = Constants.http_client.execute(getHttp);
      entity = response.getEntity();
      if (entity != null) {
        if (isdynamic) {
          String mime = entity.getContentType().getValue();
          if (mime.contains("image")) {
            newname += "." + mime.split("image/")[1].split(";")[0];
          } else if(!newname.contains(".")){
            newname += ".jpg";
          }
        }
        FileUtil.writeImage(instanceid, newname, EntityUtils.toByteArray(entity));
      }
    } catch (Exception e) {
      logger.info("Downloading file error,Url: [" + url + "] ", e);
    } finally {
      try {
        EntityUtils.consume(entity);
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return newname;
  }

  @SuppressWarnings("unchecked")
  public static void login(Map<String, String> paraMap)
  {
    CloseableHttpResponse response = null;
    HttpPost post = new HttpPost(paraMap.get("login_url"));

    List<NameValuePair> param = new ArrayList<NameValuePair>();
    Iterator<?> it = paraMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String> parmEntry = (Entry<String, String>) it.next();
      param
          .add(new BasicNameValuePair(parmEntry.getKey(), parmEntry.getValue()));
    }
    try {
      UrlEncodedFormEntity params = new UrlEncodedFormEntity(param,
          Consts.UTF_8);
      post.setEntity(params);
      response = Constants.http_client.execute(post);
      post.releaseConnection();

      // String newUrl = paraMap.get("login_url");
      // HttpGet get = new HttpGet(newUrl);
      // CloseableHttpResponse response2 = httpclient.execute(get);
      post.abort();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
