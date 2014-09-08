package com.ganqiang.swift.storage.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.Storable;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.StringUtil;

@SuppressWarnings({"unchecked" })
public class HBaseStore implements Storable {

    private static final Logger logger = Logger.getLogger(HBaseStore.class);

    @Override
    public <Store> Result readOne(Store... s) {
        HConnectionController hcc = (HConnectionController)s[0];
        String rowKey = (String)s[1];
        HTableInterface table = null;
        try {
            table = hcc.getHTableInterface(Constants.hbase_table_name);
            Get get = new Get(rowKey.getBytes());  
            org.apache.hadoop.hbase.client.Result rs = table.get(get);
            Result rowData = new Result();
            for (Cell cell : rs.rawCells()) {
                String column = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                  if("ID".equals(column)){
                    rowData.setId(value);
                  }else if("NAME".equals(column)){
                    rowData.setName(value);
                  }else if("PLATFORM".equals(column)){
                    rowData.setPlatform(value);
                  }else if("SITE".equals(column)){
                    rowData.setSite(value);
                  }else if("BORROWER".equals(column)){
                    rowData.setBorrower(value);
                  }else if("MONEY".equals(column)){
                    rowData.setMoney(Double.valueOf(value));
                  }else if("YEAR_RATE".equals(column)){
                    rowData.setYearRate(Double.valueOf(value));
                  }else if("DAY_RATE".equals(column)){
                    rowData.setDayRate(Double.valueOf(value));
                  }else if("REPAY_LIMIT_TIME".equals(column)){
                    rowData.setRepayLimitTime(value);
                  }else if("PROGRESS".equals(column)){
                    rowData.setProgress(Double.valueOf(value));
                  }else if("STATUS".equals(column)){
                    rowData.setStatus(value);
                  }else if("REMAIN_TIME".equals(column)){
                    rowData.setRemainTime(value);
                  }else if("REPAY_MODE".equals(column)){
                    rowData.setRepayMode(value);
                  }else if("REPAY_PERMONTH".equals(column)){
                    rowData.setRepayPerMonth( Double.valueOf(value));
                  }else if("TOTAL_NUM".equals(column)){
                    rowData.setTotalNum( Integer.valueOf(value));
                  }else if("CATEGORY".equals(column)){
                    rowData.setCategory( value);
                  }else if("REMAIN_MONEY".equals(column)){
                    rowData.setRemainMoney( Double.valueOf(value));
                  }else if("DETAIL_DESC".equals(column)){
                    rowData.setDetailDesc( value);
                  }else if("REWARD".equals(column)){
                    rowData.setReward( value);
                  }else if("AGENCY".equals(column)){
                    rowData.setAgency( value);
                  }else if("START_TIME".equals(column)){
                    rowData.setStartTime( value);
                  }else if("END_TIME".equals(column)){
                    rowData.setEndTime( value);
                  }else if("SECURITY_MODE".equals(column)){
                    rowData.setSecurityMode( value);
                  }else if("CREDIT_RATING".equals(column)){
                    rowData.setCreditRating( value);
                  }else if("AVATAR".equals(column)){
                    rowData.setAvatar( value);
                  }else if("URL".equals(column)){
                    rowData.setUrl( value);
                  }else if("CREATE_TIME".equals(column)){
                    rowData.setCreateTime( DateUtil.parse(value));
                  }
            } 
        } catch (IOException e) {
            logger.error("Cannot to excute readOne method by hbase  rowkey: "+rowKey, e);
        }  finally{
            try {
                if (table != null){
                    table.close();
                }
            } catch (IOException e) {
                logger.error("Cannot to excute readOne method by hbase  ", e);
            }
        }
        
        return null;
    }

    @Override
    public <Store> void writeBatch(Store... s) {
        HConnectionController hcc = (HConnectionController)s[0];
        List<Result> results  = (List<Result>)s[1];
        HTableInterface table = null;
        String columnfamily = Constants.hbase_column_family;
        try {
            table = hcc.getHTableInterface(Constants.hbase_table_name);
            List<Put> list=new ArrayList<Put>();
            for(Result r : results){
              Put p=new Put(Bytes.toBytes(r.getUrl()));
              p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("NAME"), Bytes.toBytes(r.getName()));
              p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("PLATFORM"), Bytes.toBytes(r.getPlatform()));
              if(!StringUtil.isNullOrBlank(r.getBorrower())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("BORROWER"), Bytes.toBytes(r.getBorrower()));
              }
              p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("YEAR_RATE"), Bytes.toBytes(r.getYearRate().toString()));
              if(r.getDayRate() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("DAY_RATE"), Bytes.toBytes(r.getDayRate().toString()));
              }
              if(!StringUtil.isNullOrBlank(r.getRepayLimitTime())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REPAY_LIMIT_TIME"), Bytes.toBytes(r.getRepayLimitTime()));
              }
              if (r.getProgress() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("PROGRESS"), Bytes.toBytes(r.getProgress().toString()));
              }
              if(!StringUtil.isNullOrBlank(r.getStatus())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("STATUS"), Bytes.toBytes(r.getStatus()));
              }
             if (!StringUtil.isNullOrBlank(r.getRemainTime())){
                 p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REMAIN_TIME"), Bytes.toBytes(r.getRemainTime()));
             }
              if (!StringUtil.isNullOrBlank(r.getRepayMode())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REPAY_MODE"), Bytes.toBytes(r.getRepayMode()));
              }
              if(r.getRepayPerMonth() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REPAY_PERMONTH"), Bytes.toBytes(r.getRepayPerMonth().toString()));
              }
              if(r.getTotalNum() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("TOTAL_NUM"), Bytes.toBytes(r.getTotalNum().toString()));
              }
             if(!StringUtil.isNullOrBlank(r.getCategory())){
                 p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("CATEGORY"), Bytes.toBytes(r.getCategory()));
              }
              if (r.getRemainMoney() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REMAIN_MONEY"), Bytes.toBytes(r.getRemainMoney().toString()));
              }
              if (!StringUtil.isNullOrBlank(r.getDetailDesc())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("DETAIL_DESC"), Bytes.toBytes(r.getDetailDesc()));
              }
              if (!StringUtil.isNullOrBlank(r.getReward())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("REWARD"), Bytes.toBytes(r.getReward()));
              }
              if (!StringUtil.isNullOrBlank(r.getAgency())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("AGENCY"), Bytes.toBytes(r.getAgency()));
              }
              if (!StringUtil.isNullOrBlank(r.getStartTime())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("START_TIME"), Bytes.toBytes(r.getStartTime()));
              }
             if (!StringUtil.isNullOrBlank(r.getEndTime())){
                 p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("END_TIME"), Bytes.toBytes(r.getEndTime()));
             }
              if (!StringUtil.isNullOrBlank(r.getSecurityMode())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("SECURITY_MODE"), Bytes.toBytes(r.getSecurityMode()));
              }
              if (!StringUtil.isNullOrBlank(r.getCreditRating())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("CREDIT_RATING"), Bytes.toBytes(r.getCreditRating()));
              }
              if(!StringUtil.isNullOrBlank(r.getAvatar())){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("AVATAR"), Bytes.toBytes(r.getAvatar()));
              }
              if (r.getCreateTime() != null){
                  p.add(Bytes.toBytes(columnfamily),Bytes.toBytes("CREATE_TIME"), Bytes.toBytes(DateUtil.dateToStr(r.getCreateTime())));
              }
              list.add(p);
            }
            table.put(list);
        } catch (IOException e) {
            logger.error("Cannot to excute writeBatch method by hbase ", e);
        }  finally{
            try {
                if (table != null){
                    table.close();
                }
            } catch (IOException e) {
                logger.error("Cannot to excute writeBatch method by hbase  ", e);
            }    
        }
    }

}
