package com.example.coffee.converters;

import com.example.coffee.services.ResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonConverter implements Converter<ResponseService, String> {
    @Override
    public String convert(ResponseService t) {
        try {
            return new ObjectMapper().writeValueAsString(t);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "{}";
    }
}
