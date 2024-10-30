package org.esfe.services.implement;

import org.esfe.models.Rol;
import org.esfe.repository.RolRepository;
import org.esfe.services.interfaces.IRolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolService  implements IRolService {

    @Autowired
    private RolRepository rolRepository;

    @Override
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Override
    public List<Rol> findAll(Sort sort) {
        return rolRepository.findAll(sort);
    }


    @Override
    public List<Rol> getAll() {
        return rolRepository.findAll();
    }

    @Override
    public Optional<Rol> findOneById(Integer rolId) {
        return rolRepository.findById(rolId);
    }

    @Override
    public Rol createOrEditOne(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    public void deleteOneById(Integer rolId) {
        rolRepository.deleteById(rolId);
    }
}
