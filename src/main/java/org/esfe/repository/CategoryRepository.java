package org.esfe.repository;

import org.esfe.models.Category;
import org.esfe.models.Rol;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository  extends JpaRepository<Category, Integer> {
    List<Category> findAll(Sort sort);
}
