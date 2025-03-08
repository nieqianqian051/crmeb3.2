package com.maijishop.crmeb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.common.utils.Result;
import com.maijishop.crmeb.entity.OrderRefund;
import com.maijishop.crmeb.service.OrderRefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单退款控制器
 *
 * @author maijishop
 */
@Slf4j
@RestController
@RequestMapping("/order/refund")
@Api(tags = "订单退款", description = "订单退款相关接口")
public class OrderRefundController {

    @Autowired
    private OrderRefundService orderRefundService;

    /**
     * 申请退款
     */
    @PostMapping("/apply")
    @ApiOperation("申请退款")
    public Result<Boolean> applyRefund(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String orderNo = (String) params.get("orderNo");
            Long userId = Long.valueOf(params.get("userId").toString());
            BigDecimal refundAmount = new BigDecimal(params.get("refundAmount").toString());
            String refundReason = (String) params.get("refundReason");
            String refundExplain = (String) params.get("refundExplain");
            String refundProof = (String) params.get("refundProof");
            Integer refundType = Integer.valueOf(params.get("refundType").toString());
            
            boolean result = orderRefundService.applyRefund(orderId, orderNo, userId, refundAmount, 
                                                         refundReason, refundExplain, refundProof, refundType);
            
            return result ? Result.success(true) : Result.fail("申请退款失败");
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.fail("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 取消退款申请
     */
    @PostMapping("/cancel")
    @ApiOperation("取消退款申请")
    public Result<Boolean> cancelRefund(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            
            boolean result = orderRefundService.cancelRefund(id, userId);
            
            return result ? Result.success(true) : Result.fail("取消退款申请失败");
        } catch (Exception e) {
            log.error("取消退款申请失败", e);
            return Result.fail("取消退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取退款详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取退款详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "退款ID", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "userId", value = "用户ID", required = false, dataType = "long", paramType = "query")
    })
    public Result<OrderRefund> getRefundDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        OrderRefund refund = orderRefundService.getRefundDetail(id, userId);
        if (refund == null) {
            return Result.fail("退款申请不存在或无权查看");
        }
        return Result.success(refund);
    }

    /**
     * 获取订单退款列表
     */
    @GetMapping("/order/{orderId}")
    @ApiOperation("获取订单退款列表")
    @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, dataType = "long", paramType = "path")
    public Result<List<OrderRefund>> getOrderRefundList(@PathVariable Long orderId) {
        List<OrderRefund> refundList = orderRefundService.getOrderRefundList(orderId);
        return Result.success(refundList);
    }

    /**
     * 获取用户退款列表
     */
    @GetMapping("/user/list")
    @ApiOperation("获取用户退款列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, dataType = "int", paramType = "query", defaultValue = "10")
    })
    public Result<IPage<OrderRefund>> getUserRefundPage(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        IPage<OrderRefund> refundPage = orderRefundService.getUserRefundPage(userId, page, limit);
        return Result.success(refundPage);
    }

    /**
     * 后台-退款申请分页列表
     */
    @GetMapping("/page")
    @ApiOperation("后台-退款申请分页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, dataType = "int", paramType = "query", defaultValue = "10"),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "refundNo", value = "退款单号", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "refundStatus", value = "退款状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "refundType", value = "退款类型", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "string", paramType = "query")
    })
    public Result<IPage<OrderRefund>> getRefundPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        
        Page<OrderRefund> pageParm = new Page<>(page, limit);
        IPage<OrderRefund> refundPage = orderRefundService.getRefundPage(pageParm, orderNo, refundNo, 
                                                                       refundStatus, refundType, beginTime, endTime);
        
        return Result.success(refundPage);
    }

    /**
     * 后台-处理退款申请
     */
    @PostMapping("/handle")
    @ApiOperation("后台-处理退款申请")
    public Result<Boolean> handleRefund(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer refundStatus = Integer.valueOf(params.get("refundStatus").toString());
            String rejectReason = (String) params.get("rejectReason");
            String handlePerson = (String) params.get("handlePerson");
            String handleRemark = (String) params.get("handleRemark");
            
            boolean result = orderRefundService.handleRefund(id, refundStatus, rejectReason, handlePerson, handleRemark);
            
            return result ? Result.success(true) : Result.fail("处理退款申请失败");
        } catch (Exception e) {
            log.error("处理退款申请失败", e);
            return Result.fail("处理退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 后台-完成退款
     */
    @PostMapping("/complete")
    @ApiOperation("后台-完成退款")
    public Result<Boolean> completeRefund(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer refundStatus = Integer.valueOf(params.get("refundStatus").toString());
            String refundTradeNo = (String) params.get("refundTradeNo");
            
            boolean result = orderRefundService.completeRefund(id, refundStatus, refundTradeNo);
            
            return result ? Result.success(true) : Result.fail("完成退款操作失败");
        } catch (Exception e) {
            log.error("完成退款操作失败", e);
            return Result.fail("完成退款操作失败: " + e.getMessage());
        }
    }

    /**
     * 后台-退款统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("后台-退款统计数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginTime", value = "开始时间", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "string", paramType = "query")
    })
    public Result<Map<String, Object>> getRefundStatistics(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        
        Map<String, Object> statistics = orderRefundService.getRefundStatistics(beginTime, endTime);
        
        return Result.success(statistics);
    }
} 