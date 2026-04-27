package com.recruit.c360.repository;
import com.recruit.c360.entity.RedFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface RedFlagRepository extends JpaRepository<RedFlag, UUID> {
    List<RedFlag> findByProfileId(UUID profileId);
}
