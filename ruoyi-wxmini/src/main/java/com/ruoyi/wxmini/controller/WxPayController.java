package com.ruoyi.wxmini.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.util.WxMiniUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 微信小程序支付接口
 *
 * @author weijiayu
 * @date 2025/5/18 23:27
 */
@Slf4j
@RestController
@RequestMapping("/wxmini/pay")
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @PostMapping("/order/create")
    public AjaxResult createOrder() {
        try {
            // 1. 创建下单请求对象
            WxPayUnifiedOrderV3Request v3Request = new WxPayUnifiedOrderV3Request();
            v3Request.setAppid(wxPayService.getConfig().getAppId());
            v3Request.setMchid(wxPayService.getConfig().getMchId());
            v3Request.setDescription("order desc"); // 订单描述，在微信账单展示
            v3Request.setOutTradeNo("orderNo" + new Date().getTime()); // 订单编号，保证唯一
            int timeExpireMin = 5; // 支付超时时间，超时微信侧自动关闭失效
            v3Request.setTimeExpire(DateUtil.format(DateUtil.offsetMinute(new Date(), timeExpireMin),
                    DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));

            // https://github.com/binarywang/WxJava/issues/949
//            v3Request.setNotifyUrl(wxPayService.getConfig().getNotifyUrl());
            v3Request.setNotifyUrl("https://xxx.com/api/wxmini/pay/notify"); // 支付结果通知回调地址

            WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
            amount.setTotal(1); // 订单金额，单位分
            v3Request.setAmount(amount);

            String userId = WxMiniUserContext.getCurrentUserId();
            String openId = "xxx"; // 支付者的微信openId，可以使用当前登录的用户Id在业务层查询出用户信息
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(userId);
            v3Request.setPayer(payer);

            // 2. 调用下单接口
            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = wxPayService.createOrderV3(TradeTypeEnum.JSAPI,
                    v3Request);
            // 3. 返回支付参数
            return AjaxResult.success(jsapiResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    // 主动查询支付结果。建议配合若依的定时任务使用，结合支付时设置的超时时间，查询超时仍在支付中的订单，更新支付结果
    @GetMapping("/order/query")
    public AjaxResult queryOrder(@RequestParam("outTradeNo") String outTradeNo) {
        try {
            WxPayOrderQueryV3Request request = new WxPayOrderQueryV3Request();
            request.setOutTradeNo(outTradeNo);
            return AjaxResult.success(wxPayService.queryOrderV3(request));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    // 支付结果回调
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Step 1: 读取请求体中的 JSON 数据（即 notifyData）
            ServletInputStream inputStream = request.getInputStream();
            String notifyData = IoUtil.readUtf8(inputStream);

            // Step 2: 从请求头中提取 SignatureHeader（关键头信息）
            // @link https://pay.weixin.qq.com/doc/v3/merchant/4012791902
            SignatureHeader signatureHeader = SignatureHeader.builder()
                    .serial(request.getHeader("Wechatpay-Serial"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .timeStamp(request.getHeader("Wechatpay-Timestamp"))
                    .build();
            log.debug("微信支付回调通知-报文：notifyData={}, signatureHeader={}", notifyData, signatureHeader);

            // Step 3: 解析支付异步通知结果
            WxPayNotifyV3Result result = this.wxPayService.parseOrderNotifyV3Result(notifyData, signatureHeader);

            // TODO Step 4: 判断是否成功
            String tradeState = result.getResult().getTradeState();
            log.debug("微信支付回调通知-支付结果={}", result.getResult());
            boolean isPaySuccess = "SUCCESS".equals(tradeState);

            // TODO Step 5: 处理业务逻辑，例如更新订单状态等

            // Step 6: 返回 success 给微信服务器，避免重复回调
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
        }
    }
}
