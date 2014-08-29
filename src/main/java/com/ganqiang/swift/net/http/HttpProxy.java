package com.ganqiang.swift.net.http;

import org.apache.http.HttpHost;

public class HttpProxy
{
  private HttpHost httpHost;
  private String username;
  private String password;

  public HttpProxy(HttpHost httpHost){
    this.httpHost = httpHost;
  }

  public HttpProxy(HttpHost httpHost, String username, String password){
    this.httpHost = httpHost;
    this.username = username;
    this.password = password;
  }
  public HttpHost getHttpHost()
  {
    return httpHost;
  }
  public void setHttpHost(HttpHost httpHost)
  {
    this.httpHost = httpHost;
  }
  public String getUsername()
  {
    return username;
  }
  public void setUsername(String username)
  {
    this.username = username;
  }
  public String getPassword()
  {
    return password;
  }
  public void setPassword(String password)
  {
    this.password = password;
  }

  
}
