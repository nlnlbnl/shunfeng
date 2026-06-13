package com.shunfeng.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ParcelCreateRequest(
        @NotBlank(message = "运单号不能为空")
        @Size(max = 64, message = "运单号长度不能超过64")
        String trackingNo,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
        String recipientPhone,

        @NotBlank(message = "快递公司不能为空")
        @Size(max = 64, message = "快递公司长度不能超过64")
        String expressCompany,

        @NotBlank(message = "货架位置不能为空")
        @Size(max = 64, message = "货架位置长度不能超过64")
        String shelfLocation
) {
}
