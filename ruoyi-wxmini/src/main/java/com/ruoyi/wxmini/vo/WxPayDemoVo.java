package com.ruoyi.wxmini.vo;

import lombok.Data;

/**
 * demo-支付请求参数vo
 *
 * @author weijiayu
 * @date 2025/6/18 21:19
 */
@Data
public class WxPayDemoVo {

    // 申请支付时，传入购买的商品id，再在后台关联金额，而不是直接从前端传金额
    private Long productId;
}
