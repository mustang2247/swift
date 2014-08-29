package com.ganqiang.swift.net.http;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.python.modules.time.Time;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.util.StringUtil;

public abstract class AbstractHttpProxyHandler
{
  private static final Logger logger = Logger.getLogger(AbstractHttpProxyHandler.class);

  protected static void insideCheck(String[] keys, String[] urls,  List<HttpProxy> proxys)
  {
    ExecutorService executorService = Executors.newFixedThreadPool(urls.length);
    for(HttpProxy proxy : proxys){
      CheckThread[] threads = new CheckThread[urls.length];
      for (int i = 0; i < threads.length; i++) {
        threads[i] = new CheckThread(keys[i], urls[i], proxy);
//        threads[i].start();
        executorService.execute(threads[i]);
      }
    }
    executorService.shutdown();
  }

  protected static void outsideCheck(String id, String[] sites, List<HttpProxy> proxys)
  {
    ExecutorService executorService = Executors.newFixedThreadPool(sites.length);
    for(HttpProxy proxy : proxys){
      CheckThread[] threads = new CheckThread[sites.length];
      for (int i = 0; i < threads.length; i++) {
        String key = Seed.getKey(id, sites[i]);
        threads[i] = new CheckThread(key, sites[i], proxy);
//        threads[i].start();
        executorService.execute(threads[i]);
      }
    }
    executorService.shutdown();
  }

  static class CheckThread extends Thread {
    private String url;
    private String key;
    private HttpProxy proxy;
    public CheckThread(String key, String url, HttpProxy proxy)
    {
      this.url = url;
      this.key = key;
      this.proxy = proxy;
    }

    @Override
    public void run()
    {
      CloseableHttpResponse response = null;
      try {
        while(StringUtil.isNullOrBlank(url)){
          Time.sleep(1000);
        }
        HttpHost host = proxy.getHttpHost();
        HttpClientContext hcc = null;
        
        
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("User-Agent",HttpUserAgentLooper.getNext());
        RequestConfig requestConfig = RequestConfig.custom()
                        .setProxy(proxy.getHttpHost()).setConnectTimeout(1000).setSocketTimeout(10000).build();
        httpget.setConfig(requestConfig);
        
        
        if (!StringUtil.isNullOrBlank(proxy.getUsername()) || !StringUtil.isNullOrBlank(proxy.getPassword())) {
          hcc = HttpClientContext.create();
          CredentialsProvider credsProvider = new BasicCredentialsProvider();
          credsProvider.setCredentials(
              new AuthScope(host.getHostName(), host.getPort()),
              new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
          hcc.setCredentialsProvider(credsProvider);
//          AuthCache authCache = new BasicAuthCache();
//          BasicScheme basicAuth = new BasicScheme();
//          authCache.put(host, basicAuth);
//          hcc.setAuthCache(authCache);
          Constants.http_context_map.put(key, hcc);
        }
        
        if (hcc != null) {
          response = Constants.http_client.execute(httpget,hcc);
        } else {
          response = Constants.http_client.execute(httpget);
        }
        
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
          Constants.addHttpProxy(key, proxy);
          logger.info("Worker [" + Thread.currentThread().getName() + "] --- Http proxy ["+host.getHostName()+":"+host.getPort()+"] test ["+url+"] successful.");
        } else {
          logger.error("Worker [" + Thread.currentThread().getName() + "] --- Http proxy ["+host.getHostName()+":"+host.getPort()+"] test ["+url+"] failed.");
        }
      } catch (Exception ex) {
        logger.error("Worker [" + Thread.currentThread().getName() + "] --- Http proxy ["+proxy.getHttpHost().getHostName()+":"+proxy.getHttpHost().getPort()+"] test ["+url+"] failed.");
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

}
