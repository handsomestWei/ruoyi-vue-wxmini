package com.ruoyi.wxmini.bo;

import lombok.Data;

/**
 * 微信小程序支付下单参数
 *
 * @author weijiayu
 * @date 2025/6/12 23:10
 */
@Data
public class WxPayCreateOrderParam {

    private String orderNo;
    private String orderDesc;
    private Integer amount;
    private String openId;
    // 业务上可以保存该参数，提供给后端定时任务使用，判断订单是否已经超时；也可以提供给前端用于支付倒计时展示
    private String timeExpire;
}
