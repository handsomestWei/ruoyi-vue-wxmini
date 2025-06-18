package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.vo.WxPayDemoVo;

/**
 * demo-支付接口。增加一层子接口做隔离不同业务的支付，继承父接口，实现多态，而不是在impl类直接实现同一个父接口
 *
 * @author weijiayu
 * @date 2025/6/18 21:19
 */
public interface IWxPayDemoService extends IWxPayBaseService<WxPayDemoVo> {
}
