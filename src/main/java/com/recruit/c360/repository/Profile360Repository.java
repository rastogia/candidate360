package com.recruit.c360.repository;
import com.recruit.c360.entity.Profile360;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface Profile360Repository extends JpaRepository<Profile360, UUID> {
    Optional<Profile360> findByCandidateIdAndJobRoleId(UUID candidateId, UUID jobRoleId);
    @Query("SELECT p FROM Profile360 p WHERE p.jobRole.id = :jobRoleId AND p.status = 'COMPLETE' ORDER BY p.createdAt DESC")
    List<Profile360> findCompleteProfilesByRole(UUID jobRoleId);
}
