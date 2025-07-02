package com.tjoeun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FavoriteController {

    @Autowired

    @PostMapping("/favorite/toggle/{recruitmentId}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long recruitmentId,
                                                  @AuthenticationPrincipal CustomUser user) {
        boolean result = favoriteService.toggleFavorite(user.getId(), recruitmentId);
        return ResponseEntity.ok(result);
    }
}
