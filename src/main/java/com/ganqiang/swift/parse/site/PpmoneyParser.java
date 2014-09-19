package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public class PpmoneyParser implements Parsable {
	private static final Logger logger = Logger.getLogger(PpmoneyParser.class);
	private static final String jsonurl = "http://www.ppmoney.com/investment/records?page=0&projectId=";

	@SuppressWarnings("unchecked")
	public List<Result> parse(Event event) {
		Seed seed = (Seed) event.get(Event.seed_key);
		String instanceid = seed.getId();
		String key = seed.getKey();
		SiteType type = seed.getType();
		List<FetchedPage> fplist = (List<FetchedPage>) event
				.get(Event.fetchedPages_key);
		Map<String, Result> resultMap = (HashMap<String, Result>) event
				.get(Event.results_key);
		List<Result> results = new ArrayList<Result>();
		String path = Constants.inside_avatar_path_map.get(key);
		String logo = Constants.seed_map.get(type).getLogo();
		try {
			for (FetchedPage fetchedPage : fplist) {
				String url = fetchedPage.getUrl();
				logger.info("Worker [" + Thread.currentThread().getName()
						+ "] --- [PpmoneyParser] begin parse from [" + url
						+ "].");
				String[] array = url.split("/");
				String pageid = array[array.length - 1];
				Result result = resultMap.get(pageid);

				if (result == null) {
					continue;
				}

				result.setUrl(url);
				Document doc = Jsoup.parse(fetchedPage.getContent());

				String detail = doc.select("div[class=fullMsg]").text().trim();
				if (StringUtil.isNullOrBlank(detail)) {
					detail = doc.select("p[class=p0]").text();
				}
				result.setDetailDesc(detail);

				String investurl = jsonurl + pageid;
				String content = HttpHelper.getContentFromUrl(investurl);
				if (!content.contains("Not Found")) {
					JSONObject jo = new JSONObject(content);
					Object totalCount = jo.getJSONObject("Data").get(
							"TotalCount");
					if (totalCount != null) {
						result.setTotalNum(Integer.valueOf(totalCount
								.toString()));
					}
				}
				Elements title = doc.select("ul[class=cf] li");
				if (title != null) {
					String titlechildren = title.get(4).text();
					if (!titlechildren.contains("业务类型")) {
						titlechildren = title.get(5).text();
					}
					titlechildren = titlechildren.replaceAll("业务类型：", "");
					result.setCategory(titlechildren);
				}
				result.setName(doc.select("div[class=l-proj] h1").text());

				Elements up = doc.select("div[class*=l-proj-c] li");
				if (!up.isEmpty()) {
					Elements me = up.select("span[class=value]");
					String money = me.get(0).text().replaceAll(",", "");
					String danwei = up.get(0).select("span").get(2).text();
					if (danwei.contains("万元")) {
						money = money.replaceAll("万元", "");
						result.setMoney(CalculateUtil.mul(
								Double.valueOf(money), 10000d));
					} else {
						result.setMoney(Double.valueOf(money
								.replaceAll("元", "")));
					}
					String creditRating = up.get(1).select("span").get(1).text();
					result.setCreditRating(creditRating);

					String rate = up.get(2).select("span").get(1).text().replaceAll("%", "");
				 String ratedanwei =	up.get(2).select("span").get(2).text();
					if (ratedanwei.contains("年")) {
						result.setYearRate(Double.valueOf(rate));
					} else if (ratedanwei.contains("日")) {
						result.setDayRate(Double.valueOf(rate));
					}
					String repayLimitTime = up.get(3).text().replaceAll("投资期限: ", "");
					result.setRepayLimitTime(repayLimitTime.replaceAll("融资期限：",
							"").replaceAll("投资期限：", ""));
					String rm = up.get(6).text();
					Element repaymode = null;
					if(!rm.contains("偿还方式")){
						repaymode = up.get(9);
					}else{
						repaymode = up.get(6);
					}
					rm = repaymode.text().replaceAll("偿还方式：", "");
					result.setRepayMode(rm);
					
					String ag = up.get(4).text();
					Element agency = null;
					if(!ag.contains("专业保证")){
						agency = up.get(7);
					}else{
						agency = up.get(4);
					}
					result.setAgency(agency.select("span").get(1).text());
					
					String[] tt = up.get(8).select("span").get(1).text().trim().replaceAll(" ", "").split("至");
					result.setStartTime(tt[0]);
					result.setEndTime(tt[1]);
					
				}


				String filename = FileUtil.downloadAvatar(instanceid,
						SiteType.PPMONEY, path, "", logo);
				result.setAvatar(instanceid, filename);

				results.add(result);

				logger.info("Worker [" + Thread.currentThread().getName()
						+ "] --- [PpmoneyParser] end parse from [" + url + "].");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Worker [" + Thread.currentThread().getName()
					+ "] --- [PpmoneyParser] execute failure. ", e);
		}

		return results;
	}
	
}
