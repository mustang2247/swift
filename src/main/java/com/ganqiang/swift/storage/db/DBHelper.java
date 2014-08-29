package com.ganqiang.swift.storage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public final class DBHelper {

	private static final Logger logger = Logger.getLogger(DBHelper.class);

	public static void openTransaction(Connection conn) {
		try {
			conn.setAutoCommit(false);
//			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (SQLException e) {
			logger.error("Job " +Thread.currentThread().getName()+ " cannot to execute openTransaction method ",e.getCause());
		}
	}

	public static void commit(Connection conn) {
		try {
			conn.commit();
		} catch (SQLException e) {
			logger.error("Job " +Thread.currentThread().getName()+ " cannot to execute comit method ",e.getCause());
		}
	}

	public static void rollback(Connection conn) {
		try {
			conn.rollback();
		} catch (SQLException e) {
			logger.error("Job " +Thread.currentThread().getName()+ " cannot to execute rollback method ",e.getCause());
		}
	}

	public static void close(ResultSet rs,PreparedStatement psmt) {
    try {
      if (rs != null) {
        rs.close();
        rs = null;
      }
    } catch (SQLException e) {
      logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB resultset.", e.getCause());
    }finally{
      try {
        if (psmt != null) {
          psmt.close();
          psmt = null;
        }
      } catch (SQLException e) {
        logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB prepared statment.", e.getCause());
      }
    }
  }
	
	 public static void close(Connection con,ResultSet rs,PreparedStatement psmt) {
	    try {
	      if (rs != null) {
	        rs.close();
	        rs = null;
	      }
	    } catch (SQLException e) {
	      logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB resultset.", e.getCause());
	    }finally{
	      try {
	        if (psmt != null) {
	          psmt.close();
	          psmt = null;
	        }
	        if (con != null) {
	          con.close();
	          con = null;
	        }
	      } catch (SQLException e) {
	        logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB prepared statment or connection.", e.getCause());
	      }
	    }
	  }
	
	public static void close(Connection con,Statement st,ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
        rs = null;
      }
    } catch (SQLException e) {
      logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB resultset.", e.getCause());
    }finally{
      try {
        if (st != null) {
          st.close();
          st = null;
        }
        if (con != null) {
          con.close();
          con = null;
        }
      } catch (SQLException e) {
        logger.error("Job " +Thread.currentThread().getName()+ " cannot to close DB connection or statment.", e.getCause());
      }
    }
  }
	
}
