package com.a404.boardgamers.Security.config;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationCheck {
    public static AuthorizationCheck object;
    private List<String> tokenRequiredPathList;

    // 토큰이 필요한 url 경로를 싱글톤 패턴으로 관리
    private AuthorizationCheck() {
        tokenRequiredPathList = new ArrayList<>();
        tokenRequiredPathList.add("/user/changepwd");
        tokenRequiredPathList.add("/user/delete");
        tokenRequiredPathList.add("/user/profile/{nickname}");
        tokenRequiredPathList.add("/user/profile");
        tokenRequiredPathList.add("/follow/request");
        tokenRequiredPathList.add("/follow/requestList");
        tokenRequiredPathList.add("/post/upload");
        tokenRequiredPathList.add("/post/main");
        tokenRequiredPathList.add("/place/review");
        tokenRequiredPathList.add("/place/review/image/{reviewId}");
        tokenRequiredPathList.add("/placelist/{listId}");
        tokenRequiredPathList.add("/notice");
    }

    public static AuthorizationCheck getObject() {
        if (object == null) {
            object = new AuthorizationCheck();
        }
        return object;
    }

    public List<String> getPathList() {
        return tokenRequiredPathList;
    }
}