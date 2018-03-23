package cn.fcgyl.btc.test;

import cn.fcgyl.btc.util.CacheService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheTest {

    @Autowired
    CacheService cacheService;

    @Test
    public void expireTest() throws InterruptedException {
        cacheService.createOrUpdate("test", 1, 1);
        Thread.sleep(2000);
        Assert.assertFalse(cacheService.exist("test"));
    }
}