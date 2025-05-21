package com.ruoyi.wxmini.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.ruoyi.wxmini.service.IWxMiniJwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author weijiayu
 * @date 2025/4/22 22:09
 */
@Service
@Slf4j
public class WxMiniJwtServiceImpl implements IWxMiniJwtService {

    private static final String JWT_KEY_USER_ID = "userId";

    // jwt密钥
    @Value("${token.secret:asd}")
    private String key;
    // jwt有效期。单位分钟
    @Value("${token.expireTime:60}")
    private int expireTime;

    @Override
    public String createToken(String userId) {
        Map<String, Object> payload = new HashMap<>();
//        payload.put(JWTPayload.SUBJECT, userInfo);
        payload.put(JWT_KEY_USER_ID, userId);
        /**
         * @see cn.hutool.jwt.JWTValidator#validateDate(JWTPayload, Date, long)
         */
        payload.put(JWTPayload.EXPIRES_AT, DateTime.now().offset(DateField.MINUTE, expireTime));
        return JWTUtil.createToken(payload, key.getBytes());
    }

    @Override
    public Boolean verifyToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(key.getBytes());
        // 校验有效期和签名
//        return jwt.verify();
        return jwt.validate(0);
    }

    @Override
    public String parseUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object obj = jwt.getPayload(JWT_KEY_USER_ID);
        return obj == null ? "" : obj.toString();
    }
}
