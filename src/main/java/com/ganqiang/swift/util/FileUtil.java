package com.ganqiang.swift.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed.SiteType;

public class FileUtil
{
  private static final Logger logger = Logger.getLogger(FileUtil.class);

  /**
   * inside avatar path : disk_path/inside/site/avatar/
   */
  public static String getInsideAvatarPath(String diskpath, String site){
    if (StringUtil.isNullOrBlank(diskpath)) {
      return null;
    }
    if (diskpath.endsWith("/")) {
      diskpath = diskpath.substring(0, diskpath.length() - 1);
    }
    return diskpath + File.separator + "inside" + 
        File.separator + site + File.separator + "avatar" + File.separator;
  }

  /**
   * inside page path : disk_path/inside/site/html/
   */
  public static String getInsidePagePath(String diskpath, String site){
    if (StringUtil.isNullOrBlank(diskpath)) {
      return null;
    }
    if (diskpath.endsWith("/")) {
      diskpath = diskpath.substring(0, diskpath.length() - 1);
    }
    return diskpath + File.separator + "inside" + 
        File.separator + site + File.separator + "html" + File.separator;
  }

  /**
   * outside page path : disk_path/outside/site/
   */
  public static String getOutsidePagePath(String diskpath, String site){
    try {
      URI uri = new URI(site);
      site = uri.getHost();
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return diskpath + File.separator + "outside" + File.separator + site + File.separator;
  }

  /**
  * index path : disk_path/inside/site/index/
  */
  public static String getInsideIndexPath(String diskpath, String site){
    return diskpath + site + File.separator ;
  }

  public static void makeDir(String home) {
    File homedir = new File(home);
    if (!homedir.exists()) {
      try {
        homedir.mkdirs();
      } catch (Exception ex) {
        logger.info("Can not mkdir :" + home + " Maybe include special charactor!");
      }
    }
  }  

  private static String readFile(String filename) {
    char[] buffer = new char[4096];
    int len = 0;
    StringBuilder content = new StringBuilder(4096);

    InputStreamReader fr = null;
    BufferedReader br = null;
    try {
      fr = new InputStreamReader(new FileInputStream(filename), Charset.getUtf8Encoding());
      br = new BufferedReader(fr);
      while ((len = br.read(buffer)) > -1) {
        content.append(buffer, 0, len);
      }
    } catch (Exception e) {
      logger.error("read file "+filename+" error.", e);
    } finally {
        try {
          if (br != null)
            br.close();
          if (fr != null)
            fr.close();
        } catch (IOException e) {
          logger.error("read file "+filename+" error.", e);
        }
      
    }
    return content.toString();
  }
  
  public static void createFile(String filename, String content) {
    File file = new File(filename);
    boolean flag = file.exists();
    if (flag) {
      String source = readFile(filename);
      if (content.equals(source)) {
        return;
      }
    }
    OutputStreamWriter fw = null;
    PrintWriter out = null;
    try {
      fw = new OutputStreamWriter(new FileOutputStream(filename), Charset.getUtf8Encoding());
      out = new PrintWriter(fw);
      out.print(content);
    } catch (Exception ex) {
      logger.error("create file "+filename+" error.", ex);
    } finally {
      if (out != null)
        out.close();
      if (fw != null)
        try {
          fw.close();
        } catch (IOException e) {
          logger.error("create file "+filename+" error.", e);
        }
    }
  }

  public static void writeOutsidePage(String url, String content, String sourceurl, String path){
    URI uri = null;
    try {
      uri = new URI(sourceurl);
      sourceurl = uri.getHost();
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
    }
    String filename =  url.replaceAll(sourceurl + "/", "");
    if (StringUtil.isNullOrBlank(filename)) {
      return;
    }
    filename = filename.endsWith("/") ? filename.substring(0, filename.length() - 1) : filename;
    filename = path + filename.replaceAll("/", "_") ;
    if (filename.substring(filename.length() - 1, filename.length()).equals("_")) {
      filename = filename.substring(0, filename.length() - 1) ;
    } 
    if (!filename.contains(".html") && !filename.contains(".jsp") && !filename.contains(".asp") &&
        !filename.contains(".php") && !filename.contains(".xhtml") && !filename.contains(".aspx")) {
      filename += ".html";
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filename);
      fos.write(content.getBytes("UTF-8"));
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP download file error. ", e);
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        
      }
    }
  }

  public static boolean isExists(String filename) {
    File file = new File(filename);
    return file.exists();
  }

  public static void writePage(String instanceid, String filename, String content, String coding){
    if(!FileUtil.isExists(filename)){
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(filename);
        fos.write(content.getBytes(coding));
      } catch (Exception e) {
        logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP download file error. ", e);
      } finally {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
//      if (!StringUtil.isNullOrBlank(instanceid) && Constants.SYNC_MAP.get(instanceid)) {
//        PythonHelper.sync(Constants.DATA_PATH_MAP.get(instanceid), filename);
//      }
    }
  }

  public static void writeImage(String instanceid, String filename, byte[] content){
    if(!FileUtil.isExists(filename)){
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(filename);
        fos.write(content);
      } catch (Exception e) {
        logger.error("Worker [" + Thread.currentThread().getName() + "] --- HTTP download file error. ", e);
      } finally {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
//      if (!StringUtil.isNullOrBlank(instanceid)) {
//        PythonHelper.sync(Constants.DATA_PATH_MAP.get(instanceid), filename);
//      }
    }
  }

  /**
   *  首先判断网站的头像路径是否存在，不存在将共用网站的logo，并将其下载到指定路径下 <br>
   *  type         SiteType  <br>
   *  basePath     /opt/swift/website/avatar/  <br>
   *  pageid       详细页面url中的id值  <br>
   *  logoPath     某站点的logo的url绝对地址，e.g. http://www.website.com/logo.png <br>
   *  avatarUrl[]  某站点的多个avatar绝对路径，要求有顺序性 e.g. http://img.website.com/images/123.jpg等 <br>
   */
  public static String downloadAvatar(String instanceid, SiteType type, String basePath, String pageid, 
      String logoPath, String... avatarUrl){
    String filename = "";
    if (StringUtil.isNullOrBlank(logoPath)) {
      logoPath = Constants.seed_map.get(type).getLogo();
    }
    if (avatarUrl != null && avatarUrl.length > 0) {
      for(int i=0; i < avatarUrl.length; i++){
        String url = avatarUrl[i];
        if (StringUtil.isNullOrBlank(url)) {
          continue;
        }
        basePath += pageid;
        filename = basePath + FileUtil.getPicSuffixName(url);
        if(!FileUtil.isExists(filename)){
          HttpHelper.downloadImage(instanceid, url,false, filename);
          logger.info("Worker [" + Thread.currentThread().getName() + "] is downloading avatar: ["+filename+"].");
        }
        break;
      }
      if (StringUtil.isNullOrBlank(filename)) {
        filename = FileUtil.getLogoPath(basePath, logoPath);
        if(!FileUtil.isExists(filename)){
          HttpHelper.downloadImage(instanceid, logoPath , false,  filename);
          logger.info("Worker [" + Thread.currentThread().getName() + "] is downloading avatar: ["+filename+"].");
        }
      }
    } else {
      filename = FileUtil.getLogoPath(basePath, logoPath);
      if(!FileUtil.isExists(filename)){
        HttpHelper.downloadImage(instanceid, logoPath , false,  filename);
        logger.info("Worker [" + Thread.currentThread().getName() + "] is downloading avatar: ["+filename+"].");
      }
    }
    return filename;
  }
  
  /**
   * path  /opt/test/
   * logourl  http://www.site.com/logo-site.png
   * return /opt/test/logo.png
   */
  public static String getLogoPath(String path, String logourl){
    return path + "logo" + getPicSuffixName(logourl);
  }
   
  /**
   * return .gif/.png/.jpg
   */
  public static String getPicSuffixName(String url){
    return url.substring(url.lastIndexOf(".")).split("\\_")[0];
  }
}
