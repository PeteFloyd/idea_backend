package com.learn.demo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ImagesConverter implements AttributeConverter<List<String>, String> {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<String> images) {
		if (images == null || images.isEmpty()) {
			return "[]";
		}
		try {
			return mapper.writeValueAsString(images);
		} catch (JsonProcessingException e) {
			return "[]";
		}
	}

	@Override
	public List<String> convertToEntityAttribute(String json) {
		if (json == null || json.isBlank()) {
			return new ArrayList<>();
		}
		try {
			return mapper.readValue(json, new TypeReference<>() {});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		}
	}
}
