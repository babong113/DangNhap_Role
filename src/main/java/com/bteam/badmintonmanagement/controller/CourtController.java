package com.bteam.badmintonmanagement.controller;

import com.bteam.badmintonmanagement.dto.request.CourtRequest;
import com.bteam.badmintonmanagement.dto.response.ApiResponse;
import com.bteam.badmintonmanagement.service.CourtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/court")
@RequiredArgsConstructor
public class CourtController {
    private final CourtService courtService;

    @GetMapping("/allcourt")
    public ResponseEntity<ApiResponse<?>> getAllCourt()
    {
        ApiResponse<?> response=ApiResponse.builder()
                .success(true)
                .message("Danh sách các sân")
                .data(courtService.getAllCourt())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addCourt")
    public  ResponseEntity<ApiResponse<?>> addCourt(
            @Valid
            @RequestBody CourtRequest request
            )
    {
        courtService.addCourt(request);
        ApiResponse<?> response=ApiResponse.builder()
                .success(true)
                .message("Thêm sân thành công")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
