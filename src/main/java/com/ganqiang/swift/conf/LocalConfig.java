package com.ganqiang.swift.conf;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.ganqiang.swift.net.http.HttpProxy;

public class LocalConfig
{
  private Integer threadNum;
  private List<Instance> instances;

  public Integer getThreadNum()
  {
    return threadNum;
  }

  public void setThreadNum(Integer threadNum)
  {
    this.threadNum = threadNum;
  }

  public List<Instance> getInstances()
  {
    return instances;
  }

  public void setInstances(List<Instance> instances)
  {
    this.instances = instances;
  }
  
  public static class Instance{
    private String id;
//    private boolean isConfTimer;
    private Long interval;
    private Date startTime;
//    private String[] httpProxys;
    private List<HttpProxy> httpProxys;
    private boolean inUseProxy = false;
    private boolean outUseProxy = false;
    private String[] inSeeds;
    private boolean inIsdownload = false;
    private boolean jssupport = false;
    private String[] outSeeds;
    private boolean outIsdownload = true;
    private boolean isSync = false;
    private String syncDomain;
    private boolean isCascade = false;
    private Integer depth = 1;
    private Long indelay = 0l;
    private Long outdelay = 0l;
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
    public String getIndex()
    {
      return index;
    }
    public void setIndex(String index)
    {
      if(!index.substring(index.length()-1, index.length()).equals(File.separator)) {
        index += File.separator;
      }
      this.index = index;
    }
    public List<HttpProxy> getHttpProxys()
    {
      return httpProxys;
    }
    public void setHttpProxys(List<HttpProxy> httpProxys)
    {
      this.httpProxys = httpProxys;
    }
    public Long getIndelay()
    {
      return indelay;
    }
    public void setIndelay(Long indelay)
    {
      this.indelay = indelay;
    }
    public Long getOutdelay()
    {
      return outdelay;
    }
    public void setOutdelay(Long outdelay)
    {
      this.outdelay = outdelay;
    }
    public String getSyncDomain()
    {
      return syncDomain;
    }
    public void setSyncDomain(String syncDomain)
    {
      this.syncDomain = syncDomain;
    }
    public boolean isInUseProxy()
    {
      return inUseProxy;
    }
    public void setInUseProxy(boolean inUseProxy)
    {
      this.inUseProxy = inUseProxy;
    }
    public boolean isOutUseProxy()
    {
      return outUseProxy;
    }
    public void setOutUseProxy(boolean outUseProxy)
    {
      this.outUseProxy = outUseProxy;
    }
//    public String[] getHttpProxys()
//    {
//      return httpProxys;
//    }
//    public void setHttpProxys(String[] httpProxys)
//    {
//      this.httpProxys = httpProxys;
//    }
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
    public boolean isSync()
    {
      return isSync;
    }
    public void setSync(boolean isSync)
    {
      this.isSync = isSync;
    }
    
    public boolean isJssupport()
    {
      return jssupport;
    }
    public void setJssupport(boolean jssupport)
    {
      this.jssupport = jssupport;
    }
    public String getId()
    {
      return id;
    }
    public void setId(String id)
    {
      this.id = id;
    }
    public String[] getInSeeds()
    {
      return inSeeds;
    }
    public void setInSeeds(String[] inSeeds)
    {
      this.inSeeds = inSeeds;
    }
    public boolean isInIsdownload()
    {
      return inIsdownload;
    }
    public void setInIsdownload(boolean inIsdownload)
    {
      this.inIsdownload = inIsdownload;
    }
    public String[] getOutSeeds()
    {
      return outSeeds;
    }
    public void setOutSeeds(String[] outSeeds)
    {
      this.outSeeds = outSeeds;
    }
    public boolean isOutIsdownload()
    {
      return outIsdownload;
    }
    public void setOutIsdownload(boolean outIsdownload)
    {
      this.outIsdownload = outIsdownload;
    }
//    public boolean isConfTimer()
//    {
//      return isConfTimer;
//    }
//    public void setConfTimer(boolean isConfTimer)
//    {
//      this.isConfTimer = isConfTimer;
//    }
    public Long getInterval()
    {
      return interval;
    }
    public void setInterval(Long interval)
    {
      this.interval = interval;
    }
    public Date getStartTime()
    {
      return startTime;
    }
    public void setStartTime(Date startTime)
    {
      this.startTime = startTime;
    }
   
    public String getDisk()
    {
      return disk;
    }

    public void setDisk(String disk)
    {
      this.disk = disk;
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
}
