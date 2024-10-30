package org.esfe.repository;

import org.esfe.models.Rol;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository  extends JpaRepository<Rol, Integer> {
    List<Rol> findAll(Sort sort);
}
