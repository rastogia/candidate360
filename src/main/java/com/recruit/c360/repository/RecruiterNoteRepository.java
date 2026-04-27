package com.recruit.c360.repository;
import com.recruit.c360.entity.RecruiterNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface RecruiterNoteRepository extends JpaRepository<RecruiterNote, UUID> {
    List<RecruiterNote> findByProfileIdOrderByCreatedAtDesc(UUID profileId);
}
