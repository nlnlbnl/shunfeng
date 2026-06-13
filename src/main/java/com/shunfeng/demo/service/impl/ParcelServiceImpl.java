package com.shunfeng.demo.service.impl;

import com.shunfeng.demo.common.PageResponse;
import com.shunfeng.demo.dto.ParcelCreateRequest;
import com.shunfeng.demo.dto.ParcelPageRequest;
import com.shunfeng.demo.dto.ParcelResponse;
import com.shunfeng.demo.entity.Parcel;
import com.shunfeng.demo.enums.ParcelStatus;
import com.shunfeng.demo.exception.BusinessException;
import com.shunfeng.demo.mapper.ParcelMapper;
import com.shunfeng.demo.service.ParcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParcelServiceImpl implements ParcelService {
    private final ParcelMapper parcelMapper;

    public ParcelServiceImpl(ParcelMapper parcelMapper) {
        this.parcelMapper = parcelMapper;
    }

    @Override
    public ParcelResponse create(ParcelCreateRequest request) {
        String trackingNo = request.trackingNo().trim();
        parcelMapper.findByTrackingNo(trackingNo).ifPresent(parcel -> {
            throw new BusinessException(4001, "运单号已存在");
        });

        Parcel parcel = new Parcel();
        parcel.setTrackingNo(trackingNo);
        parcel.setRecipientPhone(request.recipientPhone().trim());
        parcel.setExpressCompany(request.expressCompany().trim());
        parcel.setShelfLocation(request.shelfLocation().trim());
        parcel.setStatus(ParcelStatus.WAITING_PICKUP.name());
        parcel.setInboundTime(LocalDateTime.now());

        return toResponse(parcelMapper.insert(parcel));
    }

    @Override
    public List<ParcelResponse> listWaitingByPhone(String phone) {
        return parcelMapper.findWaitingByPhone(phone.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ParcelResponse pickup(Long id) {
        Parcel parcel = parcelMapper.findById(id)
                .orElseThrow(() -> new BusinessException(4040, "包裹不存在"));
        if (ParcelStatus.PICKED_UP.name().equals(parcel.getStatus())) {
            throw new BusinessException(4001, "包裹已取件");
        }
        Parcel updated = parcelMapper.updatePickup(id, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(4001, "包裹已取件"));
        return toResponse(updated);
    }

    @Override
    public PageResponse<ParcelResponse> page(ParcelPageRequest request) {
        long total = parcelMapper.count(request);
        List<ParcelResponse> records = parcelMapper.page(request).stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(request.normalizedPage(), request.normalizedPageSize(), total, records);
    }

    private ParcelResponse toResponse(Parcel parcel) {
        ParcelStatus status = ParcelStatus.valueOf(parcel.getStatus());
        return new ParcelResponse(
                parcel.getId(),
                parcel.getTrackingNo(),
                parcel.getRecipientPhone(),
                parcel.getExpressCompany(),
                parcel.getShelfLocation(),
                parcel.getStatus(),
                status.text(),
                parcel.getInboundTime(),
                parcel.getOutboundTime(),
                isOverdue(parcel)
        );
    }

    private boolean isOverdue(Parcel parcel) {
        return ParcelStatus.WAITING_PICKUP.name().equals(parcel.getStatus())
                && parcel.getInboundTime().plusHours(48).isBefore(LocalDateTime.now());
    }
}
