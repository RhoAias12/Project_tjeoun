package com.tjoeun.controller;

import com.tjoeun.service.MyPageService;
import com.tjoeun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class FavoriteController {

    @Autowired
    private UserService userService;

    @Autowired
    private MyPageService myPageService;

    @PostMapping("/favorite/toggle")
    @ResponseBody
    public ResponseEntity<String> toggleFavorite(@RequestParam Long recruitmentId, Principal principal) {
        String userEmail = principal.getName();
        boolean isFavorited = myPageService.toggleFavorite(userEmail, recruitmentId);
        return ResponseEntity.ok(isFavorited ? "favorited" : "unfavorited");
    }

}
