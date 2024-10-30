package org.esfe.services.implement;

import org.esfe.models.Product;
import org.esfe.models.Rol;
import org.esfe.models.User;
import org.esfe.repository.UserRepository;
import org.esfe.services.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User findById(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null); // Devuelve null si no se encuentra el usuario
    }
    @Override
    public List<User> getUsersByStatus(int status) {
        return userRepository.findByStatus(status);
    }


    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }


    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findOneById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User createOrEditOne(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteOneById(Integer userId) {
        userRepository.deleteById(userId);
    }



    @Override
    public Page<User> searchUsers(String name, String lastname,Integer rolId, Integer status, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("name"), "%" + name + "%"));
        }
        if (lastname != null && !lastname.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("lastname"), "%" + lastname + "%"));
        }

        if (rolId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rol").get("id"), rolId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return userRepository.findAll(spec, pageable);
    }

}
