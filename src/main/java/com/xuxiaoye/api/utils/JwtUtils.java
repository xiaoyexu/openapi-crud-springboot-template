package com.xuxiaoye.api.utils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;

import com.xuxiaoye.api.bean.TokenPair;
import com.xuxiaoye.api.common.exceptions.InternalServerErrorException;
import com.xuxiaoye.api.common.exceptions.InvalidJWTException;
import com.xuxiaoye.api.common.exceptions.JWTExpiredException;

@Log4j2
public class JwtUtils {

    public static final String ISSUER = "";

    private JwtUtils() {
    }

    public static String generateJWTToken(PrivateKey privateKey, String subject, Map<String, Object> claims, long seconds) {
        return generateJWTToken(privateKey, claims, seconds, subject, ISSUER);
    }

    public static String generateJWTToken(PrivateKey privateKey, Map<String, Object> claims, long seconds, String subject, String issuer) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + seconds * 1000))
                .issuer(issuer)
                .claims(claims)
                .signWith(privateKey)
                .compact();
    }

    public static String generateJWTToken(byte[] privateKeyBytes, String subject, Map<String, Object> claims, long seconds) {
        try {
            PrivateKey privateKey = getPriKey(privateKeyBytes);
            return generateJWTToken(privateKey, subject, claims, seconds);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InternalServerErrorException("Generate JWT Failed");
        }
    }

    public static String generateJWTToken(byte[] privateKeyBytes, String subject, Map<String, Object> claims) {
        try {
            PrivateKey privateKey = getPriKey(privateKeyBytes);
            return generateJWTToken(privateKey, subject, claims, 60);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InternalServerErrorException("Generate JWT Failed");
        }
    }

    public static TokenPair generateJWTTokenPair(byte[] privateKeyBytes, long accessTokenExpireSeconds, long refreshTokenExpireSeconds, String userId, String username, Map<String, Object> claims) {
        try {
            PrivateKey privateKey = getPriKey(privateKeyBytes);
            return generateJWTTokenPair(privateKey, accessTokenExpireSeconds, refreshTokenExpireSeconds, userId, username, claims);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InternalServerErrorException("Generate JWT Failed");
        }
    }

    public static TokenPair generateJWTTokenPair(PrivateKey privateKey, long accessTokenExpireSeconds, long refreshTokenExpireMins, String userId, String username, Map<String, Object> claims) {
        String accessToken = generateJWTToken(privateKey, claims, accessTokenExpireSeconds, userId, ISSUER);
        String freshToken = generateJWTToken(privateKey, claims, refreshTokenExpireMins, userId, ISSUER);
        return new TokenPair(accessToken, freshToken);
    }

    public static Claims validateJWTToken(String token, byte[] publicKeyBytes) throws JWTExpiredException, InvalidJWTException {
        try {
            PublicKey publicKey = getPubKey(publicKeyBytes);
            return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.error(e.getLocalizedMessage());
            throw new JWTExpiredException("JWT Expired");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new InvalidJWTException("Invalid JWT value");
        }
    }

    public static PublicKey getPubKey(byte[] bytes)
            throws Exception {
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static PrivateKey getPriKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static String getSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes("UTF-8"));

            // Convert byte array into hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
