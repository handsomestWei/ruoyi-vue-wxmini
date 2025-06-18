package com.ruoyi.wxmini.service;

/**
 * @author weijiayu
 * @date 2025/4/22 22:05
 */
public interface IWxMiniJwtService {

    String createToken(String userId);

    Boolean verifyToken(String token);

    String parseUserId(String token);
}
