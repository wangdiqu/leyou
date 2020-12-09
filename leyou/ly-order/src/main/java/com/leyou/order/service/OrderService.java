package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1.创建订单
        Order order=new Order();
        //1.1订单号及基本信息
        // 生成订单号，利用雪花算法生成全局ID
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setCreateTime(new Date());
        //1.2用户信息,使用监听器获得用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        //1.3收货人
        AddressDTO address = AddressClient.findById(orderDTO.getAddressId());
        //收货人信息
        order.setReceiver(address.getName());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverZip(address.getZipCode());
        //1.4金额
        //将Carts装换为map
        Map<Long, Integer> map = (Map<Long, Integer>) orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> ids = map.keySet();
        //根据id查询sku
        List<Sku> skus = goodsClient.querySkuByids(new ArrayList<>(ids));
        //准备订单详情集合
        List<OrderDetail> details=new ArrayList<>();
        Long totalPay=0L;
        for (Sku sku: skus){
            //计算商品总价
            totalPay=sku.getPrice() * map.get(sku.getId());
            //封装订单详情detail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(map.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        order.setTotalPay(totalPay);
        //实付金额，总金额+邮费-优惠金额
        order.setActualPay(totalPay+order.getPostFee() - 0);
        //写入数据库
        int orderCount = orderMapper.insertSelective(order);//有选择行的写入
        if(orderCount != 1){
            log.error("[创建订单] 创建订单失败！, orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //2.创建订单详情
        int detailCount = orderDetailMapper.insertList(details);
        if(detailCount != details.size()){
            log.error("[创建订单详情] 创建订单详情失败！, orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //3.创建订单状态
        OrderStatus orderStatus=new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        int statusCount = orderStatusMapper.insertSelective(orderStatus);
        if(statusCount != 1){
            log.error("[创建订单状态] 创建订单状态失败！, orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //4.减库存
        //此处有分布式事务问题，电商种一般是TCC，转账业务适合"异步确保"，解决方法有三种
        //1.主PC或者叫三PC，是先给所有的微服务发送一个执行命令，看所有执行的结果都是成功在继续执行，有一个失败就都回滚。性能差，会锁彪
        //2.TCC(补偿形式)，是所有微服务各自自行执行提交，然后集中处理执行结果，成功就继续执行，有失败就会回滚。
        //3.异步确保，他有两种，是由mq实现的分布式事务，
        //3.1第一种，只管发送命令消息，后续的自己解决，但接受消息的一端必须保证消息顺利成功，不然会接着执行直到成功，不适合此处微服务
        //3.2第二种，利用mq结合TCC
        List<CartDTO> cartDTOS=orderDTO.getCarts();
        goodsClient.decreaseStock(cartDTOS);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            //不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail orderDetail=new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetailList(details);
        //查询订单状态
        OrderStatus orderStatus=orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单金额
        Order order = queryOrderById(orderId);
        //判断状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
              throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        //Long actualPay = order.getActualPay();//真实项目中使用此方法
        Long actualPay = 1L;//测试专用为一分钱
        //商品描述
        OrderDetail detail = order.getOrderDetailList().get(0);
        String desc = detail.getTitle();
        return payHelper.creteOrder(orderId,actualPay,desc);
    }

    public void handleNotify(Map<String, String> result) {
        //数据校验
        payHelper.isSuccess(result);
        //校验签名
        payHelper.isValidSign(result);
        //校验金额
        String totalFeeStr = result.get("total_fee");
        //获取订单号
        String outTradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(outTradeNo)){
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //获取订单金额
        Long orderId = Long.valueOf(outTradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee != /*order.getActualPay()*/ 1){
            //金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //修改订单状态
        OrderStatus orderStatus=new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAYED.value());
        orderStatus.setOrderId(orderId);
        orderStatus.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if (count != 1) {
             throw new LyException(ExceptionEnum.UPDATE_ORDER_STARTUS_ERROR);
        }
        log.info("[订单回调] 订单支付成功,订单编号:{}",orderId);
    }

    public PayState qureyOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if(status !=OrderStatusEnum.UN_PAY.value()){
            //如果已支付，是真的支付了
            return PayState.SUCCESS;
        }
        //如果未支付，去微信查询支付状态
        return payHelper.queryPayState(orderId);
    }
}
