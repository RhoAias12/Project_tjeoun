package com.tjoeun.elasticsearch.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.elasticsearch.document.UserDocument;
import com.tjoeun.elasticsearch.repository.UserDocumentRepository;
import com.tjoeun.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserSyncService {

  private final UserDocumentRepository userDocumentRepository;

  public void save(Users user) {
    UserDocument doc = UserDocument.fromEntity(user); // 변환 로직 필요
    userDocumentRepository.save(doc);
  }

  public void deleteById(Long userIdx) {
    userDocumentRepository.deleteById(userIdx.intValue());
  }
}


