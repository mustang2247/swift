package com.ganqiang.swift.conf;

public class RemoteConfig
{
  private Integer threadNum;
  private Integer port;
  private String protocal;//binary, compact, json
  private String server;//simple，hsha，threadpool，nonblocking
  private boolean isSync = false;
  private String syncDomain;
  private boolean isCascade = false;
  private Integer depth = 1;
  private String disk;
  private String index;
  private String dbDriver;
  private String dbUrl;
  private String dbUsername;
  private String dbPassword;
  private Integer dbPoolSize;
  private String thriftServer;
  private Integer seqId;
  private Integer totalNodes;
  private String address;

  public Integer getSeqId() {
	return seqId;
}
public void setSeqId(Integer seqId) {
	this.seqId = seqId;
}
public Integer getTotalNodes() {
	return totalNodes;
}
public void setTotalNodes(Integer totalNodes) {
	this.totalNodes = totalNodes;
}
public String getAddress() {
	return address;
}
public void setAddress(String address) {
	this.address = address;
}
public String getThriftServer()
  {
    return thriftServer;
  }
  public void setThriftServer(String thriftServer)
  {
    this.thriftServer = thriftServer;
  }
  public String getProtocal()
  {
    return protocal;
  }
  public void setProtocal(String protocal)
  {
    this.protocal = protocal;
  }
  public Integer getPort()
  {
    return port;
  }
  public void setPort(Integer port)
  {
    this.port = port;
  }
  public String getServer()
  {
    return server;
  }
  public void setServer(String server)
  {
    this.server = server;
  }
  public Integer getThreadNum()
  {
    return threadNum;
  }
  public void setThreadNum(Integer threadNum)
  {
    this.threadNum = threadNum;
  }
  public boolean isSync()
  {
    return isSync;
  }
  public void setSync(boolean isSync)
  {
    this.isSync = isSync;
  }
  public String getSyncDomain()
  {
    return syncDomain;
  }
  public void setSyncDomain(String syncDomain)
  {
    this.syncDomain = syncDomain;
  }
  public boolean isCascade()
  {
    return isCascade;
  }
  public void setCascade(boolean isCascade)
  {
    this.isCascade = isCascade;
  }
  public Integer getDepth()
  {
    return depth;
  }
  public void setDepth(Integer depth)
  {
    this.depth = depth;
  }
  public String getDisk()
  {
    return disk;
  }
  public void setDisk(String disk)
  {
    if (disk.endsWith("/")) {
      disk = disk.substring(0, disk.length()-1);
    }
    this.disk = disk;
  }
  public String getIndex()
  {
    return index;
  }
  public void setIndex(String index)
  {
    if (index.endsWith("/")) {
      index = index.substring(0, index.length()-1);
    }
    this.index = index;
  }
  public String getDbDriver()
  {
    return dbDriver;
  }
  public void setDbDriver(String dbDriver)
  {
    this.dbDriver = dbDriver;
  }
  public String getDbUrl()
  {
    return dbUrl;
  }
  public void setDbUrl(String dbUrl)
  {
    this.dbUrl = dbUrl;
  }
  public String getDbUsername()
  {
    return dbUsername;
  }
  public void setDbUsername(String dbUsername)
  {
    this.dbUsername = dbUsername;
  }
  public String getDbPassword()
  {
    return dbPassword;
  }
  public void setDbPassword(String dbPassword)
  {
    this.dbPassword = dbPassword;
  }
  public Integer getDbPoolSize()
  {
    return dbPoolSize;
  }
  public void setDbPoolSize(Integer dbPoolSize)
  {
    this.dbPoolSize = dbPoolSize;
  }

}
