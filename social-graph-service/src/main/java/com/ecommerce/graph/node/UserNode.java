package com.ecommerce.graph.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Node("User")
public class UserNode {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("email")
    private String email;

    @Builder.Default
    @ToString.Exclude
    @JsonIgnoreProperties("purchasedProducts")
    @Relationship(type = "PURCHASED", direction = Relationship.Direction.OUTGOING)
    private Set<ProductNode> purchasedProducts = new HashSet<>();

    @Builder.Default
    @ToString.Exclude
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