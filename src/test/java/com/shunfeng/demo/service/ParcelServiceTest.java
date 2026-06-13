package com.shunfeng.demo.service;

import com.shunfeng.demo.dto.ParcelCreateRequest;
import com.shunfeng.demo.dto.ParcelPageRequest;
import com.shunfeng.demo.dto.ParcelResponse;
import com.shunfeng.demo.entity.Parcel;
import com.shunfeng.demo.enums.ParcelStatus;
import com.shunfeng.demo.exception.BusinessException;
import com.shunfeng.demo.mapper.ParcelMapper;
import com.shunfeng.demo.service.impl.ParcelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ParcelServiceTest {

    @Autowired
    private ParcelService parcelService;

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
    void createStoresWaitingParcelWithInboundTime() {
        ParcelResponse response = parcelService.create(new ParcelCreateRequest(
                " SF123456789 ",
                "13800138000",
                "顺丰",
                " A-01-03 "
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.trackingNo()).isEqualTo("SF123456789");
        assertThat(response.recipientPhone()).isEqualTo("13800138000");
        assertThat(response.expressCompany()).isEqualTo("顺丰");
        assertThat(response.shelfLocation()).isEqualTo("A-01-03");
        assertThat(response.status()).isEqualTo(ParcelStatus.WAITING_PICKUP.name());
        assertThat(response.statusText()).isEqualTo("待取件");
        assertThat(response.inboundTime()).isNotNull();
        assertThat(response.outboundTime()).isNull();
        assertThat(response.overdue()).isFalse();
    }

    @Test
    void createRejectsDuplicateTrackingNo() {
        ParcelCreateRequest request = new ParcelCreateRequest("SF123456789", "13800138000", "顺丰", "A-01-03");
        parcelService.create(request);

        assertThatThrownBy(() -> parcelService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("运单号已存在")
                .extracting("code")
                .isEqualTo(4001);
    }

    @Test
    void listWaitingByPhoneReturnsOnlyWaitingParcels() {
        long waitingId = insertParcel("SF100000001", "13800138000", "顺丰", "A-01-01",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(2), null);
        insertParcel("JD100000001", "13800138000", "京东", "B-02-01",
                ParcelStatus.PICKED_UP.name(), LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));

        List<ParcelResponse> responses = parcelService.listWaitingByPhone("13800138000");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(waitingId);
        assertThat(responses.get(0).status()).isEqualTo(ParcelStatus.WAITING_PICKUP.name());
    }

    @Test
    void pickupMarksParcelPickedUpAndSetsOutboundTime() {
        long id = insertParcel("SF100000001", "13800138000", "顺丰", "A-01-01",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(2), null);

        ParcelResponse response = parcelService.pickup(id);

        assertThat(response.status()).isEqualTo(ParcelStatus.PICKED_UP.name());
        assertThat(response.statusText()).isEqualTo("已取件");
        assertThat(response.outboundTime()).isNotNull();
        assertThat(response.overdue()).isFalse();
    }

    @Test
    void pickupRejectsAlreadyPickedUpParcel() {
        long id = insertParcel("JD100000001", "13900139000", "京东", "B-02-01",
                ParcelStatus.PICKED_UP.name(), LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> parcelService.pickup(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("包裹已取件")
                .extracting("code")
                .isEqualTo(4001);
    }

    @Test
    void pickupRejectsParcelChangedAfterRead() {
        Parcel parcel = new Parcel();
        parcel.setId(1L);
        parcel.setTrackingNo("SF100000001");
        parcel.setRecipientPhone("13800138000");
        parcel.setExpressCompany("顺丰");
        parcel.setShelfLocation("A-01-01");
        parcel.setStatus(ParcelStatus.WAITING_PICKUP.name());
        parcel.setInboundTime(LocalDateTime.now().minusHours(2));

        ParcelMapper mapper = mock(ParcelMapper.class);
        when(mapper.findById(1L)).thenReturn(Optional.of(parcel));
        when(mapper.updatePickup(eq(1L), any(LocalDateTime.class))).thenReturn(Optional.empty());

        ParcelServiceImpl service = new ParcelServiceImpl(mapper);

        assertThatThrownBy(() -> service.pickup(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("包裹已取件")
                .extracting("code")
                .isEqualTo(4001);
    }

    @Test
    void waitingParcelOlderThan48HoursIsOverdue() {
        long id = insertParcel("YT100000001", "13800138000", "圆通", "A-01-02",
                ParcelStatus.WAITING_PICKUP.name(), LocalDateTime.now().minusHours(50), null);

        ParcelResponse response = parcelService.page(new ParcelPageRequest(1, 10, null, null, null))
                .records()
                .stream()
                .filter(record -> record.id().equals(id))
                .findFirst()
                .orElseThrow();

        assertThat(response.overdue()).isTrue();
    }

    @Test
    void pickedUpParcelOlderThan48HoursIsNotOverdue() {
        long id = insertParcel("JD100000001", "13900139000", "京东", "B-02-01",
                ParcelStatus.PICKED_UP.name(), LocalDateTime.now().minusHours(60), LocalDateTime.now().minusHours(58));

        ParcelResponse response = parcelService.page(new ParcelPageRequest(1, 10, null, null, null))
                .records()
                .stream()
                .filter(record -> record.id().equals(id))
                .findFirst()
                .orElseThrow();

        assertThat(response.overdue()).isFalse();
    }

    private long insertParcel(String trackingNo, String phone, String company, String shelfLocation,
                              String status, LocalDateTime inboundTime, LocalDateTime outboundTime) {
        jdbcTemplate.update("""
                        INSERT INTO parcel
                          (tracking_no, recipient_phone, express_company, shelf_location, status, inbound_time, outbound_time)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                trackingNo, phone, company, shelfLocation, status, inboundTime, outboundTime);
        return jdbcTemplate.queryForObject("SELECT id FROM parcel WHERE tracking_no = ?", Long.class, trackingNo);
    }
}
