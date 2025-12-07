package gtemp.gtemp_io.service;

import gtemp.gtemp_io.repository.TemplateImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateImageService {

    @Autowired
    private TemplateImageRepository templateImageRepository;

    public List<String> getImagePathsByTemplateId(Long templateId) {
        return templateImageRepository.findByTemplateId(templateId)
                .stream()
                .map(templateImage -> {
                    String imagePath = templateImage.getImagePath();

                    imagePath = imagePath.trim();
                    imagePath = imagePath.replaceAll("\\\\", "/");

                    return "http://localhost:8080/" + imagePath;
                })
                .collect(Collectors.toList());
    }
}
