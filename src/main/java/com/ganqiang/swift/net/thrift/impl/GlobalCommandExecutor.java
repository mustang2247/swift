package com.ganqiang.swift.net.thrift.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.CommondExecutor;
import com.ganqiang.swift.net.thrift.GlobalCommand;

public class GlobalCommandExecutor implements CommondExecutor
{
  private static final Logger logger = Logger.getLogger(GlobalCommandExecutor.class);
  

  private final static String bin_sh = System.getProperty("user.dir") + "/bin/swift ";
  private final static String sh_restart = "restart-remote";
  private final static String sh_stop = "stop";
  
  private GlobalCommand command;
  
  @SuppressWarnings("unused")
  private GlobalCommandExecutor(){
    
  }
  
  public GlobalCommandExecutor(GlobalCommand command){
    this.command = command;
  }

  @Override
  public void execute()
  {
    switch(command){
      case STOP:
        exec(bin_sh + sh_stop);
        break;
      case RESTART:
        exec(bin_sh + sh_restart);
        break;
    }
  }

  public static void exec(String command)
  {
    boolean err = false;
    try {
      Process process = new ProcessBuilder(command.split(" ")).start();
      BufferedReader result = new BufferedReader(new InputStreamReader(
          process.getInputStream()));
      String s;
      while ((s = result.readLine()) != null) {
        logger.info(s);
      }
      BufferedReader errors = new BufferedReader(new InputStreamReader(
          process.getErrorStream()));
      while ((s = errors.readLine()) != null) {
        logger.error(s);
        err = true;
      }
    } catch (IOException e) {
      logger.error("Exception", e);
      if (!command.startsWith("CMD /C"))
        exec("CMD /C" + command);
      else
        throw new RuntimeException(e);
    }
    if (err) {
      try {
        throw new RuntimeException("Error executing");
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}
