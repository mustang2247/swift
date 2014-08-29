package com.ganqiang.swift.conf;

public class SyncConfig
{

  private String selector;
  private String remoteHost;
  private String remoteUser;
  private String remotePwd;
  private String remoteModule;
  private String remoteDir;
  private String remotePort;
  private String remotePath;
  private String localPwdFile;

  public String getSelector()
  {
    return selector;
  }
  public void setSelector(String selector)
  {
    this.selector = selector;
  }
  public String getRemoteHost()
  {
    return remoteHost;
  }
  public void setRemoteHost(String remoteHost)
  {
    this.remoteHost = remoteHost;
  }
  public String getRemoteUser()
  {
    return remoteUser;
  }
  public void setRemoteUser(String remoteUser)
  {
    this.remoteUser = remoteUser;
  }
  public String getRemotePwd()
  {
    return remotePwd;
  }
  public void setRemotePwd(String remotePwd)
  {
    this.remotePwd = remotePwd;
  }
  public String getRemoteModule()
  {
    return remoteModule;
  }
  public void setRemoteModule(String remoteModule)
  {
    this.remoteModule = remoteModule;
  }
  public String getRemoteDir()
  {
    return remoteDir;
  }
  public void setRemoteDir(String remoteDir)
  {
    this.remoteDir = remoteDir;
  }
  public String getRemotePort()
  {
    return remotePort;
  }
  public void setRemotePort(String remotePort)
  {
    this.remotePort = remotePort;
  }
  public String getLocalPwdFile()
  {
    return localPwdFile;
  }
  public void setLocalPwdFile(String localPwdFile)
  {
    this.localPwdFile = localPwdFile;
  }
  public String getRemotePath()
  {
    return remotePath;
  }
  public void setRemotePath(String remotePath)
  {
    this.remotePath = remotePath;
  }
}
