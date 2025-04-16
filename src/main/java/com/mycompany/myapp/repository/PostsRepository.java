package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Posts;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Posts entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {}
