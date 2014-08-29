package com.ganqiang.swift.storage;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.StringUtil;

public class Result {

  private static final Logger logger = Logger.getLogger(Result.class);

	private String id;//primary key
	private String platform;//平台名称
	private String site;//平台url
	private String url;//标的url地址
	private String name;//标名称
	private Double money;//金额
	private Double yearRate;//年利率
  private Double dayRate;//日利率,比如红岭创投	
	private String borrower;//借款人
	private Double progress;//进度，单位是%
	private String status;//状态,还款中/投标中/等待审核
	private String remainTime;//剩余时间
	private Double remainMoney;//还需(可投)金额
	private String repayMode;//还款方式
  private String repayLimitTime;//还款期限，单位月/天等	
	private Double repayPerMonth;//每月还款数
	private Integer totalNum;//投标人数
	private SiteType type;//P2P网站类型
	private String category;//标类型
	private String detailDesc;//借款详情
	private String reward;//奖励的利率，钱等
	private String agency;//担保机构
	private String startTime;//投标开始时间
	private String endTime;//投标结束时间
	private String securityMode;//保证方式
	private String creditRating;//信用等级
	private String avatar;//标的头像存放路径
	private Date createTime;
	private Date updateTime;

	public Result(){
	}

	public Result(SiteType type){
		String platform = Constants.seed_map.get(type).getPlatform();
		String site = Constants.seed_map.get(type).getHomePage();
		this.setType(type);
		this.setId(StringUtil.generateID());
		this.setSite(site);
  	this.setPlatform(platform);
  	this.setCreateTime(new Date());
	}

  public void setProgress(Double progress) {
    this.progress = progress;
    if(progress == 100d){
      setZeroRemainTime();
      setZeroRemainMoney();
    } else if (progress == 0d){
      if (this.money != null) {
        setRemainMoney(this.money);
      }
    } else {
      if (this.money != null) {
        setRemainMoney(CalculateUtil.getDoubleHalfValue(CalculateUtil.mul(this.money, (1 - progress / 100) )));
      }
    }
  }

  public void setProgress(Double money, Double remainMoney){
    Double progress = CalculateUtil.mul(100, CalculateUtil.sub(1, CalculateUtil.div(remainMoney, money, 4)));
    this.setProgress(progress);
  }
  
  public void setRemainMoney(Double remainMoney){
    this.remainMoney = remainMoney;
    if (remainMoney == 0d) {
      setProgress(100d);
    }
  }

  public void setAvatar(String instanceid, String avatar)
  {
    String domain = Constants.sync_domain_map.get(instanceid);
    if (StringUtil.isNullOrBlank(domain)) {
      this.avatar = avatar;
      return;
    }
    String path = Constants.disk_path_map.get(instanceid);    
    if (StringUtil.isNullOrBlank(path)) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- instance=["+instanceid+"] set sync domain failed.");
    }
    if (!path.substring(path.length()-1, path.length()).equals(File.separator)) {
      path = path + File.separator;
    }
    if (!domain.substring(domain.length()-1, domain.length()).equals(File.separator)) {
      domain = domain + File.separator;
    }
    avatar = avatar.replaceAll(path, domain);
    this.avatar = avatar;
  }

  public void setAvatar(String avatar)
  {
    this.avatar = avatar;
  }
  
  private void setZeroRemainTime() {
    this.remainTime = Constants.zero_remain_time;
  }
	
  private void setZeroRemainMoney() {
    this.remainMoney = Constants.zero_remain_money;
  }
  
	public boolean isRequireUpdate(Result result){
    boolean flag = false;
    if(null == result){
      return flag;
    }
//    if(!(this.getPlatform() == null ? result.getPlatform() == null : this.getPlatform().equals(result.getPlatform()))){
//      flag = true;
//    }
//    if(!(this.getName() == null ? result.getName() == null : this.getName().equals(result.getName()))){
//      flag = true;
//    }
//    if(!(this.getBorrower() == null ? result.getBorrower() == null : this.getBorrower().equals(result.getBorrower()))){
//      flag = true;
//    }
//    if(!(this.getSite() == null ? result.getSite() == null : this.getSite().equals(result.getSite()))){
//      flag = true;
//    }
//    if(!(this.getMoney()== null ? result.getMoney() == null : this.getMoney().equals(result.getMoney()))){
//      flag = true;
//    }
//    if(!(this.getYearRate()== null ? result.getYearRate() == null : this.getYearRate().equals(result.getYearRate()))){
//      flag = true;
//    }
//    if(!(this.getDayRate()== null ? result.getDayRate() == null : this.getDayRate().equals(result.getDayRate()))){
//      flag = true;
//    }
    if(!(this.getRepayLimitTime()== null ? result.getRepayLimitTime() == null : this.getRepayLimitTime().equals(result.getRepayLimitTime()))){
      flag = true;
    }
    if(!(this.getProgress()== null ? result.getProgress() == null : this.getProgress().equals(result.getProgress()))){
      flag = true;
    }
    if(!(this.getRemainTime()== null ? result.getRemainTime() == null : this.getRemainTime().equals(result.getRemainTime()))){
      flag = true;
    }
    if(!(this.getRepayMode()== null ? result.getRepayMode() == null : this.getRepayMode().equals(result.getRepayMode()))){
      flag = true;
    }
    if(!(this.getRepayPerMonth()== null ? result.getRepayPerMonth() == null : this.getRepayPerMonth().equals(result.getRepayPerMonth()))){
      flag = true;
    }
    if(!(this.getTotalNum()== null ? result.getTotalNum() == null : this.getTotalNum().equals(result.getTotalNum()))){
      flag = true;
    }
    if(!(this.getDetailDesc()== null ? result.getDetailDesc() == null : this.getDetailDesc().equals(result.getDetailDesc()))){
      flag = true;
    }
    if(!(this.getCategory()== null ? result.getCategory() == null : this.getCategory().equals(result.getCategory()))){
      flag = true;
    }
    if(!(this.getRemainMoney()== null ? result.getRemainMoney() == null : this.getRemainMoney().equals(result.getRemainMoney()))){
      flag = true;
    }
    if(!(this.getStatus()== null ? result.getStatus() == null : this.getStatus().equals(result.getStatus()))){
      flag = true;
    }
    return flag;
	}
	
	public boolean isRequireDayStatUpdate(Result result){
    boolean flag = false;
    if(null == result){
      return flag;
    }
    if(DateUtil.dateToStr(this.getCreateTime(),DateUtil.yyyyMMdd).equals(DateUtil.dateToStr(result.getCreateTime(),DateUtil.yyyyMMdd))){
      if(!(this.getRepayLimitTime()== null ? result.getRepayLimitTime() == null : this.getRepayLimitTime().equals(result.getRepayLimitTime()))){
        flag = true;
      }
      if(!(this.getProgress()== null ? result.getProgress() == null : this.getProgress().equals(result.getProgress()))){
        flag = true;
      }
      if(!(this.getRemainTime()== null ? result.getRemainTime() == null : this.getRemainTime().equals(result.getRemainTime()))){
        flag = true;
      }
      if(!(this.getRepayMode()== null ? result.getRepayMode() == null : this.getRepayMode().equals(result.getRepayMode()))){
        flag = true;
      }
      if(!(this.getRepayPerMonth()== null ? result.getRepayPerMonth() == null : this.getRepayPerMonth().equals(result.getRepayPerMonth()))){
        flag = true;
      }
      if(!(this.getTotalNum()== null ? result.getTotalNum() == null : this.getTotalNum().equals(result.getTotalNum()))){
        flag = true;
      }
      if(!(this.getDetailDesc()== null ? result.getDetailDesc() == null : this.getDetailDesc().equals(result.getDetailDesc()))){
        flag = true;
      }
      if(!(this.getCategory()== null ? result.getCategory() == null : this.getCategory().equals(result.getCategory()))){
        flag = true;
      }
      if(!(this.getRemainMoney()== null ? result.getRemainMoney() == null : this.getRemainMoney().equals(result.getRemainMoney()))){
        flag = true;
      }
      if(!(this.getStatus()== null ? result.getStatus() == null : this.getStatus().equals(result.getStatus()))){
        flag = true;
      }
    }
    return flag;
  }
	
	public boolean isRequireLuceneUpdate(Result result){
    boolean flag = false;
    if(null == result){
      return true;
    }
    if(!(this.getPlatform() == null ? result.getPlatform() == null : this.getPlatform().equals(result.getPlatform()))){
      flag = true;
    }
    if(!(this.getName() == null ? result.getName() == null : this.getName().equals(result.getName()))){
      flag = true;
    }
    if(!(this.getBorrower() == null ? result.getBorrower() == null : this.getBorrower().equals(result.getBorrower()))){
      flag = true;
    }
    if(!(this.getSite() == null ? result.getSite() == null : this.getSite().equals(result.getSite()))){
      flag = true;
    }
    if(!(this.getMoney()== null ? result.getMoney() == null : this.getMoney().equals(result.getMoney()))){
      flag = true;
    }
    if(!(this.getYearRate()== null ? result.getYearRate() == null : this.getYearRate().equals(result.getYearRate()))){
      flag = true;
    }
    if(!(this.getDayRate()== null ? result.getDayRate() == null : this.getDayRate().equals(result.getDayRate()))){
      flag = true;
    }
    if(!(this.getProgress()== null ? result.getProgress() == null : this.getProgress().equals(result.getProgress()))){
      flag = true;
    }
    if(!(this.getCategory()== null ? result.getCategory() == null : this.getCategory().equals(result.getCategory()))){
      flag = true;
    }
    if(!(this.getStatus()== null ? result.getStatus() == null : this.getStatus().equals(result.getStatus()))){
      flag = true;
    }
    return flag;
  }

	public String getAvatar()
  {
    return avatar;
  }

  public Double getRemainMoney()
  {
    return remainMoney;
  }

  public Double getDayRate()
  {
    return dayRate;
  }

  public void setDayRate(Double dayRate)
  {
    this.dayRate = dayRate;
  }

  public String getId()
  {
    return id;
  }

  public Date getCreateTime()
  {
    return createTime;
  }

  public void setCreateTime(Date createTime)
  {
    this.createTime = createTime;
  }

  public void setId(String id)
  {
    this.id = id;
  }
	public String getAgency()
  {
    return agency;
  }
  public void setAgency(String agency)
  {
    this.agency = agency;
  }
  public String getStartTime()
  {
    return startTime;
  }
  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }
  public String getEndTime()
  {
    return endTime;
  }
  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }
  public String getSecurityMode()
  {
    return securityMode;
  }
  public void setSecurityMode(String securityMode)
  {
    this.securityMode = securityMode;
  }
  public String getCreditRating()
  {
    return creditRating;
  }
  public void setCreditRating(String creditRating)
  {
    this.creditRating = creditRating;
  }

  public Double getRepayPerMonth()
  {
    return repayPerMonth;
  }

  public void setRepayPerMonth(Double repayPerMonth)
  {
    this.repayPerMonth = repayPerMonth;
  }
  public String getUrl()
  {
    return url;
  }
  public void setUrl(String url)
  {
    this.url = url;
  }
  public String getDetailDesc(){
    return detailDesc;
  }
  public void setDetailDesc(String detailDesc){
    this.detailDesc = detailDesc;
  }
  public Date getUpdateTime(){
    return updateTime;
  }
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
  public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRepayLimitTime()
  {
    return repayLimitTime;
  }
  public void setRepayLimitTime(String repayLimitTime)
  {
    this.repayLimitTime = repayLimitTime;
  }
  public String getBorrower() {
		return borrower;
	}
	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemainTime() {
		return remainTime;
	}
	public void setRemainTime(String remainTime) {
		this.remainTime = remainTime;
	}
	
	public String getRepayMode() {
		return repayMode;
	}
	public void setRepayMode(String repayMode) {
		this.repayMode = repayMode;
	}

	public SiteType getType()
  {
    return type;
  }

  public void setType(SiteType type)
  {
    this.type = type;
  }

  public Double getMoney()
  {
    return money;
  }

  public void setMoney(Double money)
  {
    this.money = money;
  }
  public String getReward()
  {
    return reward;
  }

  public void setReward(String reward)
  {
    this.reward = reward;
  }

  public Double getYearRate()
  {
    return yearRate;
  }

  public void setYearRate(Double yearRate)
  {
    this.yearRate = yearRate;
  }

  public Double getProgress()
  {
    return progress;
  }

	public Integer getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}
  public String getCategory()
  {
    return category;
  }
  public void setCategory(String category)
  {
    this.category = category;
  }
}
