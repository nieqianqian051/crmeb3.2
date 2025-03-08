package com.maijishop.crmeb.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author maijishop
 */
@Data
@Component
@ConfigurationProperties(prefix = "maijishop.jwt")
public class JwtUtil {

    /**
     * 密钥
     */
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    private long expire;

    /**
     * token请求头名称
     */
    private String header;

    /**
     * 创建JWT
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT
     */
    public String createToken(String userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(userId)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 创建JWT
     *
     * @param userId 用户ID
     * @param claims 自定义信息
     * @return JWT
     */
    public String createToken(String userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT
     *
     * @param token JWT
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户ID
     *
     * @param token JWT
     * @return 用户ID
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取用户名
     *
     * @param token JWT
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 获取自定义信息
     *
     * @param token JWT
     * @param key   key
     * @return 自定义信息
     */
    public Object getClaim(String token, String key) {
        Claims claims = parseToken(token);
        return claims.get(key);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            return !parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
} 