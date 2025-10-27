package com.gtempio.gtemp.service;

import org.springframework.stereotype.Service;
import com.gtempio.gtemp.entity.Template;
import com.gtempio.gtemp.repository.TemplateRepository;

@Service
public class TemplateService {
    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository){
        this.templateRepository = templateRepository;
    }

    public Template createTemplate(Template template){
        return templateRepository.save(template);
    }
}
