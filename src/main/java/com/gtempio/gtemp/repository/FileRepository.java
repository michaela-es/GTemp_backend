package com.gtempio.gtemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtempio.gtemp.entity.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
