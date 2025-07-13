package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.elasticsearch.document.UserDocument;
import com.tjoeun.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserSyncService {

  private final ElasticsearchClient esClient;

  public void indexUserToES(Users user) throws IOException {
//    UserDocument doc = UserDocument.builder()
//      .userIdx(user.getUserIdx())
//      .userName(user.getUserName())
//      .userEmail(user.getUserEmail())
//      .userNickname(user.getUserNickname())
//      .userBirth(user.getUserBirth())
//      .userCreatedAt(user.getUserCreatedAt())
//      .userRole(user.getUserRole().name())
//      .build();
//
//    esClient.index(i -> i
//      .index("users")
//      .id(String.valueOf(user.getUserIdx()))
//      .document(doc)
//    );
  }
}

