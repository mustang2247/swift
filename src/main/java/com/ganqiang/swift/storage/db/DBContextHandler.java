package com.ganqiang.swift.storage.db;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;

public class DBContextHandler implements Visitable
{

  public void localInit(Instance instance){
    PoolProperties p = new PoolProperties();
    p.setUrl(instance.getDbUrl());
    p.setDriverClassName(instance.getDbDriver());
    p.setUsername(instance.getDbUsername());
    p.setPassword(instance.getDbPassword());
    p.setJmxEnabled(true);
    p.setTestWhileIdle(false);
    p.setTestOnBorrow(true);
    p.setValidationQuery("SELECT 1");
    p.setTestOnReturn(false);
    p.setLogValidationErrors(true);
    p.setValidationInterval(30000);
    p.setTimeBetweenEvictionRunsMillis(30000);
    p.setMaxActive(200);
    p.setInitialSize(instance.getDbPoolSize());
    p.setMaxWait(10000);
//    p.setRemoveAbandonedTimeout(120);
    p.setMinEvictableIdleTimeMillis(30000);
    p.setMinIdle(10);
    p.setLogAbandoned(true);
    p.setRemoveAbandoned(false);
    p.setJdbcInterceptors(
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    DataSource dataSource = new DataSource();
    dataSource.setPoolProperties(p);
    Constants.local_datasource_map.put(instance.getId(), dataSource);
  }
  
  public void remoteInit(){
    RemoteConfig config = Constants.remote_config;
    PoolProperties p = new PoolProperties();
    p.setUrl(config.getDbUrl());
    p.setDriverClassName(config.getDbDriver());
    p.setUsername(config.getDbUsername());
    p.setPassword(config.getDbPassword());
    p.setJmxEnabled(true);
    p.setTestWhileIdle(false);
    p.setTestOnBorrow(true);
    p.setValidationQuery("SELECT 1");
    p.setTestOnReturn(false);
    p.setLogValidationErrors(true);
    p.setValidationInterval(30000);
    p.setTimeBetweenEvictionRunsMillis(30000);
    p.setMaxActive(200);
    p.setInitialSize(config.getDbPoolSize());
    p.setMaxWait(10000);
//    p.setRemoveAbandonedTimeout(120);
    p.setMinEvictableIdleTimeMillis(30000);
    p.setMinIdle(10);
    p.setLogAbandoned(true);
    p.setRemoveAbandoned(false);
    p.setJdbcInterceptors(
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    DataSource dataSource = new DataSource();
    dataSource.setPoolProperties(p);
    Constants.remote_datasource = dataSource;
  }


  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitDBContext(this);
  }

}
