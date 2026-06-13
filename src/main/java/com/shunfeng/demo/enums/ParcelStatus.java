package com.shunfeng.demo.enums;

public enum ParcelStatus {
    WAITING_PICKUP("待取件"),
    PICKED_UP("已取件");

    private final String text;

    ParcelStatus(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
