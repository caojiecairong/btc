package cn.fcgyl.btc.core;

import cn.fcgyl.common.utils.NumberUtil;
import cn.fcgyl.common.utils.RedisUtil;
import cn.fcgyl.zookeeper.config.ConfigService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xma
 */
@Service
public class ReportService {
    @Autowired
    private RedisUtil redis;

    @Autowired
    private ConfigService configService;

    private Double getKRWExchangeRate() {
        return Double.valueOf(configService.getConfigOrDefault("KRW", "1087.67"));
    }

    private static String[] icos = {"BTC", "DASH", "ETH", "EOS", "BCH", "ETC", "XRP", "QTUM", "LTC"};

    public String getZbBithumb() {
        JSONObject obj = new JSONObject();
        JSONObject zbSell = new JSONObject();
        obj.put("ZB_SELL", zbSell);
        for (String icoName : icos) {
            JSONObject object = new JSONObject();
            Double zbSell1 = getRedisInfo("zb", "sell1", icoName);
            Double bithumbBuy1 = getRedisInfo("bithumb", "buy1", icoName);
            object.put("ZB_SELL1", zbSell1);
            object.put("BITH_BUY1", bithumbBuy1);
            Double diff = NumberUtil.roundUpFormatDouble((bithumbBuy1 - zbSell1 * getKRWExchangeRate()) / bithumbBuy1 * 100, 2) * 100;
            object.put("DIFF", diff.intValue());
            object.put("CHANGE", 1);
            zbSell.put(icoName, object);
        }

        JSONObject zbBuy = new JSONObject();
        obj.put("ZB_BUY", zbBuy);
        for (String icoName : icos) {
            JSONObject object = new JSONObject();
            Double zbBuy1 = getRedisInfo("zb", "buy1", icoName);
            Double bithumbSell1 = getRedisInfo("bithumb", "sell1", icoName);
            object.put("ZB_BUY1", zbBuy1);
            object.put("BITH_SELL1", bithumbSell1);
            Double diff = NumberUtil.roundUpFormatDouble((bithumbSell1 - zbBuy1 * getKRWExchangeRate()) / bithumbSell1 * 100, 2) * 100;
            object.put("DIFF", diff.intValue());
            object.put("CHANGE", 1);
            zbBuy.put(icoName, object);
        }
        return obj.toJSONString();
    }

    private Double getRedisInfo(String exchange, String type, String icoName) {
        String key = "BTC_TRADE_" + exchange + "~" + type + "~" + icoName;
        return Double.valueOf(redis.get(key, 8));
    }
}
