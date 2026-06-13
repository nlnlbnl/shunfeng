package com.shunfeng.demo.dto;

public record ParcelPageRequest(
        Integer page,
        Integer pageSize,
        String phone,
        String trackingNo,
        String status
) {
    public int normalizedPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
