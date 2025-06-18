package com.ruoyi.wxmini.service;

import com.github.binarywang.wxpay.exception.WxPayException;
import com.ruoyi.wxmini.vo.WxPayParamVo;

/**
 * 微信小程序支付接口
 *
 * @param <P> 支付请求参数
 * @author weijiayu
 * @date 2025/6/14 20:47
 */
public interface IWxPayBaseService<P> {

    /**
     * 创建支付订单
     *
     * @param userId
     * @param payVo
     * @return com.yigeng.wxmini.vo.WxPayParamVo
     * @throws Exception
     * @date 2025/6/12 23:50
     */
    public WxPayParamVo createOrder(String userId, P payVo) throws Exception;

    /**
     * 重新支付历史订单
     *
     * @param userId
     * @param orderNo
     * @return com.yigeng.wxmini.vo.WxPayParamVo
     * @throws Exception
     * @date 2025/6/13 0:20
     */
    public WxPayParamVo reCreatOrder(String userId, String orderNo) throws Exception;

    /**
     * 取消支付订单
     *
     * @param userId
     * @param orderNo
     * @return java.lang.Boolean
     * @throws WxPayException
     * @date 2025/6/13 0:11
     */
    public Boolean cancelOrder(String userId, String orderNo) throws WxPayException;

    /**
     * 查询支付结果，并立即更新订单状态（有副作用）
     *
     * @param orderNo
     * @return java.lang.Boolean
     * @throws WxPayException
     * @date 2025/6/13 0:13
     */
    public Boolean queryPayResultAndUpdOrderStatus(String orderNo) throws WxPayException;

    /**
     * 处理支付结果
     *
     * @param payResult
     * @param orderNo
     * @return java.lang.Boolean
     * @date 2025/6/13 0:12
     */
    public Boolean handlePayResult(Boolean payResult, String orderNo);
}
