package com.ganqiang.swift.storage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.Storable;
import com.ganqiang.swift.util.DateUtil;

public class DBStore implements Storable
{

  private static final Logger logger = Logger.getLogger(DBStore.class);

  private DataSource dataSource;
  
  public DBStore(DataSource dataSource){
    this.dataSource = dataSource;
  }

  @Override
  public <Store> Result readOne(Store... s)
  {
    String sql = (String)s[0];
    Connection con = null;
    Result rowData = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      con = getConnection(dataSource, true);
      pstmt = con.prepareStatement(sql);
      rs=pstmt.executeQuery();
      ResultSetMetaData md = rs.getMetaData();
      int columnCount = md.getColumnCount();
      while (rs.next()){   
        rowData = new Result();   
        for (int i=1; i<=columnCount; i++){   
          String column = md.getColumnLabel(i).toUpperCase();
          Object obj = rs.getObject(i);
          if("ID".equals(column)){
            rowData.setId(obj.toString());
          }else if("NAME".equals(column)){
            rowData.setName(obj.toString());
          }else if("PLATFORM".equals(column)){
            rowData.setPlatform(obj.toString());
          }else if("SITE".equals(column)){
            rowData.setSite(obj.toString());
          }else if("BORROWER".equals(column)){
            rowData.setBorrower(obj==null? null :obj.toString());
          }else if("MONEY".equals(column)){
            rowData.setMoney(obj==null? null :Double.valueOf(obj.toString()));
          }else if("YEAR_RATE".equals(column)){
            rowData.setYearRate(obj==null? null :Double.valueOf(obj.toString()));
          }else if("DAY_RATE".equals(column)){
            rowData.setDayRate(obj==null? null :Double.valueOf(obj.toString()));
          }else if("REPAY_LIMIT_TIME".equals(column)){
            rowData.setRepayLimitTime(obj==null? null :obj.toString());
          }else if("PROGRESS".equals(column)){
            rowData.setProgress(obj==null? null :Double.valueOf(obj.toString()));
          }else if("STATUS".equals(column)){
            rowData.setStatus(obj==null? null :obj.toString());
          }else if("REMAIN_TIME".equals(column)){
            rowData.setRemainTime(obj==null? null :obj.toString());
          }else if("REPAY_MODE".equals(column)){
            rowData.setRepayMode(obj==null? null :obj.toString());
          }else if("REPAY_PERMONTH".equals(column)){
            rowData.setRepayPerMonth(obj==null? null : Double.valueOf(obj.toString()));
          }else if("TOTAL_NUM".equals(column)){
            rowData.setTotalNum(obj==null? null : Integer.valueOf(obj.toString()));
          }else if("CATEGORY".equals(column)){
            rowData.setCategory(obj==null? null : obj.toString());
          }else if("REMAIN_MONEY".equals(column)){
            rowData.setRemainMoney(obj==null? null : Double.valueOf(obj.toString()));
          }else if("DETAIL_DESC".equals(column)){
            rowData.setDetailDesc(obj==null? null : obj.toString());
          }else if("REWARD".equals(column)){
            rowData.setReward(obj==null? null : obj.toString());
          }else if("AGENCY".equals(column)){
            rowData.setAgency(obj==null? null : obj.toString());
          }else if("START_TIME".equals(column)){
            rowData.setStartTime(obj==null? null : obj.toString());
          }else if("END_TIME".equals(column)){
            rowData.setEndTime(obj==null? null : obj.toString());
          }else if("SECURITY_MODE".equals(column)){
            rowData.setSecurityMode(obj==null? null : obj.toString());
          }else if("CREDIT_RATING".equals(column)){
            rowData.setCreditRating(obj==null? null : obj.toString());
          }else if("AVATAR".equals(column)){
            rowData.setAvatar(obj==null? null : obj.toString());
          }else if("URL".equals(column)){
            rowData.setUrl(obj==null? null : obj.toString());
          }else if("CREATE_TIME".equals(column)){
            rowData.setCreateTime(obj==null? null : DateUtil.parse(obj.toString()));
          }
        }
      }
    } catch (Exception e) {
      logger.error("Cannot to excute find method by sql : "+sql, e);
    } finally{
      DBHelper.close(con,rs, pstmt);
    }
    return rowData;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Store> void writeBatch(Store... s)
  {
    List<String> sqls = (List<String>)s[0];
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getConnection(dataSource, true);
      DBHelper.openTransaction(con);
      stmt = con.createStatement();
      for(String sql : sqls){
        stmt.addBatch(sql);
      }
      stmt.executeBatch();
      DBHelper.commit(con);
    } catch (SQLException e) {
      DBHelper.rollback(con);
      logger.error("Cannot to excute insert method by sql : "+sqls, e);
    } finally{
      DBHelper.close(con, stmt,rs);
    }
  }

  private Connection getConnection(DataSource dataSource,boolean flag) {
    Connection conn = null;
    try {
      if(flag){
        Future<Connection> future = dataSource.getConnectionAsync();
        while (!future.isDone()) {
          logger.info("Worker [" +Thread.currentThread().getName()+ "] connection is not yet available.It will auto sleep for 1 ms.");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException x) {
            logger.error("Worker [" + Thread.currentThread().getName() + "] auto sleep is blocked.",x);
          }
        }
        conn = future.get();
      } else {
        conn = dataSource.getConnection();
      }
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] db connection is fault.",e);
    }
    return conn;
  }

}
