package com.recruit.c360.repository;
import com.recruit.c360.entity.CandidateHandle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface CandidateHandleRepository extends JpaRepository<CandidateHandle, UUID> {
    List<CandidateHandle> findByCandidateId(UUID candidateId);
    List<CandidateHandle> findByCandidateIdAndConfirmedTrue(UUID candidateId);
}
