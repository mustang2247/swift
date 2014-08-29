================================
==   SQL SCRIPT FOR MYSQL     ==
================================

CREATE DATABASE `p2p`
CHARACTER SET 'utf8'
COLLATE 'utf8_general_ci';

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `ID` varchar(50) NOT NULL PRIMARY KEY,
  `PLATFORM` varchar(20) NOT NULL COMMENT '平台名称',
  `SITE` varchar(500) COMMENT '平台站点地址',
  `URL` varchar(500) COMMENT '标的url地址',
  `NAME` varchar(500) COMMENT '标题',
  `BORROWER` varchar(50) COMMENT '借款人',
  `CATEGORY` varchar(50) COMMENT '标类型',
  `SECURITY_MODE` varchar(50) COMMENT '保障方式',
  `CREDIT_RATING` varchar(50) COMMENT '信用等级',
  `MONEY` double  COMMENT '金额,单位是元',
  `REMAIN_MONEY` double  COMMENT '剩余(可投)金额,单位是元',
  `YEAR_RATE` double  COMMENT '年利率，单位是%',
  `DAY_RATE` double  COMMENT '日利率，单位是%，比如红岭创投',
  `REWARD` varchar(50)  COMMENT '奖励的年利率，钱等',
  `AGENCY` varchar(50) COMMENT '担保机构',
  `PROGRESS` double  COMMENT '进度，单位是%，100%代表标已结束，还款中',
  `REMAIN_TIME` varchar(20)  COMMENT '剩余时间',
  `START_TIME` varchar(20)  COMMENT '投标开始时间',
  `END_TIME` varchar(20)  COMMENT '投标结束时间',
  `REPAY_MODE` varchar(50)  COMMENT '还款方式',
  `REPAY_LIMIT_TIME` varchar(20) COMMENT '还款期限，单位月/天等',
  `REPAY_PERMONTH` double  COMMENT '每月还款',
  `TOTAL_NUM` int(11)  COMMENT '投标人数',
  `STATUS` varchar(50) COMMENT '标的状态，还款中/投标中/等待审核',
  `AVATAR` varchar(1024)  COMMENT '标的头像存放路径',
  `DETAIL_DESC` text  COMMENT '标的详细描述',
  `CREATE_TIME` datetime COMMENT '创建日期',
  `UPDATE_TIME` datetime COMMENT '更新日期'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `day_project`;
CREATE TABLE `day_project` (
  `ID` varchar(50) NOT NULL PRIMARY KEY,
  `PLATFORM` varchar(20) NOT NULL COMMENT '平台名称',
  `SITE` varchar(500) COMMENT '平台站点地址',
  `URL` varchar(500) COMMENT '标的url地址',
  `NAME` varchar(500) COMMENT '标题',
  `BORROWER` varchar(50) COMMENT '借款人',
  `CATEGORY` varchar(50) COMMENT '标类型',
  `SECURITY_MODE` varchar(50) COMMENT '保障方式',
  `CREDIT_RATING` varchar(50) COMMENT '信用等级',
  `MONEY` double  COMMENT '金额,单位是元',
  `REMAIN_MONEY` double  COMMENT '剩余(可投)金额,单位是元',
  `YEAR_RATE` double  COMMENT '年利率，单位是%',
  `DAY_RATE` double  COMMENT '日利率，单位是%，比如红岭创投',
  `REWARD` varchar(50)  COMMENT '奖励的年利率，钱等',
  `AGENCY` varchar(50) COMMENT '担保机构',
  `PROGRESS` double  COMMENT '进度，单位是%，100%代表标已结束，还款中',
  `REMAIN_TIME` varchar(20)  COMMENT '剩余时间',
  `START_TIME` varchar(20)  COMMENT '投标开始时间',
  `END_TIME` varchar(20)  COMMENT '投标结束时间',
  `REPAY_MODE` varchar(50)  COMMENT '还款方式',
  `REPAY_LIMIT_TIME` varchar(20) COMMENT '还款期限，单位月/天等',
  `REPAY_PERMONTH` double  COMMENT '每月还款',
  `TOTAL_NUM` int(11)  COMMENT '投标人数',
  `STATUS` varchar(50) COMMENT '标的状态，还款中/投标中/等待审核',
  `AVATAR` varchar(1024)  COMMENT '标的头像存放路径',
  `DETAIL_DESC` text  COMMENT '标的详细描述',
  `CREATE_TIME` datetime COMMENT '创建日期',
  `UPDATE_TIME` datetime COMMENT '更新日期'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `day_site_stat`;
CREATE TABLE `day_site_stat` (
  `ID` varchar(50) NOT NULL PRIMARY KEY,
  `PLATFORM` varchar(20) NOT NULL COMMENT '平台名称',
  `CURRENT_YEAR` int(11)  COMMENT '统计时间：年',
  `MONTH_OF_YEAR` int(11)  COMMENT '统计时间：月份',
  `WEEK_OF_YEAR` int(11)  COMMENT '统计时间：周',
  `STAT_TIME` varchar(20) NOT NULL COMMENT '统计某日期的数据，以天为单位',
  `INC_PROJECT` int(11)  COMMENT '每天新标的数',
  `INC_MONEY` double  COMMENT '每天增量金额,单位是元'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
