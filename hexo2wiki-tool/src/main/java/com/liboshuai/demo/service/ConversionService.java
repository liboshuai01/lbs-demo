package com.liboshuai.demo.service;

import java.io.IOException;

public interface ConversionService {
    long processDirectory(String sourceDirectoryPath, String targetDirectoryPath) throws IOException;
}
