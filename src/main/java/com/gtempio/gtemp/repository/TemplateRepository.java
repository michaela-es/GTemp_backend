package com.gtempio.gtemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtempio.gtemp.entity.Template;

@Repository
public interface TemplateRepository extends JpaRepository <Template, Long> {
}
