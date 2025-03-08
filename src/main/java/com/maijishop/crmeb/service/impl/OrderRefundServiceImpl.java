package com.maijishop.crmeb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maijishop.crmeb.entity.Order;
import com.maijishop.crmeb.entity.OrderItem;
import com.maijishop.crmeb.entity.OrderRefund;
import com.maijishop.crmeb.mapper.OrderItemMapper;
import com.maijishop.crmeb.mapper.OrderMapper;
import com.maijishop.crmeb.mapper.OrderRefundMapper;
import com.maijishop.crmeb.service.OrderRefundService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单退款服务实现类
 *
 * @author maijishop
 */
@Slf4j
@Service
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements OrderRefundService {

    @Resource
    private OrderRefundMapper orderRefundMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(Long orderId, String orderNo, Long userId, BigDecimal refundAmount, 
                             String refundReason, String refundExplain, String refundProof, Integer refundType) {
        // 1. 检查订单是否存在且属于该用户
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .eq(Order::getIsDel, 0));
        
        if (order == null) {
            log.error("申请退款失败：订单不存在或不属于该用户, orderId={}, userId={}", orderId, userId);
            return false;
        }
        
        // 2. 检查订单是否已支付
        if (order.getPayStatus() == 0) {
            log.error("申请退款失败：订单未支付, orderId={}", orderId);
            return false;
        }
        
        // 3. 检查退款金额是否合法
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0 || refundAmount.compareTo(order.getPayAmount()) > 0) {
            log.error("申请退款失败：退款金额不合法, refundAmount={}, orderAmount={}", refundAmount, order.getPayAmount());
            return false;
        }
        
        // 4. 生成退款单号
        String refundNo = generateRefundNo(orderNo);
        
        // 5. 创建退款申请
        OrderRefund refund = new OrderRefund();
        refund.setOrderId(orderId);
        refund.setOrderNo(orderNo);
        refund.setRefundNo(refundNo);
        refund.setUserId(userId);
        refund.setRefundAmount(refundAmount);
        refund.setRefundReason(refundReason);
        refund.setRefundExplain(refundExplain);
        refund.setRefundProof(refundProof);
        refund.setRefundType(refundType);
        refund.setRefundStatus(0);  // 初始状态：退款申请中
        refund.setIsDel(0);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        
        return save(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long id, Integer refundStatus, String rejectReason, String handlePerson, String handleRemark) {
        if (id == null || refundStatus == null) {
            return false;
        }
        
        // 1. 检查退款申请是否存在
        OrderRefund refund = getById(id);
        if (refund == null) {
            log.error("处理退款申请失败：退款申请不存在, id={}", id);
            return false;
        }
        
        // 2. 检查退款状态是否为待处理
        if (refund.getRefundStatus() != 0) {
            log.error("处理退款申请失败：退款状态不是待处理, id={}, status={}", id, refund.getRefundStatus());
            return false;
        }
        
        // 3. 更新退款状态
        int result = orderRefundMapper.handleRefund(
                id,
                refundStatus,
                rejectReason,
                handlePerson,
                LocalDateTime.now(),
                handleRemark
        );
        
        if (result > 0) {
            // 4. 如果拒绝退款，更新订单项退款状态
            if (refundStatus == 2) {
                Order order = orderMapper.selectById(refund.getOrderId());
                if (order != null) {
                    List<OrderItem> orderItems = orderItemMapper.selectByOrderId(refund.getOrderId());
                    for (OrderItem item : orderItems) {
                        orderItemMapper.updateRefundStatus(item.getId(), 3); // 3 = 退款失败
                    }
                }
            }
        }
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRefund(Long id, Integer refundStatus, String refundTradeNo) {
        if (id == null || refundStatus == null) {
            return false;
        }
        
        // 1. 检查退款申请是否存在
        OrderRefund refund = getById(id);
        if (refund == null) {
            log.error("完成退款操作失败：退款申请不存在, id={}", id);
            return false;
        }
        
        // 2. 检查退款状态是否为处理中
        if (refund.getRefundStatus() != 1) {
            log.error("完成退款操作失败：退款状态不是处理中, id={}, status={}", id, refund.getRefundStatus());
            return false;
        }
        
        // 3. 更新退款状态
        int result = orderRefundMapper.completeRefund(
                id,
                refundStatus,
                LocalDateTime.now(),
                refundTradeNo
        );
        
        if (result > 0) {
            // 4. 更新订单状态和订单项退款状态
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null && refundStatus == 3) {  // 3 = 退款成功
                // 更新订单状态为已退款
                orderMapper.updateStatus(order.getId(), 6, LocalDateTime.now());
                
                // 更新订单项退款状态
                List<OrderItem> orderItems = orderItemMapper.selectByOrderId(refund.getOrderId());
                for (OrderItem item : orderItems) {
                    orderItemMapper.updateRefundStatus(item.getId(), 2); // 2 = 退款成功
                }
            }
        }
        
        return result > 0;
    }

    @Override
    public OrderRefund getRefundDetail(Long id, Long userId) {
        if (id == null) {
            return null;
        }
        
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderRefund::getId, id)
                .eq(OrderRefund::getIsDel, 0);
        
        // 如果指定了用户ID，则验证退款是否属于该用户
        if (userId != null) {
            queryWrapper.eq(OrderRefund::getUserId, userId);
        }
        
        return getOne(queryWrapper);
    }

    @Override
    public List<OrderRefund> getOrderRefundList(Long orderId) {
        if (orderId == null) {
            return null;
        }
        
        return orderRefundMapper.selectOrderRefundList(orderId);
    }

    @Override
    public IPage<OrderRefund> getUserRefundPage(Long userId, Integer page, Integer limit) {
        if (userId == null) {
            return null;
        }
        
        Page<OrderRefund> pageParm = new Page<>(page, limit);
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getIsDel, 0)
                .orderByDesc(OrderRefund::getCreateTime);
        
        return page(pageParm, queryWrapper);
    }

    @Override
    public IPage<OrderRefund> getRefundPage(Page<OrderRefund> page, String orderNo, String refundNo, 
                                          Integer refundStatus, Integer refundType, String beginTime, String endTime) {
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderRefund::getIsDel, 0);
        
        if (StringUtils.isNotBlank(orderNo)) {
            queryWrapper.eq(OrderRefund::getOrderNo, orderNo);
        }
        
        if (StringUtils.isNotBlank(refundNo)) {
            queryWrapper.eq(OrderRefund::getRefundNo, refundNo);
        }
        
        if (refundStatus != null) {
            queryWrapper.eq(OrderRefund::getRefundStatus, refundStatus);
        }
        
        if (refundType != null) {
            queryWrapper.eq(OrderRefund::getRefundType, refundType);
        }
        
        if (StringUtils.isNotBlank(beginTime)) {
            LocalDateTime begin = LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge(OrderRefund::getCreateTime, begin);
        }
        
        if (StringUtils.isNotBlank(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le(OrderRefund::getCreateTime, end);
        }
        
        queryWrapper.orderByDesc(OrderRefund::getCreateTime);
        
        return page(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getRefundStatistics(String beginTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime begin = null;
        LocalDateTime end = null;
        
        if (StringUtils.isNotBlank(beginTime)) {
            begin = LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        if (StringUtils.isNotBlank(endTime)) {
            end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        // 统计总申请数
        int totalRefunds = orderRefundMapper.countRefunds(null, begin, end);
        result.put("totalRefunds", totalRefunds);
        
        // 统计待处理申请数
        int pendingRefunds = orderRefundMapper.countRefunds(0, begin, end);
        result.put("pendingRefunds", pendingRefunds);
        
        // 统计处理中申请数
        int processingRefunds = orderRefundMapper.countRefunds(1, begin, end);
        result.put("processingRefunds", processingRefunds);
        
        // 统计成功退款数
        int successRefunds = orderRefundMapper.countRefunds(3, begin, end);
        result.put("successRefunds", successRefunds);
        
        // 统计失败退款数
        int failedRefunds = orderRefundMapper.countRefunds(4, begin, end);
        result.put("failedRefunds", failedRefunds);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefund(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        
        // 1. 检查退款申请是否存在且属于该用户
        OrderRefund refund = getOne(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getId, id)
                .eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getIsDel, 0));
        
        if (refund == null) {
            log.error("取消退款申请失败：退款申请不存在或不属于该用户, id={}, userId={}", id, userId);
            return false;
        }
        
        // 2. 检查退款状态是否为申请中或审核中
        if (refund.getRefundStatus() > 1) {
            log.error("取消退款申请失败：退款已处理，无法取消, id={}, status={}", id, refund.getRefundStatus());
            return false;
        }
        
        // 3. 逻辑删除退款申请
        refund.setIsDel(1);
        refund.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(refund);
        
        if (result) {
            // 4. 更新订单项退款状态
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(refund.getOrderId());
            for (OrderItem item : orderItems) {
                orderItemMapper.updateRefundStatus(item.getId(), 0); // 0 = 未申请退款
            }
        }
        
        return result;
    }
    
    /**
     * 生成退款单号
     */
    private String generateRefundNo(String orderNo) {
        return "RF" + orderNo.substring(2) + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
    }
} 