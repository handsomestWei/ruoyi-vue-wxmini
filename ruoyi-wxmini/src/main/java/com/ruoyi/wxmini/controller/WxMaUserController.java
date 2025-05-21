package com.ruoyi.wxmini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.domain.UserInfo;
import com.ruoyi.wxmini.service.IUserInfoService;
import com.ruoyi.wxmini.util.WxMiniUserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wxmini/user")
public class WxMaUserController {

    private final WxMaService wxMaService;
    @Resource
    private IUserInfoService userInfoService;

    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @GetMapping("/info")
    public AjaxResult info(String appid, String sessionKey,
                           String signature, String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            return AjaxResult.error(String.format("can not find appid=[%s] config", appid));
        }

        try {
            // 用户信息校验
//            if (!wxMaService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
//                return AjaxResult.error("user check failed");
//            }

            // 解密用户信息
            WxMaUserInfo wxUserInfo = wxMaService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
            UserInfo userInfo = userInfoService.selectUserInfoByUserId(WxMiniUserContext.getCurrentUserId());
            if (userInfo != null) {
                userInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
                userInfo.setUserName(wxUserInfo.getNickName());
                userInfoService.updateUserInfo(userInfo);
            }
            return AjaxResult.success(wxUserInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    /**
     * <pre>
     * 获取用户绑定手机号信息
     * </pre>
     */
    @GetMapping("/phone")
    public AjaxResult phone(String appid, String sessionKey, String signature,
                            String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            return AjaxResult.error(String.format("can not find appid=[%s] config", appid));
        }

        try {
            // 用户信息校验
//            if (!wxMaService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
//                return AjaxResult.error("user check failed");
//            }

            // 解密
            WxMaPhoneNumberInfo phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData,
                    iv);
            String phone = phoneNoInfo.getPhoneNumber();
            UserInfo userInfo = userInfoService.selectUserInfoByUserId(WxMiniUserContext.getCurrentUserId());
            if (userInfo != null) {
                userInfo.setPhone(phone);
                userInfoService.updateUserInfo(userInfo);
            }
            return AjaxResult.success(phoneNoInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }

}
