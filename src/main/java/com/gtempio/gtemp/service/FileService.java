package com.gtempio.gtemp.service;

import org.springframework.stereotype.Service;
import com.gtempio.gtemp.entity.File;
import com.gtempio.gtemp.repository.FileRepository;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public File createFile(File file){
        return fileRepository.save(file);
    }
}
