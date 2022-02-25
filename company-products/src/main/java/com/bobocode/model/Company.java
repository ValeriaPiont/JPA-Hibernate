package com.bobocode.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Setter(value = AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "company")
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
        product.setCompany(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setCompany(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id.equals(company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
