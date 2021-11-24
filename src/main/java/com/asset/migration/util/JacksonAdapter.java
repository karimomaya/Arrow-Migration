package com.asset.migration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JacksonAdapter implements IJsonAdapter{

    @Override
    public <T> T convert(File file, Class clazz) throws IOException {
        // create object mapper instance
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.readValue(file, clazz);
    }

    @Override
    public <T> String writeValueAsString(T object){
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
