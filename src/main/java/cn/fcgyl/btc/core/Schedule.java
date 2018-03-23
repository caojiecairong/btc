package cn.fcgyl.btc.core;

import cn.fcgyl.common.utils.HTTPUtil;
import cn.fcgyl.common.utils.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author xma
 */
@Component
public class Schedule {
    private static final int SCHEDULE_INTERVAL = 1000 * 6;

    @Autowired
    RedisUtil redis;

    @Scheduled(fixedDelay = SCHEDULE_INTERVAL)
    public void updateBithumb() throws IOException {
        String[] icos = {"BTC", "DASH", "ETH", "EOS", "BCH", "ETC", "XRP", "QTUM", "LTC"};
        String exchange = "bithumb";
        JSONObject res = JSON.parseObject(HTTPUtil.sendGet("https://api.bithumb.com/public/orderbook/ALL/", "BITHUMB", "请求交易信息"));
        if ("0000".equals(res.getString("status"))) {
            for (String icoName : icos) {
                // bid == buy
                JSONArray array = res.getJSONObject("data").getJSONObject(icoName).getJSONArray("bids");
                setRedisInfo(exchange, "buy1", icoName, ((JSONObject) array.get(0)).getDouble("price"));
                setRedisInfo(exchange, "buy5", icoName, ((JSONObject) array.get(4)).getDouble("price"));

                // asks == sells
                array = res.getJSONObject("data").getJSONObject(icoName).getJSONArray("asks");
                setRedisInfo(exchange, "sell1", icoName, ((JSONObject) array.get(0)).getDouble("price"));
                setRedisInfo(exchange, "sell5", icoName, ((JSONObject) array.get(4)).getDouble("price"));
            }
        } else {
            System.out.println("失败了");
        }
    }

    @Scheduled(fixedDelay = SCHEDULE_INTERVAL)
    public void updateZB() throws IOException {
        String[] icos = {"BTC_usdt", "DASH_usdt", "ETH_usdt", "EOS_usdt", "BCC_usdt", "ETC_usdt", "XRP_usdt", "QTUM_usdt", "LTC_usdt"};
        String exchange = "zb";
        for (String icoName : icos) {
            JSONObject res = JSON.parseObject(HTTPUtil.sendGet("http://api.zb.com/data/v1/ticker?market=" + icoName.toLowerCase(), "zb", "请求交易信息"));
            System.out.println(res.getJSONObject("ticker").getString("sell"));
            setRedisInfo(exchange, "sell1", icoName.replace("_usdt", ""), res.getJSONObject("ticker").getDouble("sell"));
            setRedisInfo(exchange, "buy1", icoName.replace("_usdt", ""), res.getJSONObject("ticker").getDouble("buy"));
//                // bid == buy
//                JSONArray array = res.getJSONObject("data").getJSONObject(icoName).getJSONArray("bids");
//                setRedisInfo(exchange, "buy1", icoName, ((JSONObject) array.get(0)).getDouble("price"));
//                setRedisInfo(exchange, "buy5", icoName, ((JSONObject) array.get(4)).getDouble("price"));
//
//                // asks == sells
//                array = res.getJSONObject("data").getJSONObject(icoName).getJSONArray("asks");
//                setRedisInfo(exchange, "sell1", icoName, ((JSONObject) array.get(0)).getDouble("price"));
//                setRedisInfo(exchange, "sell5", icoName, ((JSONObject) array.get(4)).getDouble("price"));
        }
    }

    private void setRedisInfo(String exchange, String type, String icoName, Double value) {
        String key = "BTC_TRADE_" + exchange + "~" + type + "~" + icoName.replace("BCC", "BCH");
        redis.set(key, value.toString(), 8);
    }
}
