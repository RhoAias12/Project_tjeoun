package com.tjoeun.converter;

import com.tjoeun.entity.ApplyHistory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApplyStatusConverter implements AttributeConverter<ApplyHistory.ApplyStatus, String> {

  @Override
  public String convertToDatabaseColumn(ApplyHistory.ApplyStatus status) {
    return status != null ? status.getDisplay() : null;
  }

  @Override
  public ApplyHistory.ApplyStatus convertToEntityAttribute(String dbData) {
    return dbData != null ? ApplyHistory.ApplyStatus.fromDisplay(dbData) : null;
  }
}