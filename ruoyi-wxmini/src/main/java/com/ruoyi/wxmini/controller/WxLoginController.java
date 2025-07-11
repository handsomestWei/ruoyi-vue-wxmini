package com.ruoyi.wxmini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.wxmini.bo.WxUserInfo;
import com.ruoyi.wxmini.domain.UserInfo;
import com.ruoyi.wxmini.service.IUserInfoService;
import com.ruoyi.wxmini.service.IWxMiniJwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信小程序登录接口
 *
 * @author weijiayu
 * @date 2025/4/25 22:15
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wxmini")
public class WxLoginController {

    private final WxMaService wxMaService;

    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IWxMiniJwtService jwtService;

    /**
     * 登陆接口
     */
    @GetMapping("/login")
    public AjaxResult login(String appid, String code) {
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("empty jscode");
        }

        if (!wxMaService.switchover(appid)) {
            return AjaxResult.error(String.format("can not find appid=[%s] config", appid));
        }

        WxUserInfo wxUserInfo = new WxUserInfo();
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            // 同步微信侧用户信息：如果已注册，返回用户信息；如果未注册，自动注册并保存用户信息
            String openId = session.getOpenid();
            UserInfo userInfo = userInfoService.selectUserInfoByOpenId(openId);
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setUserId(UUID.randomUUID().toString());
                userInfo.setOpenId(openId);
                userInfo.setUnionId(session.getUnionid());
                userInfoService.insertUserInfo(userInfo);
            }
            // 只返回必要信息给前端
            wxUserInfo.wapper(session, userInfo);
            // 生成令牌，用于自定义业务接口鉴权
            wxUserInfo.setApiToken(jwtService.createToken(userInfo.getUserId()));
            return AjaxResult.success(wxUserInfo);
        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }
}
