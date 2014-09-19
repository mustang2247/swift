package com.ganqiang.swift.storage.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.StringUtil;

public class SqlBuilder {

	private static final Logger logger = Logger.getLogger(SqlBuilder.class);
	private List<String> insertSqls = new ArrayList<String>();
	private List<String> updateSqls = new ArrayList<String>();
	private List<String> insertDaySqls = new ArrayList<String>();
	private List<String> updateDaySqls = new ArrayList<String>();
	
	private String insertsql = "insert into project (ID,PLATFORM,URL,SITE,NAME,BORROWER,MONEY,YEAR_RATE,DAY_RATE,REPAY_LIMIT_TIME,"
			+ "PROGRESS,REMAIN_TIME,REPAY_MODE,REPAY_PERMONTH,TOTAL_NUM,CATEGORY,REMAIN_MONEY,DETAIL_DESC,"
			+ "REWARD,AGENCY,START_TIME,END_TIME,SECURITY_MODE,CREDIT_RATING,STATUS,AVATAR,CREATE_TIME) values ( ";

	private String insertdaysql = "insert into day_project (ID,PLATFORM,URL,SITE,NAME,BORROWER,MONEY,YEAR_RATE,DAY_RATE,REPAY_LIMIT_TIME,"
			+ "PROGRESS,REMAIN_TIME,REPAY_MODE,REPAY_PERMONTH,TOTAL_NUM,CATEGORY,REMAIN_MONEY,DETAIL_DESC,"
			+ "REWARD,AGENCY,START_TIME,END_TIME,SECURITY_MODE,CREDIT_RATING,STATUS,AVATAR,CREATE_TIME) values ( ";

	private String updatesql = "update project set ";

	private String updatedaysql = "update day_project set ";

	public void buildSqls(DBStore dbstore, List<Result> list, SiteType type,
			String instanceid) {
		String thriftServer = "";
		List<Result> results = new ArrayList<Result>();
		if (!StringUtil.isNullOrBlank(Constants.remote_ts)) {
			thriftServer = Constants.remote_ts;
		} else if (!Constants.local_ts_map.isEmpty()) {
			thriftServer = Constants.local_ts_map.get(instanceid);
		}

		String platform = Constants.seed_map.get(type).getPlatform();
		if (list == null || list.size() == 0) {
			return;
		}
		for (Result result : list) {
			// source
			Result dbresult = dbstore
					.readOne("select * from project where PLATFORM='"
							+ platform + "' " + "and MONEY='"
							+ result.getMoney() + "' and NAME='"
							+ result.getName() + "' " +
							// "and BORROWER='"+result.getBorrower() + "'" +
							"and URL='" + result.getUrl() + "'");
			// "and YEAR_RATE='"+result.getYearRate()+"'"); //year_rate可能为空

			if (dbresult == null) {
				insertSqls.add(insertsql + buildInsertSql(result));
				results.add(result);
			} else if (result.isRequireUpdate(dbresult)) {
				updateSqls.add(updatesql
						+ buildUpdateSql(result, dbresult.getId()));
				results.add(result);
			}

			// stat
			Result dbdayresult = dbstore
					.readOne("select * from day_project where PLATFORM='"
							+ platform + "' " + "and MONEY='"
							+ result.getMoney() + "' and NAME='"
							+ result.getName() + "' " + "and URL='"
							+ result.getUrl() + "' and CREATE_TIME like '"
							+ DateUtil.getCurrentDay() + "%'");
			if (dbdayresult == null) {
				insertDaySqls.add(insertdaysql + buildInsertSql(result));
			} else if (result.isRequireDayStatUpdate(dbdayresult)) {
				updateDaySqls.add(updatedaysql
						+ buildUpdateSql(result, dbdayresult.getId()));
			}
		}

		if (!StringUtil.isNullOrBlank(thriftServer)) {
			sendIndexDate(results, thriftServer);
		}
	}

	private void sendIndexDate(List<Result> list, String thriftServer) {
		JSONArray jsonArray = new JSONArray();
		for (Result result : list) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", result.getId());
			jsonObject.put("platform", result.getPlatform());
			jsonObject.put("site", result.getSite());
			jsonObject.put("url", result.getUrl());
			jsonObject.put("name", result.getName());
			jsonObject.put("borrower", result.getBorrower());
			jsonObject.put("category", result.getCategory());
			jsonObject.put("security_mode", result.getSecurityMode());
			jsonObject.put("credit_rating", result.getCreditRating());
			jsonObject.put("money", result.getMoney());
			jsonObject.put("remain_money", result.getRemainMoney());
			jsonObject.put("year_rate", result.getYearRate());
			jsonObject.put("day_rate", result.getDayRate());
			jsonObject.put("reward", result.getReward());
			jsonObject.put("agency", result.getAgency());
			jsonObject.put("progress", result.getProgress());
			jsonObject.put("remain_time", result.getRemainTime());
			jsonObject.put("start_time", result.getStartTime());
			jsonObject.put("end_time", result.getEndTime());
			jsonObject.put("repay_mode", result.getRepayMode());
			jsonObject.put("repay_limit_time", result.getRepayLimitTime());
			jsonObject.put("repay_permonth", result.getRepayPerMonth());
			jsonObject.put("total_num", result.getTotalNum());
			jsonObject.put("status", result.getStatus());
			jsonObject.put("avatar", result.getAvatar());
			jsonObject.put("detail_desc", result.getDetailDesc());
			jsonObject.put("create_time",
					DateUtil.dateToStr(result.getCreateTime()));
			jsonObject.put(
					"update_time",
					result.getUpdateTime() != null ? DateUtil.dateToStr(result
							.getUpdateTime()) : null);
			jsonArray.put(jsonObject);
		}

		String ip = thriftServer.split("\\:")[0];
		String port = thriftServer.split("\\:")[1];
		TTransport transport = new TSocket(ip, Integer.valueOf(port));
		TProtocol protocol = new TBinaryProtocol(transport);
		ThriftService.Client client = new ThriftService.Client(protocol);
		try {
			transport.open();
			client.addOrUpdP2pIndex(jsonArray.toString());
			logger.info("swift is sending index json data...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			transport.close();
		}
	}

	private String buildInsertSql(Result result) {
		String sql = " '" + StringUtil.generateID(result.getUrl()) + "','"
				+ result.getPlatform() + "','" + result.getUrl() + "','"
				+ result.getSite() + "','" + result.getName() + "',";
		if (result.getBorrower() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getBorrower() + "',";
		}
		sql += " '" + result.getMoney() + "',";
		if (result.getYearRate() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getYearRate() + "',";
		}
		if (result.getDayRate() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getDayRate() + "',";
		}
		if (result.getRepayLimitTime() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getRepayLimitTime() + "',";
		}
		if (result.getProgress() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getProgress() + "',";
		}
		if (result.getRemainTime() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getRemainTime() + "',";
		}
		if (result.getRepayMode() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getRepayMode() + "',";
		}
		if (result.getRepayPerMonth() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getRepayPerMonth() + "',";
		}
		if (result.getTotalNum() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getTotalNum() + "',";
		}
		if (result.getCategory() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getCategory() + "',";
		}
		if (result.getRemainMoney() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getRemainMoney() + "',";
		}
		if (result.getDetailDesc() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getDetailDesc() + "',";
		}
		if (result.getReward() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getReward() + "',";
		}
		if (result.getAgency() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getAgency() + "',";
		}
		if (result.getStartTime() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getStartTime() + "',";
		}
		if (result.getEndTime() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getEndTime() + "',";
		}
		if (result.getSecurityMode() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getSecurityMode() + "',";
		}
		if (result.getCreditRating() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getCreditRating() + "',";
		}
		if (result.getStatus() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getStatus() + "',";
		}
		if (result.getAvatar() == null) {
			sql += "" + null + ",";
		} else {
			sql += "'" + result.getAvatar() + "',";
		}
		sql += "'" + DateUtil.dateToStr(result.getCreateTime()) + "'  )";
		return sql;
	}

	private String buildUpdateSql(Result result, String id) {
		String sql = "BORROWER='" + result.getBorrower()
				+ "',REPAY_LIMIT_TIME='" + result.getRepayLimitTime() + "',";
		if (result.getProgress() == null) {
			sql += "PROGRESS=" + null + ",";
		} else {
			sql += "PROGRESS='" + result.getProgress() + "',";
		}
		if (result.getRepayPerMonth() == null) {
			sql += "REPAY_PERMONTH=" + null + ",";
		} else {
			sql += "REPAY_PERMONTH='" + result.getRepayPerMonth() + "',";
		}
		if (result.getTotalNum() == null) {
			sql += "TOTAL_NUM=" + null + ",";
		} else {
			sql += "TOTAL_NUM='" + result.getTotalNum() + "',";
		}
		if (result.getRemainTime() == null) {
			sql += "REMAIN_TIME=" + null + ",";
		} else {
			sql += "REMAIN_TIME='" + result.getRemainTime() + "',";
		}
		if (result.getRepayMode() == null) {
			sql += "REPAY_MODE=" + null + ",";
		} else {
			sql += "REPAY_MODE='" + result.getRepayMode() + "',";
		}
		if (result.getCategory() == null) {
			sql += "CATEGORY=" + null + ",";
		} else {
			sql += "CATEGORY='" + result.getCategory() + "',";
		}
		if (result.getRemainMoney() == null) {
			sql += "REMAIN_MONEY=" + null + ",";
		} else {
			sql += "REMAIN_MONEY='" + result.getRemainMoney() + "',";
		}
		if (result.getDetailDesc() == null) {
			sql += "DETAIL_DESC=" + null + ",";
		} else {
			sql += "DETAIL_DESC='" + result.getDetailDesc() + "',";
		}
		if (result.getReward() == null) {
			sql += "REWARD=" + null + ",";
		} else {
			sql += "REWARD='" + result.getReward() + "',";
		}
		if (result.getAgency() == null) {
			sql += "AGENCY=" + null + ",";
		} else {
			sql += "AGENCY='" + result.getAgency() + "',";
		}
		if (result.getStartTime() == null) {
			sql += "START_TIME=" + null + ",";
		} else {
			sql += "START_TIME='" + result.getStartTime() + "',";
		}
		if (result.getEndTime() == null) {
			sql += "END_TIME=" + null + ",";
		} else {
			sql += "END_TIME='" + result.getEndTime() + "',";
		}
		if (result.getSecurityMode() == null) {
			sql += "SECURITY_MODE=" + null + ",";
		} else {
			sql += "SECURITY_MODE='" + result.getSecurityMode() + "',";
		}
		if (result.getCreditRating() == null) {
			sql += "CREDIT_RATING=" + null + ",";
		} else {
			sql += "CREDIT_RATING='" + result.getCreditRating() + "',";
		}
		if (result.getStatus() == null) {
			sql += "STATUS=" + null + ",";
		} else {
			sql += "STATUS='" + result.getStatus() + "',";
		}
		sql += "UPDATE_TIME='" + DateUtil.dateToStr(new Date())
				+ "' where id='" + id + "' ";
		return sql;
	}

	public List<String> getInsertSqls() {
		return insertSqls;
	}

	public void setInsertSqls(List<String> insertSqls) {
		this.insertSqls = insertSqls;
	}

	public List<String> getUpdateSqls() {
		return updateSqls;
	}

	public void setUpdateSqls(List<String> updateSqls) {
		this.updateSqls = updateSqls;
	}

	public List<String> getInsertDaySqls() {
		return insertDaySqls;
	}

	public void setInsertDaySqls(List<String> insertDaySqls) {
		this.insertDaySqls = insertDaySqls;
	}

	public static void main(String... args) {
		String str = "1.1.2.2:123";
		System.out.println(str.split("\\.")[0]);
	}

}
