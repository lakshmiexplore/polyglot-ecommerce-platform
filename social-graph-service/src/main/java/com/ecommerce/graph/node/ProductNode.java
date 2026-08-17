package com.ecommerce.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Node("Product")
public class ProductNode {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("category")
    private String category;
}