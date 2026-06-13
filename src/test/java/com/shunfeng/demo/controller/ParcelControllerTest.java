package com.shunfeng.demo.controller;

import com.shunfeng.demo.enums.ParcelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS parcel");
        jdbcTemplate.execute("""
                CREATE TABLE parcel (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  tracking_no VARCHAR(64) NOT NULL,
                  recipient_phone VARCHAR(20) NOT NULL,
                  express_company VARCHAR(64) NOT NULL,
                  shelf_location VARCHAR(64) NOT NULL,
                  status VARCHAR(32) NOT NULL DEFAULT 'WAITING_PICKUP',
                  inbound_time TIMESTAMP NOT NULL,
                  outbound_time TIMESTAMP NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  UNIQUE KEY uk_tracking_no (tracking_no)
                )
                """);
    }

    @Test
    void createReturnsCreatedParcel() throws Exception {
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trackingNo": "SF123456789",
                                  "recipientPhone": "13800138000",
                                  "expressCompany": "顺丰",
                                  "shelfLocation": "A-01-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.trackingNo").value("SF123456789"))
                .andExpect(jsonPath("$.data.status").value("WAITING_PICKUP"))
                .andExpect(jsonPath("$.data.statusText").value("待取件"));
    }

    @Test
    void createRejectsInvalidPhone() throws Exception {
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trackingNo": "SF123456789",
                                  "recipientPhone": "123",
                                  "expressCompany": "顺丰",
                                  "shelfLocation": "A-01-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4000))
                .andExpect(jsonPath("$.message").value("手机号格式错误"));
    }

    @Test
    void createRejectsBlankRequiredField() throws Exception {
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trackingNo": "",
                                  "recipientPhone": "13800138000",
                                  "expressCompany": "顺丰",
                                  "shelfLocation": "A-01-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4000))
                .andExpect(jsonPath("$.message").value("运单号不能为空"));
    }

    @Test
    void waitingLookupReturnsOnlyWaitingParcels() throws Exception {
        insertParcel("SF100000001", "13800138000", "顺丰", "A-01-01",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(2), null);
        insertParcel("JD100000001", "13800138000", "京东", "B-02-01",
                ParcelStatus.PICKED_UP.name(), LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));

        mockMvc.perform(get("/api/parcels/waiting").param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].trackingNo").value("SF100000001"));
    }

    @Test
    void waitingLookupReturnsEmptyListForPhoneWithoutParcels() throws Exception {
        mockMvc.perform(get("/api/parcels/waiting").param("phone", "13700137000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void pickupReturnsPickedUpParcel() throws Exception {
        long id = insertParcel("SF100000001", "13800138000", "顺丰", "A-01-01",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(2), null);

        mockMvc.perform(put("/api/parcels/{id}/pickup", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"))
                .andExpect(jsonPath("$.data.outboundTime").isNotEmpty());
    }

    @Test
    void createFormatsTimesWithSpaceSeparator() throws Exception {
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trackingNo": "SF123456789",
                                  "recipientPhone": "13800138000",
                                  "expressCompany": "顺丰",
                                  "shelfLocation": "A-01-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inboundTime", containsString(" ")))
                .andExpect(jsonPath("$.data.inboundTime", not(containsString("T"))));
    }

    @Test
    void pageReturnsOverdueFlag() throws Exception {
        insertParcel("YT100000001", "13800138000", "圆通", "A-01-02",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(50), null);

        mockMvc.perform(get("/api/parcels").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].overdue").value(true));
    }

    @Test
    void pageRejectsInvalidPage() throws Exception {
        mockMvc.perform(get("/api/parcels").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4000))
                .andExpect(jsonPath("$.message").value("分页参数错误"));
    }

    @Test
    void pageRejectsInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/parcels").param("status", "LOST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4000))
                .andExpect(jsonPath("$.message").value("包裹状态不合法"));
    }

    private long insertParcel(String trackingNo, String phone, String company, String shelfLocation,
                              String statusValue, LocalDateTime inboundTime, LocalDateTime outboundTime) {
        jdbcTemplate.update("""
                        INSERT INTO parcel
                          (tracking_no, recipient_phone, express_company, shelf_location, status, inbound_time, outbound_time)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                trackingNo, phone, company, shelfLocation, statusValue, inboundTime, outboundTime);
        return jdbcTemplate.queryForObject("SELECT id FROM parcel WHERE tracking_no = ?", Long.class, trackingNo);
    }
}
