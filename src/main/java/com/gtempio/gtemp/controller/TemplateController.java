package com.gtempio.gtemp.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtempio.gtemp.entity.Template;
import com.gtempio.gtemp.service.TemplateService;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService){
        this.templateService = templateService;
    }

    @PostMapping("/templates")
    public Template createTemplate(@RequestBody Template template){
        return templateService.createTemplate(template);
    }
}
