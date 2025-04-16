package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.ReportedComments;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ReportedComments entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReportedCommentsRepository extends JpaRepository<ReportedComments, Long>, JpaSpecificationExecutor<ReportedComments> {}
