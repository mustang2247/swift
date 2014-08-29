#!/usr/bin/env python

from logging.handlers import TimedRotatingFileHandler
import logging
import os
import command

class BootStrap:

    def __init__(self):
        self.__init_log()
        self.logger = logging.getLogger("BootStrap")
        self.logger.info("start python script......")

    def __init_log(self):
        ld = command.swift_path + "/log/"
        if not os.path.exists(ld):
            os.mkdir(ld)
        root = logging.getLogger()
        logHandler = TimedRotatingFileHandler(ld + "swift-sync.log", when="midnight")
        logHandler.setFormatter(logging.Formatter('%(asctime)s  %(levelname)s [%(name)s] - <%(message)s>'))
        root.addHandler(logHandler)
        root.setLevel(logging.NOTSET)

    def load(self, conf, local_root_data_path, local_sub_data_path):
        if conf["selector"] == "rsync":
            r = command.Rsync(conf)
            r.send(local_root_data_path, local_sub_data_path)
            self.logger.info("loading python script finish......")
        elif conf["selector"] == "scp":
            s = command.Scp(conf)
            s.send(local_root_data_path, local_sub_data_path)
            self.logger.info("loading python script finish......")
        elif conf["selector"] == "ftp":
            f = command.Ftp(conf)
            f.send(local_root_data_path, local_sub_data_path)
            self.logger.info("loading python script finish......")
        else:
            self.logger.error("configure conf is null.")

c = BootStrap()
def main(conf, local_root_data_path, local_sub_data_path):
    c.load(conf, local_root_data_path.rstrip("/"), local_sub_data_path)

