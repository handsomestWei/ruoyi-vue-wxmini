package com.ruoyi.wxmini.vo;

import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import lombok.Data;

/**
 * 小程序端拉起支付用的请求参数
 *
 * @author weijiayu
 * @date 2025/6/11 16:21
 */
@Data
public class WxPayParamVo {

    private String orderNo;
    private WxPayUnifiedOrderV3Result.JsapiResult payParam;
    // 历史订单。如果有待支付订单（支付之前可以先核查历史订单），该字段有值
    private String hisOrderNo;
}