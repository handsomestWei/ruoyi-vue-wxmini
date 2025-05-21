package com.ruoyi.wxmini.bo;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.ruoyi.wxmini.domain.UserInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author weijiayu
 * @date 2025/4/25 21:38
 */
@Data
public class WxUserInfo {

    private String sessionKey;
    private String openId;

    private String userName;
    /**
     * 用户类型。在小程序端可以根据用户类型做页面权限访问控制
     */
    private String userType;
    private String phone;
    private String avatarUrl;

    private String apiToken;

    public void wapper(WxMaJscode2SessionResult wxSession, UserInfo userInfo) {
        this.sessionKey = wxSession.getSessionKey();
        this.openId = wxSession.getOpenid();

        this.userName = userInfo.getUserName();
        this.userType = userInfo.getUserType();
        this.phone = userInfo.getPhone();
        this.avatarUrl = userInfo.getAvatarUrl();

        if (StringUtils.isEmpty(this.userName)) {
            this.userName = "微信用户";
        }
        if (StringUtils.isEmpty(this.userType)) {
            this.userType = "0";
        }
    }
}
