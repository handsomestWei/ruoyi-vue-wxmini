package com.ruoyi.wxmini.service;

import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.wxmini.bo.WxPayCreateOrderParam;
import com.ruoyi.wxmini.vo.WxPayParamVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序支付模板抽象基类
 *
 * @param <P> 支付请求参数
 * @author weijiayu
 * @date 2025/6/12 23:12
 */
@Service
public abstract class AbsWxPayBaseService<P> {

    @Resource
    private WxPayService wxPayService;

    private static final String WX_PAY_NOTIFY_URL = "https://xxx.com/api/wxmini/pay/notify";
    // 无锁化的Map+原子操作，记录资源的“占用状态”。synchronized会让同一资源的请求串行化，虽然能保证唯一性，但高并发下会阻塞线程，影响吞吐量。
    private ConcurrentHashMap<String, Object> resourceFlagMap = new ConcurrentHashMap<>();

    /**
     * 创建支付订单
     *
     * @param userId
     * @param payVo
     * @return com.ruoyi.wxmini.vo.WxPayParamVo
     * @throws Exception
     * @date 2025/6/12 23:50
     */
    public WxPayParamVo createOrder(String userId, P payVo) throws Exception {
        // 获取资源id，尝试占用资源。对资源加锁，避免生成重复订单
        String resourceId = this.getResourceId(payVo);
        if (!(resourceFlagMap.putIfAbsent(resourceId, Boolean.TRUE) == null)) {
            // 已有请求在处理该资源，直接返回或抛异常
            throw new RuntimeException("请稍后再试");
        }
        try {
            // 1、创建订单前的业务核验
            if (!this.checkBeforeCreatOrder(userId, payVo)) {
                return null;
            }
            // 局部变量上下文缓存，减少数据重复查询
            HashMap<String, Object> contextMap = new HashMap<>();
            // 2、构建订单参数
            WxPayCreateOrderParam orderParam = this.buildOrderParam(userId, payVo, contextMap);
            if (orderParam == null) {
                return null;
            }
            // 3、获取支付参数
            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = this.createOrder(orderParam);
            if (jsapiResult == null) {
                return null;
            }
            // 4、保存订单信息
            String orderNo = orderParam.getOrderNo();
            if (this.saveOrderInfo(orderNo, payVo, orderParam, contextMap)) {
                WxPayParamVo payParamVo = new WxPayParamVo();
                payParamVo.setOrderNo(orderNo);
                payParamVo.setPayParam(jsapiResult);
                return payParamVo;
            } else {
                return null;
            }
        } finally {
            // 释放资源占用
            resourceFlagMap.remove(resourceId);
        }
    }

    /**
     * 重新支付历史订单
     *
     * @param userId
     * @param orderNo
     * @return com.ruoyi.wxmini.vo.WxPayParamVo
     * @throws Exception
     * @date 2025/6/13 0:20
     */
    public WxPayParamVo reCreatOrder(String userId, String orderNo) throws Exception {
        // 1、业务核验
        if (!this.checkUserOrderIsMatch(userId, orderNo)) {
            return null;
        }
        // 2、重新构建支付请求参数
        P payVo = this.buildPayVoWithReCreatOrder(userId, orderNo);
        // 3、查询上次支付结果
        if (this.queryPayResultAndUpdOrderStatus(orderNo)) {
            // 已支付成功，无需再次支付
            return null;
        }
        // 重新生成新订单
        return this.createOrder(userId, payVo);
    }

    /**
     * 取消支付订单
     *
     * @param userId
     * @param orderNo
     * @return java.lang.Boolean
     * @throws WxPayException
     * @date 2025/6/13 0:11
     */
    public Boolean cancelOrder(String userId, String orderNo) throws WxPayException {
        // 1、业务核验
        if (!this.checkUserOrderIsMatch(userId, orderNo)) {
            return false;
        }
        // 2、查询上次支付结果。以实际支付结果为准
        this.queryPayResultAndUpdOrderStatus(orderNo);
        return true;
    }

    /**
     * 查询支付结果，并立即更新订单状态（有副作用）
     *
     * @param orderNo
     * @return java.lang.Boolean
     * @throws WxPayException
     * @date 2025/6/13 0:13
     */
    public Boolean queryPayResultAndUpdOrderStatus(String orderNo) throws WxPayException {
        WxPayOrderQueryV3Request request = new WxPayOrderQueryV3Request();
        request.setOutTradeNo(orderNo);
        WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(request);
        Boolean payResult = "SUCCESS".equals(result.getTradeState());
        if (!payResult) {
            // 非支付成功，强制关闭旧订单
            wxPayService.closeOrderV3(orderNo);
        }
        this.handlePayResult(payResult, orderNo);
        return payResult;
    }

    /**
     * 处理支付结果
     *
     * @param payResult
     * @param orderNo
     * @return java.lang.Boolean
     * @date 2025/6/13 0:12
     */
    public Boolean handlePayResult(Boolean payResult, String orderNo) {
        if (payResult) {
            return this.updOrderWithPaySuccess(orderNo);
        } else {
            return this.closeOrder(orderNo);
        }
    }

    /*
     * 获取资源Id，粒度要细。后续创建订单时，对资源加锁，避免创建重复订单。
     * 某些资源也可能没有限制，不需要锁定，每次只需要返回唯一id即可
     */
    public abstract String getResourceId(P payVo);

    /**
     * 创建订单前做业务核验
     *
     * @param userId
     * @param payVo
     * @return java.lang.Boolean
     * @throws Exception
     * @date 2025/6/12 23:50
     */
    public abstract Boolean checkBeforeCreatOrder(String userId, P payVo) throws Exception;

    /**
     * 校验用户和订单是否匹配
     *
     * @param userId
     * @param orderNo
     * @return java.lang.Boolean
     * @date 2025/6/13 0:10
     */
    public abstract Boolean checkUserOrderIsMatch(String userId, String orderNo);

    /**
     * 构建支付订单参数
     *
     * @param userId
     * @param payVo
     * @param contextMap
     * @return com.ruoyi.wxmini.bo.WxPayCreateOrderParam
     * @date 2025/6/12 23:51
     */
    public abstract WxPayCreateOrderParam buildOrderParam(String userId, P payVo,
                                                          HashMap<String, Object> contextMap);

    /**
     * 重新构建支付请求参数
     *
     * @param userId
     * @param orderNo
     * @return P
     * @throws
     * @date 2025/6/13 0:23
     */
    public abstract P buildPayVoWithReCreatOrder(String userId, String orderNo);

    /**
     * 保存订单信息
     *
     * @param orderNo
     * @param payVo
     * @param contextMap
     * @return java.lang.Boolean
     * @date 2025/6/12 23:52
     */
    public abstract Boolean saveOrderInfo(String orderNo, P payVo, WxPayCreateOrderParam orderParam, HashMap<String,
            Object> contextMap);

    /**
     * 支付成功，更新订单信息
     *
     * @param orderNo
     * @return java.lang.Boolean
     * @date 2025/6/12 23:57
     */
    public abstract Boolean updOrderWithPaySuccess(String orderNo);

    /**
     * 关闭订单
     *
     * @param orderNo
     * @return java.lang.Boolean
     * @date 2025/6/12 23:58
     */
    public abstract Boolean closeOrder(String orderNo);

    /**
     * 创建支付订单参数
     *
     * @param orderParam
     * @return com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result.JsapiResult
     * @throws
     * @date 2025/6/12 23:52
     */
    private WxPayUnifiedOrderV3Result.JsapiResult createOrder(WxPayCreateOrderParam orderParam) throws WxPayException {
        // 1. 创建下单请求对象
        WxPayUnifiedOrderV3Request v3Request = new WxPayUnifiedOrderV3Request();
        v3Request.setAppid(wxPayService.getConfig().getAppId());
        v3Request.setMchid(wxPayService.getConfig().getMchId());
        v3Request.setDescription(orderParam.getOrderDesc());
        v3Request.setOutTradeNo(orderParam.getOrderNo());
        v3Request.setTimeExpire(orderParam.getTimeExpire());

        // https://github.com/binarywang/WxJava/issues/949
//        v3Request.setNotifyUrl(wxPayService.getConfig().getNotifyUrl());
        v3Request.setNotifyUrl(WX_PAY_NOTIFY_URL);

        WxPayUnifiedOrderV3Request.Amount amountObj = new WxPayUnifiedOrderV3Request.Amount();
        amountObj.setTotal(orderParam.getAmount()); // 单位分
        v3Request.setAmount(amountObj);

        WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
        payer.setOpenid(orderParam.getOpenId());
        v3Request.setPayer(payer);

        // 2. 调用下单接口
        return wxPayService.createOrderV3(TradeTypeEnum.JSAPI, v3Request);
    }
}
