package com.maijishop.crmeb.controller;

import com.maijishop.crmeb.common.utils.RedisUtil;
import com.maijishop.crmeb.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * @author maijishop
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello CRMEB Pro 3.2!");
    }
    
    /**
     * 测试Redis
     */
    @GetMapping("/redis")
    public Result<Map<String, Object>> testRedis() {
        // 测试字符串
        redisUtil.set("test:string", "CRMEB Pro 3.2");
        // 测试Hash
        redisUtil.hashSet("test:hash", "name", "CRMEB");
        redisUtil.hashSet("test:hash", "version", "3.2");
        // 测试List
        redisUtil.listSet("test:list", "CRMEB");
        redisUtil.listSet("test:list", "Pro");
        redisUtil.listSet("test:list", "3.2");
        
        Map<String, Object> result = new HashMap<>();
        result.put("string", redisUtil.get("test:string"));
        result.put("hash", redisUtil.hashGetAll("test:hash"));
        result.put("list", redisUtil.listGet("test:list", 0, -1));
        
        return Result.success("Redis测试成功", result);
    }
} 