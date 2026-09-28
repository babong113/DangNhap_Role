package com.bteam.badmintonmanagement.service;

import com.bteam.badmintonmanagement.dto.request.CourtRequest;
import com.bteam.badmintonmanagement.entity.court.Court;
import com.bteam.badmintonmanagement.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {
    private final CourtRepository courtRepository;

    public List<Court> getAllCourt()
    {
        List<Court> courts=courtRepository.findAll();
        return courts;
    }

    public void addCourt(CourtRequest request)
    {
        Court court= Court.builder()
                        .name(request.getName())
                        .courtType(String.valueOf(request.getCourtType()))
                        .status(String.valueOf(request.getStatus())).build();
        courtRepository.save(court);
    }
}
