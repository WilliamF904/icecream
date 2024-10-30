package org.esfe.models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45)
    private String rolname;

    @OneToMany(mappedBy = "rol")
    private Set<User> users = new HashSet<>();

    public Rol() {
    }

    public Rol(Integer id, String rolname, Set<User> users) {
        this.id = id;
        this.rolname = rolname;
        this.users = users;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getRolname() {
        return rolname;
    }

    public void setRolname(String rolname) {
        this.rolname = rolname;
    }
}
