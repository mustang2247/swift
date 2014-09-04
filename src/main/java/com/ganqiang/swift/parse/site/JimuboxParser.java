package com.ganqiang.swift.parse.site;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.FileUtil;

public class JimuboxParser implements Parsable {
	private static final Logger logger = Logger.getLogger(JimuboxParser.class);

	@SuppressWarnings("unchecked")
	public List<Result> parse(Event event) {
		Seed seed = (Seed) event.get(Event.seed_key);
		SiteType type = seed.getType();
		String instanceid = seed.getId();
		String key = seed.getKey();
		List<FetchedPage> fplist = (List<FetchedPage>) event
				.get(Event.fetchedPages_key);
		List<Result> results = new ArrayList<Result>();
		String path = Constants.inside_avatar_path_map.get(key);
		String homepage = Constants.seed_map.get(type).getHomePage();
		String logo = Constants.seed_map.get(type).getLogo();
		try {
			for (FetchedPage fetchedPage : fplist) {
				String url = fetchedPage.getUrl();
				logger.info("Worker [" + Thread.currentThread().getName()
						+ "] --- [JimuboxParser] begin parse from [" + url
						+ "].");

				Result result = new Result(type);
				result.setUrl(url);
				Document doc = Jsoup.parse(fetchedPage.getContent());

				// Elements up= doc.select(".project-summary");
				Elements down = doc.select("div[id=ProjectInfo]");

				// Element bottom = doc.getElementById("investments_area");
				// String uptext = up.text().toString();

				String downtext = down.text().toString();
				String temp = "";
				if (downtext.contains("融资方")) {
					temp = downtext.split("融资方")[1].trim();
				} else if (downtext.contains("融资企业代理人")) {
					temp = downtext.split("融资企业代理人")[1].trim();
				} else{
					temp = downtext;
				}

				String borrower = temp.split(" ")[0];
				result.setBorrower(borrower);

				String yr = temp.split("年化利率")[1].trim().split(" ")[0].trim();
				if (yr.contains("+")) {
					String[] yrs = yr.split("\\+");
					result.setYearRate(Double.valueOf(yrs[0]
							.replaceAll("%", "")));
					result.setReward(yrs[1]);
				} else {
					result.setYearRate(Double.valueOf(doc.select("span[class=data-tips] span[class=important]").text()));
				}

				if(temp.contains("资金用途")){
					temp = temp.split("资金用途")[1];
				}else{
					temp = temp.split("借款用途")[1];
				}
				
				String desc = temp.split("本期融资金额")[0].trim();
				result.setDetailDesc(desc);

				temp = temp.split("本期融资金额")[1];
				String money = temp.split("本次授信额度")[0].trim();
				if (money.contains("万")) {
					result.setMoney(Double.valueOf(money.replaceAll("万元", "")
							.trim()) * 10000);
				} else {
					result.setMoney(Double.valueOf(money));
				}
				
				temp = temp.split("投标截止时间")[1].trim();
				String lastdate = temp.split(" ")[0] + " " + temp.split(" ")[1];
				result.setEndTime(lastdate);

				String risk = doc.select("div[id=RiskControl]").text();
				if (risk.contains("担保方")) {
					String agency = risk.split("担保方 ")[1].split(" 所有担保项目 ")[0]
							.trim();
					result.setAgency(agency);
				}

				if (risk.contains(" 担保情况 ")) {
					String securityMode = risk.split(" 担保情况 ")[1]
							.split(" 反担保情况 ")[0].split("提供")[1].split("。")[0]
							.trim();
					result.setSecurityMode(securityMode);
				}

				result.setName(doc.select("div[class=project-title] h2").text());

				Elements estatus = doc.select("div[class=status-container] i");
				if (!estatus.isEmpty()) {
					String str = estatus.attr("class");
					if (str.equals("project-status-repayment")) {
						result.setStatus(Constants.status_hkz);
					} else if (str.equals("project-status-completed")) {
						result.setStatus(Constants.status_ymb);
					}
					result.setProgress(100d);
				} else {
					Elements p = doc
							.select("div[style*=position:absolute;width:50px;height:50px;top:0;left:0;text-align:center;line-height:50px]");
					if (!p.isEmpty()) {
						result.setProgress(Double.valueOf(p.text().replaceAll(
								"%", "")));
						result.setStatus(Constants.status_tbz);
						String remainMoney = doc
								.select("p[style=margin-bottom:0px;] span[class=important]")
								.text().replaceAll("\\,", "");
						result.setRemainMoney(Double.valueOf(remainMoney));
						String remainTime = doc
								.select("p[style=margin-bottom:0] small[class=muted]")
								.text().replaceAll("剩余时间", "").trim();
						result.setRemainTime(remainTime.replaceAll(" ", ""));
					}
				}

				String catagory = doc.select("span[class=tag]").text().trim()
						.replaceAll(" ", "");
				result.setCategory(catagory);

				String month = doc.select("span[class=data-tips]").get(1)
						.text().trim().replaceAll(" ", "");
				result.setRepayLimitTime(month);

				String filename = FileUtil.downloadAvatar(instanceid, type,
						path, "", logo);
				result.setAvatar(instanceid, filename);

				String html = HttpHelper.getContentFromUrl(homepage
						+ "Project/GetInvest?take=1000&skip=0&id="
						+ url.split("Index/")[1]);
				JSONArray json = new JSONArray(html);

				result.setTotalNum(json.length());

				Elements crele = doc
						.select("div[class=grade-card] dl[class=dl-horizontal]");
				if (!crele.isEmpty()) {
					String cr = crele.select("dd").get(0).text().trim();
					result.setCreditRating(cr.replaceAll(" ", ""));
				}
				// 积木盒子在您投资次日开始计息，积木盒子是按日计息、按月付息、到期还本的还本付息方式。
				result.setRepayMode("还本付息");

				results.add(result);

				logger.info("Worker [" + Thread.currentThread().getName()
						+ "] --- [JimuboxParser] end parse from [" + url + "].");
			}
		} catch (Exception e) {
			logger.error("Worker [" + Thread.currentThread().getName()
					+ "] --- [JimuboxParser] execute failure. ", e);
		}

		return results;
	}

	public static void main(String... args) throws UnsupportedEncodingException {

		String a = "10%+1%";
		System.out.println(a.split("\\+")[0]);

	}

}
