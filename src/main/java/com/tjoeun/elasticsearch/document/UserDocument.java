package com.tjoeun.elasticsearch.document;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "users")
public class UserDocument {
  @Id
  private Integer userIdx;

  @Field(type = FieldType.Text)
  private String userName;

  @Field(type = FieldType.Keyword)
  private String userEmail;

  @Field(type = FieldType.Keyword)
  private String userNickname;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
  private  Long userBirth;

  @Field(type = FieldType.Keyword)
  private String userRole;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private java.sql.Timestamp userCreatedAt;
}

