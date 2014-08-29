package com.ganqiang.swift.net.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;

public class HttpContextHandler implements Visitable
{

  private final int pool_max_conn = 100;
  private final int soket_timeout = 5000;
  private final int conn_timeout = 100000;
  private final int conn_request_timeout = 100000;
  private final boolean check_conn = false;
  private CloseableHttpClient httpclient;
  private RequestConfig globalRequestConfig;
  private DnsResolver dnsResolver;
  private Registry<ConnectionSocketFactory> socketFactoryRegistry;
  private ConnectionKeepAliveStrategy keepAliveStrategy;
  private HttpRequestRetryHandler retryHandler;

  public CloseableHttpClient getHttpClient(){
    return httpclient;
  }

  public RequestConfig getGlobalRequestConfig(){
    return globalRequestConfig;
  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitHttpContext(this);
  }

  public void init() {
    setDnsResolver();
    setSocketFactoryRegistry();
    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
        socketFactoryRegistry, dnsResolver);

    SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
    connManager.setDefaultSocketConfig(socketConfig);

    MessageConstraints messageConstraints = MessageConstraints.custom()
        .setMaxHeaderCount(200).setMaxLineLength(2000).build();

    ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setMalformedInputAction(CodingErrorAction.IGNORE)
        .setUnmappableInputAction(CodingErrorAction.IGNORE)
        .setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints)
        .build();

    connManager.setDefaultConnectionConfig(connectionConfig);
    connManager.setMaxTotal(pool_max_conn);
    // connManager.setDefaultMaxPerRoute(max_per_route);
    // connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)),
    // 20);

    globalRequestConfig = RequestConfig.custom()
        .setSocketTimeout(soket_timeout)
        .setConnectTimeout(conn_timeout)
        .setConnectionRequestTimeout(conn_request_timeout)
        .setCookieSpec(CookieSpecs.BEST_MATCH)
        .setExpectContinueEnabled(true)
        .setStaleConnectionCheckEnabled(true)
        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
        .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();

    setKeepAliveStrategy();
    setRetryHandler();

    // jre proxy service
    // SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
    //        ProxySelector.getDefault());
    httpclient = HttpClients.custom().setConnectionManager(connManager)
        // .setProxy(new HttpHost("62.135.151.112", 80))
        .setKeepAliveStrategy(keepAliveStrategy)
        .setRetryHandler(retryHandler)
        .setDefaultRequestConfig(globalRequestConfig).build();
    if (check_conn) {
      IdleConnectionEvictor connEvictor = new IdleConnectionEvictor(connManager);
      connEvictor.start();
    }

    Constants.http_request_config = globalRequestConfig;
    Constants.http_client = httpclient; 
  }

  //Use custom DNS resolver to override the system DNS resolution.
  private void setDnsResolver(){
    dnsResolver = new SystemDefaultDnsResolver()
    {
      @Override
      public InetAddress[] resolve(final String host)
          throws UnknownHostException
      {
        if (host.equalsIgnoreCase("myhost")) {
          return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
        } else {
          return super.resolve(host);
        }
      }
    };
  }

  //Create a registry of custom connection socket factories for supported
  // protocol schemes.
  private  void setSocketFactoryRegistry(){
    SSLContext sslcontext = SSLContexts.createSystemDefault();
    X509HostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();
    socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslcontext, hostnameVerifier))
        .build();
  }

  private  void setKeepAliveStrategy(){
    keepAliveStrategy = new ConnectionKeepAliveStrategy()
    {
      public long getKeepAliveDuration(HttpResponse response,
          HttpContext context)
      {
        // Honor 'keep-alive' header
        HeaderElementIterator it = new BasicHeaderElementIterator(
            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
          HeaderElement he = it.nextElement();
          String param = he.getName();
          String value = he.getValue();
          if (value != null && param.equalsIgnoreCase("timeout")) {
            try {
              return Long.parseLong(value) * 1000;
            } catch (NumberFormatException ignore) {
            }
          }
        }
        // keep alive for 30 seconds
        return 30 * 1000;
      }
    };
  }

  private  void setRetryHandler(){
    retryHandler = new HttpRequestRetryHandler()
    {
      public boolean retryRequest(IOException exception, int executionCount,
          HttpContext context)
      {
        // 如果超过最大重试次数，那么就不要继续了
        if (executionCount >= 5) {
          return false;
        }
        // 如果服务器丢掉了连接，那么就重试
        if (exception instanceof NoHttpResponseException) {
          return true;
        }
        // 不要重试SSL握手异常
        if (exception instanceof SSLHandshakeException) {
          return false;
        }
        HttpRequest request = (HttpRequest) context
            .getAttribute("http.request");
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        // 如果请求被认为是幂等的，那么就重试
        if (idempotent) {
          return true;
        }
        return false;
      }
    };
  }

  private  class IdleConnectionEvictor extends Thread
  {
    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;
    public IdleConnectionEvictor(HttpClientConnectionManager connMgr)
    {
      super();
      this.connMgr = connMgr;
    }

    @Override
    public void run()
    {
      try {
        while (!shutdown) {
          synchronized (this) {
            wait(5000);
            // Close expired connections
            connMgr.closeExpiredConnections();
            // Optionally, close connections
            // that have been idle longer than 5 sec
            connMgr.closeIdleConnections(5, TimeUnit.SECONDS);
          }
        }
      } catch (InterruptedException ex) {
        // terminate
      }
    }

    @SuppressWarnings("unused")
    public void shutdown()
    {
      shutdown = true;
      synchronized (this) {
        notifyAll();
      }
    }
  }
  
  public  void destory(){
    if(httpclient != null) {
      try {
        httpclient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
  }



}
