package com.ecommerce.graph.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Node("User")
public class UserNode {

    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    private String id;

    @Property("name")
    @ToString.Include
    private String name;

    @Property("email")
    @ToString.Include
    private String email;

    @Builder.Default
    @JsonIgnoreProperties("purchasedProducts")
    @Relationship(type = "PURCHASED", direction = Relationship.Direction.OUTGOING)
    private Set<ProductNode> purchasedProducts = new HashSet<>();

    @Builder.Default
    @JsonIgnoreProperties({"following", "purchasedProducts"})
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<UserNode> following = new HashSet<>();

    public void purchase(ProductNode product) {
        if (purchasedProducts == null) {
            purchasedProducts = new HashSet<>();
        }
        purchasedProducts.add(product);
    }

    public void follow(UserNode user) {
        if (following == null) {
            following = new HashSet<>();
        }
        following.add(user);
    }
}