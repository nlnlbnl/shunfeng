package com.shunfeng.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ParcelResponse(
        Long id,
        String trackingNo,
        String recipientPhone,
        String expressCompany,
        String shelfLocation,
        String status,
        String statusText,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime inboundTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime outboundTime,
        boolean overdue
) {
}
