package com.ruoyi.wxmini.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户信息对象 user_info
 *
 * @author ruoyi
 * @date 2025-04-25
 */
public class UserInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 平台用户id
     */
    @Excel(name = "平台用户id")
    private String userId;

    /**
     * 用户名
     */
    @Excel(name = "用户名")
    private String userName;

    /**
     * 用户类型。在小程序端可以根据用户类型做页面权限访问控制
     */
    @Excel(name = "用户类型")
    private String userType;

    /**
     * 手机号
     */
    @Excel(name = "手机号")
    private String phone;

    /**
     * 微信用户唯一标识
     */
    @Excel(name = "微信用户唯一标识")
    private String openId;

    @Excel(name = "微信全平台用户唯一标识")
    private String unionId;

    /**
     * 用户头像
     */
    @Excel(name = "用户头像")
    private String avatarUrl;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getOpenId() {
        return openId;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
