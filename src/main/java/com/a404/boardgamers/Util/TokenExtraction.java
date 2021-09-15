package com.a404.boardgamers.Util;

import com.a404.boardgamers.Config.AuthConstants;
import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;

public class TokenExtraction {
    public static String check(HttpServletRequest httpRequest) {
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
