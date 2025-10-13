package com.globant.study.controller;

import com.globant.study.dto.ScreenComponentDTO;
import com.globant.study.service.DynamicScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class DynamicScreenController {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    DynamicScreenService dynamicScreenService;

    @GetMapping("/screen")
    public List<ScreenComponentDTO> getScreenJson(@RequestParam String template, @RequestParam String user) {
        return dynamicScreenService.calculateScreenJson(template, user);
    }
}
