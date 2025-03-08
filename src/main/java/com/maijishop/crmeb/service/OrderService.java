package com.maijishop.crmeb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maijishop.crmeb.entity.Order;
import com.maijishop.crmeb.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author maijishop
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param userId      用户ID
     * @param addressId   地址ID
     * @param couponId    优惠券ID
     * @param useIntegral 使用积分
     * @param cartIds     购物车ID列表，为空则表示购买全部购物车商品
     * @param productId   商品ID，当立即购买时使用
     * @param productSkuId 商品SKU ID，当立即购买时使用
     * @param quantity    商品数量，当立即购买时使用
     * @param remark      订单备注
     * @param orderType   订单类型：0=普通订单，1=秒杀订单，2=砍价订单，3=拼团订单，4=积分订单
     * @param from        订单来源：1=H5，2=小程序，3=APP，4=PC
     * @return 订单ID和订单号
     */
    Map<String, Object> createOrder(Long userId, Long addressId, Long couponId, Integer useIntegral, 
                                   List<Long> cartIds, Long productId, Long productSkuId, Integer quantity, 
                                   String remark, Integer orderType, Integer from);

    /**
     * 获取订单预创建信息
     *
     * @param userId      用户ID
     * @param addressId   地址ID
     * @param couponId    优惠券ID
     * @param useIntegral 使用积分
     * @param cartIds     购物车ID列表，为空则表示购买全部购物车商品
     * @param productId   商品ID，当立即购买时使用
     * @param productSkuId 商品SKU ID，当立即购买时使用
     * @param quantity    商品数量，当立即购买时使用
     * @param orderType   订单类型：0=普通订单，1=秒杀订单，2=砍价订单，3=拼团订单，4=积分订单
     * @return 订单预创建信息
     */
    Map<String, Object> getPreOrderInfo(Long userId, Long addressId, Long couponId, Integer useIntegral, 
                                      List<Long> cartIds, Long productId, Long productSkuId, Integer quantity, 
                                      Integer orderType);

    /**
     * 支付订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @param payType 支付方式：0=余额支付，1=微信支付，2=支付宝支付，3=银行卡支付
     * @return 支付结果
     */
    Map<String, Object> payOrder(String orderNo, Long userId, Integer payType);

    /**
     * 处理支付回调
     *
     * @param orderNo      订单号
     * @param transactionId 支付流水号
     * @param payType      支付方式：0=余额支付，1=微信支付，2=支付宝支付，3=银行卡支付
     * @return 是否成功
     */
    boolean handlePayCallback(String orderNo, String transactionId, Integer payType);

    /**
     * 发货
     *
     * @param id          订单ID
     * @param expressCode 快递公司编码
     * @param expressName 快递公司名称
     * @param expressNo   快递单号
     * @return 是否成功
     */
    boolean ship(Long id, String expressCode, String expressName, String expressNo);

    /**
     * 确认收货
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean confirmReceive(Long id, Long userId);

    /**
     * 取消订单
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelOrder(Long id, Long userId, String reason);

    /**
     * 申请退款
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @param images 图片凭证，逗号分隔
     * @return 是否成功
     */
    boolean applyRefund(Long id, Long userId, String reason, String images);

    /**
     * 处理退款
     *
     * @param id          订单ID
     * @param status      处理状态：1=同意，2=拒绝
     * @param refundPrice 退款金额
     * @param reason      处理原因
     * @return 是否成功
     */
    boolean handleRefund(Long id, Integer status, Double refundPrice, String reason);

    /**
     * 删除订单
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteOrder(Long id, Long userId);

    /**
     * 获取订单详情
     *
     * @param id     订单ID
     * @param userId 用户ID，传入则验证订单是否属于该用户
     * @return 订单详情
     */
    Map<String, Object> getOrderDetail(Long id, Long userId);

    /**
     * 获取订单商品
     *
     * @param orderId 订单ID
     * @return 订单商品列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 获取订单列表
     *
     * @param userId  用户ID
     * @param status  订单状态：-1=全部，0=待付款，1=待发货，2=待收货，3=待评价，4=已完成，5=已取消，6=已退款
     * @param page    页码
     * @param limit   每页数量
     * @return 订单列表
     */
    IPage<Order> getUserOrderList(Long userId, Integer status, Integer page, Integer limit);

    /**
     * 获取订单物流信息
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @return 物流信息
     */
    Map<String, Object> getOrderLogistics(Long id, Long userId);

    /**
     * 订单分页列表查询
     *
     * @param page          分页参数
     * @param orderNo       订单号
     * @param status        订单状态
     * @param orderType     订单类型
     * @param payStatus     支付状态
     * @param shippingStatus 发货状态
     * @param beginTime     开始时间
     * @param endTime       结束时间
     * @return 订单分页列表
     */
    IPage<Order> getOrderPage(Page<Order> page, String orderNo, Integer status, Integer orderType, Integer payStatus, Integer shippingStatus, String beginTime, String endTime);

    /**
     * 订单统计数据
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据
     */
    Map<String, Object> getOrderStatistics(String beginTime, String endTime);

    /**
     * 获取销量排行
     *
     * @param limit     数量限制
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 销量排行
     */
    List<Map<String, Object>> getProductRanking(Integer limit, String beginTime, String endTime);

    /**
     * 自动取消超时订单
     */
    void cancelTimeoutOrders();
} 