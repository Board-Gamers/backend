package com.a404.boardgamers.Util;

import com.a404.boardgamers.Security.jwt.AuthConstants;
import com.a404.boardgamers.Security.jwt.TokenUtils;
import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

public class TokenToId {
    public static String getId(HttpServletRequest httpRequest) {
        String header = httpRequest.getHeader(AuthConstants.AUTH_HEADER);
        if (header == null) {
            return null;
        }
        String token = TokenUtils.getTokenFromHeader(header);
        Claims claims = TokenUtils.getClaimsFormToken(token);
        String userId = (String) claims.get("id");
        return userId;
    }
}