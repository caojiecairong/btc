package cn.fcgyl.btc.controller;

import cn.fcgyl.btc.core.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author xma
 */
@Controller
public class HomeController {
    @Autowired
    ReportService reportService;

    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("")
    public String home() {
        return "/app/pages/home.html";
    }

    @RequestMapping("/status")
    @ResponseBody
    public String status() {
        return "200";
    }

    @RequestMapping("/getZbBithumb")
    @ResponseBody
    public String getZbBithumb() {
        return reportService.getZbBithumb();
    }
}