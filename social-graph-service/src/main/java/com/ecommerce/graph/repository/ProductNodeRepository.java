package com.ecommerce.graph.repository;

import com.ecommerce.graph.node.ProductNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductNodeRepository extends Neo4jRepository<ProductNode, String> {

    // 1. Collaborative Filtering: "Customers who bought product X also bought Y"
    @Query("MATCH (p1:Product {id: $productId})<-[:PURCHASED]-(u:User)-[:PURCHASED]->(p2:Product) " +
           "WHERE p1 <> p2 " +
           "RETURN p2, count(u) AS commonBuyers " +
           "ORDER BY commonBuyers DESC " +
           "LIMIT $limit")
    List<ProductNode> findFrequentlyBoughtTogether(@Param("productId") String productId, @Param("limit") int limit);

    // 2. Social Influence Recommendations: Products bought by users you follow
    @Query("MATCH (u:User {id: $userId})-[:FOLLOWS]->(friend:User)-[:PURCHASED]->(p:Product) " +
           "WHERE NOT (u)-[:PURCHASED]->(p) " +
           "RETURN p, count(friend) AS friendPurchases " +
           "ORDER BY friendPurchases DESC " +
           "LIMIT $limit")
    List<ProductNode> findRecommendedByFriends(@Param("userId") String userId, @Param("limit") int limit);
}