package com.recruit.c360.repository;
import com.recruit.c360.entity.ProfileScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface ProfileScoreRepository extends JpaRepository<ProfileScore, UUID> {
    List<ProfileScore> findByProfileId(UUID profileId);
}
