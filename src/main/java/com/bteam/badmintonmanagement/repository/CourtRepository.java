package com.bteam.badmintonmanagement.repository;

import com.bteam.badmintonmanagement.entity.court.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends JpaRepository<Court,Long> {
}
