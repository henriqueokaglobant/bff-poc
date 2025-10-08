package com.globant.study.controller;

import com.globant.study.dto.ScreenDTO;
import com.globant.study.service.DynamicScreenService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.in;

@RestController
public class DynamicScreenController {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    DynamicScreenService dynamicScreenService;

    @GetMapping("/screen")
    public List<ScreenDTO> getScreenJson(@RequestParam String template, @RequestParam String license, @RequestParam String role) {
//        return new ResponseEntity<>(dynamicScreenService.calculateScreenJson(template, license, role), HttpStatus.OK);
        return dynamicScreenService.calculateScreenJson(template, license, role);
    }
}
