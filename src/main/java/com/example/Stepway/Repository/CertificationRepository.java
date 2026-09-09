package com.example.Stepway.Repository;


import com.example.Stepway.Domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository  extends JpaRepository<Certification,Long> {


    @Query(value = "SELECT * FROM certification c " +
            "where user_id = :userId",
            nativeQuery = true)
    List<Certification> getCertificationsByUserId(Long userId);


    @Query(value = "SELECT COUNT(c.id) FROM certification c " +
            "WHERE c.user_id = :userId",
            nativeQuery = true)
    Long countUsersWithRoleStudent(Long userId);

    @Query(value = "SELECT COUNT(c.id) FROM certification c ",nativeQuery = true)
    Long countTotalCertifications();



}
