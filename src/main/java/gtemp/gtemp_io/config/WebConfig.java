package gtemp.gtemp_io.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // For development - serve from both absolute path and content root
        String projectRoot = System.getProperty("user.dir");
        String uploadsAbsolutePath = "file:" + projectRoot + "/";

        System.out.println("Configuring static resources from project root: " + projectRoot);
        System.out.println("Uploads should be at: " + uploadsAbsolutePath + "uploads/");

        // This will serve files from /uploads/ relative to your project root
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/", "file:uploads/", uploadsAbsolutePath + "uploads/");
    }
}