package com.ruoyi.wxmini.service.impl;

import com.ruoyi.wxmini.bo.WxPayCreateOrderParam;
import com.ruoyi.wxmini.service.AbsWxPayBaseService;
import com.ruoyi.wxmini.service.IWxPayDemoService;
import com.ruoyi.wxmini.vo.WxPayDemoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * demo-支付接口实现类。继承+接口的组合方式
 *
 * @author weijiayu
 * @date 2025/6/18 21:21
 */
@Service
@Slf4j
public class WxPayDemoServiceImpl extends AbsWxPayBaseService<WxPayDemoVo> implements IWxPayDemoService {

    @Override
    public Boolean checkBeforeCreatOrder(String userId, WxPayDemoVo payVo) throws Exception {
        return null;
    }

    @Override
    public Boolean checkUserOrderIsMatch(String userId, String orderNo) {
        return null;
    }

    @Override
    public WxPayCreateOrderParam buildOrderParam(String userId, WxPayDemoVo payVo, HashMap<String, Object> contextMap) {
        return null;
    }

    @Override
    public WxPayDemoVo buildPayVoWithReCreatOrder(String userId, String orderNo) {
        return null;
    }

    @Override
    public Boolean saveOrderInfo(String orderNo, WxPayDemoVo payVo, WxPayCreateOrderParam orderParam, HashMap<String,
            Object> contextMap) {
        return null;
    }

    @Override
    public Boolean updOrderWithPaySuccess(String orderNo) {
        return null;
    }

    @Override
    public Boolean closeOrder(String orderNo) {
        return null;
    }
}
