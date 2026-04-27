package com.recruit.c360.repository;
import com.recruit.c360.entity.RawSourceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface RawSourceDataRepository extends JpaRepository<RawSourceData, UUID> {
    Optional<RawSourceData> findByCandidateIdAndSource(UUID candidateId, String source);
    List<RawSourceData> findByCandidateId(UUID candidateId);
    @Modifying @Transactional
    @Query("UPDATE RawSourceData r SET r.stale = true WHERE r.candidate.id = :candidateId")
    void markAllStaleForCandidate(UUID candidateId);
}
