//package cn.fcgyl.btc.websocket;
//
//import cn.fcgyl.btc.core.BaseDistributor;
//import cn.fcgyl.btc.core.DistributorService;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//public class DashboardWebsocket {
//
//    @Autowired
//    DistributorService distributorService;
//
//    @Autowired
//    private SimpMessagingTemplate simpMessagingTemplate;
//
//    public void syncPoolInfo(String distributorCode) {
//        BaseDistributor distributor = distributorService.findByCode(distributorCode);
//        WebsocketMessage msg = new WebsocketMessage();
//        JSONObject item = new JSONObject();
//        item.put("poolSize", distributor.getCurrentPoolSize());
//        item.put("maxPoolSize", distributor.getMaxPoolSize());
//        item.put("strategy", distributor.getStrategyCode());
//        item.put("cacheSize", distributor.getCachedSize());
//        item.put("totalGetSize", distributor.getTotalGetSize());
//        item.put("todayGetSize", distributor.getTodayGetSize());
//        item.put("poolGroupSize", distributor.getPoolGroupSize());
//        item.put("pause", distributor.getPauseStatus());
//        item.put("lockedGroups", distributor.getLockedGroup());
//        item.put("cacheLiveSeconds", distributor.getCacheLiveSeconds());
//        item.put("inPoolTime", distributor.getStatistics().getAvgInPoolTime());
//        item.put("outPoolTime", distributor.getStatistics().getAvgOutPoolTime());
//        item.put("processTime", distributor.getStatistics().getAvgProcessTime());
//        msg.setResult(item);
//        this.simpMessagingTemplate.convertAndSend("/topic/sync/poolInfo/" + distributorCode, JSON.toJSONString(msg));
//    }
//
//    public void syncConsumeRate(String distributorCode) {
//        BaseDistributor distributor = distributorService.findByCode(distributorCode);
//        WebsocketMessage msg = new WebsocketMessage();
//        JSONObject item = new JSONObject();
//        item.put("consumeRate", distributor.getConsumeList());
//        msg.setResult(item);
//        this.simpMessagingTemplate.convertAndSend("/topic/sync/consumeRate/" + distributorCode, JSON.toJSONString(msg));
//    }
//
//    @MessageMapping("/sync")
//    public void sync(WebsocketMessage message) {
//        syncPoolInfo(message.getDistributorCode());
//        syncConsumeRate(message.getDistributorCode());
//    }
//}