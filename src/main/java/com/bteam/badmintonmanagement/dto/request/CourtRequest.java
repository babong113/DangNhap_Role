package com.bteam.badmintonmanagement.dto.request;

import com.bteam.badmintonmanagement.entity.court.CourtStatus;
import com.bteam.badmintonmanagement.entity.court.CourtType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourtRequest {
    @NotBlank(message = "Tên sân không được để trống")
    @Size(max = 50, message = "Tên sân không được vượt quá 50 ký tự")
    private String name;

    @NotNull(message = "Loại sân không được để trống")
    private CourtType courtType;

    @NotNull(message = "Trạng thái không được để trống")
    private CourtStatus status;
}
