package org.esfe.services.interfaces;

import org.esfe.models.Rol;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface IRolService {
    List<Rol> findAll();

    List<Rol> findAll(Sort sort);

    List<Rol> getAll();

    Optional<Rol> findOneById(Integer rolId);

    Rol createOrEditOne(Rol rol);

    void deleteOneById(Integer rolId);
}
