namespace java com.ganqiang.swift.net.thrift

struct Job {
  1: required string id,
  2: optional i64 interval,
  3: optional string startTime,
  4: optional list<string> httpProxys,
  5: optional bool inUseProxy = false,
  6: optional bool outUseProxy = false,
  7: optional list<string> inSeeds,
  8: optional list<string> outSeeds,
  9: optional bool inIsdownload = false,
  10: optional bool outIsdownload = true,
  11: optional bool jsSupport = false,
  12: optional bool isCascade = false,
  13: optional i32 depth = 1,
  14: optional i64 inDelay = 0,
  15: optional i64 outDelay = 0
}

struct GlobalConfig {
  1: optional i32 port,
  2: optional string server,
  3: optional string protocal,
  4: optional i32 threadNum,
  5: optional list<string> httpProxys,
  6: optional string disk,
  7: optional bool isSync = true,
  8: optional string syncDomain,
  9: optional string index,
  10: optional string dbDriver,
  11: optional string dbUrl,
  12: optional string dbUsername,
  13: optional string dbPassword,
  14: optional i32 dbPoolSize = 10,
  15: optional i32 seqId,
  16: optional string address,
  17: optional i32 totalNodes,
   18: optional string hbaseMaster,
   19: optional string zkQuorum,
   20: optional i32 zkClientPort
}

enum JobCommand {
    PAUSE,
    CONTINUE,
    CANCEL
}

enum GlobalCommand {
    STOP,
    RESTART
}

struct JobResponse {
  1: required string jobid,
  2: required string status,
  3: required string message
}

struct GlobalResponse {
  1: required string status,
  2: required string message
}

struct PingResponse {
  1: required string cpurate,
  2: required string memrate,
  3: required string os
}


service SwiftController {
  JobResponse allot(1: Job job)
  JobResponse sendJobCommand(1: string jobid, 2: JobCommand jobcommand)
  GlobalConfig view()
  GlobalResponse update(1: GlobalConfig globalconfig)
  void sendGlobalCommand(1: GlobalCommand globalcommand)
  PingResponse ping()
}