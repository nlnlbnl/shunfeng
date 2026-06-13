package com.shunfeng.demo.controller;

import com.shunfeng.demo.common.ApiResponse;
import com.shunfeng.demo.common.PageResponse;
import com.shunfeng.demo.dto.ParcelCreateRequest;
import com.shunfeng.demo.dto.ParcelPageRequest;
import com.shunfeng.demo.dto.ParcelResponse;
import com.shunfeng.demo.enums.ParcelStatus;
import com.shunfeng.demo.exception.BusinessException;
import com.shunfeng.demo.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ApiResponse<ParcelResponse> create(@Valid @RequestBody ParcelCreateRequest request) {
        return ApiResponse.success(parcelService.create(request));
    }

    @GetMapping("/waiting")
    public ApiResponse<List<ParcelResponse>> waiting(@RequestParam String phone) {
        validatePhone(phone);
        return ApiResponse.success(parcelService.listWaitingByPhone(phone));
    }

    @PutMapping("/{id}/pickup")
    public ApiResponse<ParcelResponse> pickup(@PathVariable Long id) {
        return ApiResponse.success(parcelService.pickup(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<ParcelResponse>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String trackingNo,
            @RequestParam(required = false) String status) {
        validatePage(page, pageSize);
        validateStatus(status);
        return ApiResponse.success(parcelService.page(new ParcelPageRequest(page, pageSize, phone, trackingNo, status)));
    }

    private void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(4000, "手机号格式错误");
        }
    }

    private void validatePage(Integer page, Integer pageSize) {
        if ((page != null && page < 1) || (pageSize != null && (pageSize < 1 || pageSize > 100))) {
            throw new BusinessException(4000, "分页参数错误");
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        try {
            ParcelStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(4000, "包裹状态不合法");
        }
    }
}
