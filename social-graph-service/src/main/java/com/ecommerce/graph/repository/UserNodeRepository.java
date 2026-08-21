package com.ecommerce.graph.repository;

import com.ecommerce.graph.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {

    // Find all users who follow the given user
    @Query("MATCH (follower:User)-[:FOLLOWS]->(u:User {id: $userId}) RETURN follower.id")
    List<String> findFollowerIds(@Param("userId") String userId);
}