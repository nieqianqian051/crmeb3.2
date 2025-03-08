package com.maijishop.crmeb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maijishop.crmeb.entity.OrderRefund;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单退款服务接口
 *
 * @author maijishop
 */
public interface OrderRefundService extends IService<OrderRefund> {

    /**
     * 申请退款
     *
     * @param orderId      订单ID
     * @param orderNo      订单号
     * @param userId       用户ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @param refundExplain 退款说明
     * @param refundProof  退款凭证图片，多张用逗号分隔
     * @param refundType   退款类型：1=仅退款，2=退货退款
     * @return 是否成功
     */
    boolean applyRefund(Long orderId, String orderNo, Long userId, BigDecimal refundAmount, 
                      String refundReason, String refundExplain, String refundProof, Integer refundType);

    /**
     * 处理退款申请
     *
     * @param id            退款ID
     * @param refundStatus  退款状态：1=同意，2=拒绝
     * @param rejectReason  拒绝原因
     * @param handlePerson  处理人员
     * @param handleRemark  处理备注
     * @return 是否成功
     */
    boolean handleRefund(Long id, Integer refundStatus, String rejectReason, String handlePerson, String handleRemark);

    /**
     * 完成退款
     *
     * @param id           退款ID
     * @param refundStatus 退款状态：3=退款成功，4=退款失败
     * @param refundTradeNo 退款交易单号
     * @return 是否成功
     */
    boolean completeRefund(Long id, Integer refundStatus, String refundTradeNo);

    /**
     * 获取退款详情
     *
     * @param id     退款ID
     * @param userId 用户ID，非空则验证退款是否属于该用户
     * @return 退款详情
     */
    OrderRefund getRefundDetail(Long id, Long userId);

    /**
     * 获取订单的退款列表
     *
     * @param orderId 订单ID
     * @return 退款列表
     */
    List<OrderRefund> getOrderRefundList(Long orderId);

    /**
     * 获取用户的退款列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param limit  每页数量
     * @return 退款分页列表
     */
    IPage<OrderRefund> getUserRefundPage(Long userId, Integer page, Integer limit);

    /**
     * 退款申请分页列表查询
     *
     * @param page         分页参数
     * @param orderNo      订单号
     * @param refundNo     退款单号
     * @param refundStatus 退款状态
     * @param refundType   退款类型
     * @param beginTime    开始时间
     * @param endTime      结束时间
     * @return 退款分页列表
     */
    IPage<OrderRefund> getRefundPage(Page<OrderRefund> page, String orderNo, String refundNo, 
                                   Integer refundStatus, Integer refundType, String beginTime, String endTime);

    /**
     * 退款统计数据
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据
     */
    Map<String, Object> getRefundStatistics(String beginTime, String endTime);

    /**
     * 取消退款申请
     *
     * @param id     退款ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelRefund(Long id, Long userId);
} 