package com.ruoyi.wxmini.util;

/**
 * 微信小程序用户上下文处理器
 *
 * @author weijiayu
 * @date 2025/4/22 23:57
 */
public class WxMiniUserContext {

    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(String userId) {
        currentUser.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return currentUser.get();
    }

    /**
     * 清除当前用户 ID
     */
    public static void clear() {
        currentUser.remove();
    }
}
