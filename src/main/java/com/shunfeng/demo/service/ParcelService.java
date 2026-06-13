package com.shunfeng.demo.service;

import com.shunfeng.demo.common.PageResponse;
import com.shunfeng.demo.dto.ParcelCreateRequest;
import com.shunfeng.demo.dto.ParcelPageRequest;
import com.shunfeng.demo.dto.ParcelResponse;

import java.util.List;

public interface ParcelService {
    ParcelResponse create(ParcelCreateRequest request);

    List<ParcelResponse> listWaitingByPhone(String phone);

    ParcelResponse pickup(Long id);

    PageResponse<ParcelResponse> page(ParcelPageRequest request);
}
