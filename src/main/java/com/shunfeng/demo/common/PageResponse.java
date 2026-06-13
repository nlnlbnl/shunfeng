package com.shunfeng.demo.common;

import java.util.List;

public record PageResponse<T>(int page, int pageSize, long total, List<T> records) {
}
