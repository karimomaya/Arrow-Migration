package com.asset.migration.util;

import java.io.File;
import java.io.IOException;

public interface IJsonAdapter {
    public <T> T convert(File file, Class clazz) throws IOException;
    public <T> String writeValueAsString(T object);
}
