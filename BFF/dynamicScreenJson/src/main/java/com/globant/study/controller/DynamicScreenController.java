package com.globant.study.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.lang.System.in;

@RestController
public class DynamicScreenController {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @GetMapping("/screen")
    public ResponseEntity<String> getScreenJson(@RequestParam String filename) {
        String responseBody = "";
        HttpStatus responseStatus = HttpStatus.OK;

        try {
            File file = ResourceUtils.getFile("classpath:screen/" + filename + ".json");
            InputStream in = new FileInputStream(file);
            responseBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            responseBody = "File not found";
            LOGGER.severe(e.getMessage());
        }
        return new ResponseEntity<>(responseBody, responseStatus);
    }
}
