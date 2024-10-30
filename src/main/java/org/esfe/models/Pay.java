package org.esfe.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity(name = "pay")
public class Pay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Date paydate;

    @Column(nullable = false)
    private Time paytime;

    @Column(nullable = false)
    private String nameclient;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // Definir el mapa con Product como clave
    @ElementCollection
    @CollectionTable(name = "pay_product_quantities", joinColumns = @JoinColumn(name = "pay_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> productQuantities = new HashMap<>();

    @ManyToMany(fetch = FetchType.EAGER) // Usa EAGER para cargar siempre los productos junto con el pay
    @JoinTable(
            name = "pay_product",
            joinColumns = @JoinColumn(name = "pay_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    // Constructor, getters, setters...


    public Pay() {
    }

    // Método para calcular el total del pago
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            BigDecimal precioUnitario = entry.getKey().getPrice();
            Integer cantidad = entry.getValue();
            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
            total = total.add(subtotal);
        }
        return total;
    }

    public Pay(Integer id, Date paydate, Time paytime, String nameclient, User user, List<Product> products, Map<Product, Integer> productQuantities) {
        this.id = id;
        this.paydate = paydate;
        this.paytime = paytime;
        this.nameclient = nameclient;
        this.user = user;
        this.products = products;
        this.productQuantities = productQuantities;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getPaydate() {
        return paydate;
    }

    public void setPaydate(Date paydate) {
        this.paydate = paydate;
    }

    public Time getPaytime() {
        return paytime;
    }

    public void setPaytime(Time paytime) {
        this.paytime = paytime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNameclient() {
        return nameclient;
    }

    public void setNameclient(String nameclient) {
        this.nameclient = nameclient;
    }

    public void setProductQuantities(Map<Product, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // parte del codigo que me permite guardar la cantidad
    public Map<Product, Integer> getProductQuantities() {
        return productQuantities;
    }
}
