package com.maijishop.crmeb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.common.utils.Result;
import com.maijishop.crmeb.entity.Order;
import com.maijishop.crmeb.entity.OrderItem;
import com.maijishop.crmeb.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 *
 * @author maijishop
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Api(tags = "订单管理", description = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @ApiOperation("创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;
            Integer useIntegral = params.get("useIntegral") != null ? Integer.valueOf(params.get("useIntegral").toString()) : 0;
            
            @SuppressWarnings("unchecked")
            List<Long> cartIds = (List<Long>) params.get("cartIds");
            
            Long productId = params.get("productId") != null ? Long.valueOf(params.get("productId").toString()) : null;
            Long productSkuId = params.get("productSkuId") != null ? Long.valueOf(params.get("productSkuId").toString()) : null;
            Integer quantity = params.get("quantity") != null ? Integer.valueOf(params.get("quantity").toString()) : null;
            String remark = (String) params.get("remark");
            Integer orderType = params.get("orderType") != null ? Integer.valueOf(params.get("orderType").toString()) : 0;
            Integer from = params.get("from") != null ? Integer.valueOf(params.get("from").toString()) : 1;
            
            Map<String, Object> result = orderService.createOrder(userId, addressId, couponId, useIntegral, 
                                                               cartIds, productId, productSkuId, quantity, 
                                                               remark, orderType, from);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.fail("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单预创建信息
     */
    @PostMapping("/pre")
    @ApiOperation("获取订单预创建信息")
    public Result<Map<String, Object>> getPreOrderInfo(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;
            Integer useIntegral = params.get("useIntegral") != null ? Integer.valueOf(params.get("useIntegral").toString()) : 0;
            
            @SuppressWarnings("unchecked")
            List<Long> cartIds = (List<Long>) params.get("cartIds");
            
            Long productId = params.get("productId") != null ? Long.valueOf(params.get("productId").toString()) : null;
            Long productSkuId = params.get("productSkuId") != null ? Long.valueOf(params.get("productSkuId").toString()) : null;
            Integer quantity = params.get("quantity") != null ? Integer.valueOf(params.get("quantity").toString()) : null;
            Integer orderType = params.get("orderType") != null ? Integer.valueOf(params.get("orderType").toString()) : 0;
            
            Map<String, Object> result = orderService.getPreOrderInfo(userId, addressId, couponId, useIntegral,
                                                                   cartIds, productId, productSkuId, quantity, orderType);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单预创建信息失败", e);
            return Result.fail("获取订单预创建信息失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    @ApiOperation("支付订单")
    public Result<Map<String, Object>> payOrder(@RequestBody Map<String, Object> params) {
        try {
            String orderNo = (String) params.get("orderNo");
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer payType = Integer.valueOf(params.get("payType").toString());
            
            Map<String, Object> result = orderService.payOrder(orderNo, userId, payType);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return Result.fail("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "userId", value = "用户ID", required = false, dataType = "long", paramType = "query")
    })
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Map<String, Object> orderDetail = orderService.getOrderDetail(id, userId);
        if (orderDetail == null) {
            return Result.fail("订单不存在或无权查看");
        }
        return Result.success(orderDetail);
    }

    /**
     * 获取订单商品列表
     */
    @GetMapping("/items/{orderId}")
    @ApiOperation("获取订单商品列表")
    @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, dataType = "long", paramType = "path")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        return Result.success(orderItems);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/list")
    @ApiOperation("获取用户订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, dataType = "int", paramType = "query", defaultValue = "10")
    })
    public Result<IPage<Order>> getUserOrderList(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        IPage<Order> orderPage = orderService.getUserOrderList(userId, status, page, limit);
        return Result.success(orderPage);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Boolean> cancelOrder(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            String reason = (String) params.get("reason");
            
            boolean success = orderService.cancelOrder(id, userId, reason);
            
            return success ? Result.success(true) : Result.fail("取消订单失败");
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.fail("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm")
    @ApiOperation("确认收货")
    public Result<Boolean> confirmReceive(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            
            boolean success = orderService.confirmReceive(id, userId);
            
            return success ? Result.success(true) : Result.fail("确认收货失败");
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.fail("确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 删除订单
     */
    @PostMapping("/delete")
    @ApiOperation("删除订单")
    public Result<Boolean> deleteOrder(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            
            boolean success = orderService.deleteOrder(id, userId);
            
            return success ? Result.success(true) : Result.fail("删除订单失败");
        } catch (Exception e) {
            log.error("删除订单失败", e);
            return Result.fail("删除订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单物流信息
     */
    @GetMapping("/logistics/{id}")
    @ApiOperation("获取订单物流信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "userId", value = "用户ID", required = false, dataType = "long", paramType = "query")
    })
    public Result<Map<String, Object>> getOrderLogistics(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Map<String, Object> logistics = orderService.getOrderLogistics(id, userId);
        if (logistics == null) {
            return Result.fail("获取物流信息失败");
        }
        return Result.success(logistics);
    }

    /**
     * 后台-订单分页列表
     */
    @GetMapping("/page")
    @ApiOperation("后台-订单分页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, dataType = "int", paramType = "query", defaultValue = "10"),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "orderType", value = "订单类型", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "payStatus", value = "支付状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "shippingStatus", value = "发货状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "string", paramType = "query")
    })
    public Result<IPage<Order>> getOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) Integer payStatus,
            @RequestParam(required = false) Integer shippingStatus,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        
        Page<Order> pageParm = new Page<>(page, limit);
        IPage<Order> orderPage = orderService.getOrderPage(pageParm, orderNo, status, orderType, payStatus, 
                                                         shippingStatus, beginTime, endTime);
        
        return Result.success(orderPage);
    }

    /**
     * 后台-订单发货
     */
    @PostMapping("/ship")
    @ApiOperation("后台-订单发货")
    public Result<Boolean> ship(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String expressCode = (String) params.get("expressCode");
            String expressName = (String) params.get("expressName");
            String expressNo = (String) params.get("expressNo");
            
            boolean success = orderService.ship(id, expressCode, expressName, expressNo);
            
            return success ? Result.success(true) : Result.fail("发货失败");
        } catch (Exception e) {
            log.error("发货失败", e);
            return Result.fail("发货失败: " + e.getMessage());
        }
    }

    /**
     * 后台-订单统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("后台-订单统计数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginTime", value = "开始时间", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "string", paramType = "query")
    })
    public Result<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        
        Map<String, Object> statistics = orderService.getOrderStatistics(beginTime, endTime);
        
        return Result.success(statistics);
    }

    /**
     * 后台-商品销量排行
     */
    @GetMapping("/product/ranking")
    @ApiOperation("后台-商品销量排行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "limit", value = "数量限制", required = false, dataType = "int", paramType = "query", defaultValue = "10"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "string", paramType = "query")
    })
    public Result<List<Map<String, Object>>> getProductRanking(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        
        List<Map<String, Object>> ranking = orderService.getProductRanking(limit, beginTime, endTime);
        
        return Result.success(ranking);
    }
} 