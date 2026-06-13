package com.shunfeng.demo.mapper;

import com.shunfeng.demo.dto.ParcelPageRequest;
import com.shunfeng.demo.entity.Parcel;
import com.shunfeng.demo.enums.ParcelStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ParcelMapper {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Parcel> rowMapper = (rs, rowNum) -> {
        Parcel parcel = new Parcel();
        parcel.setId(rs.getLong("id"));
        parcel.setTrackingNo(rs.getString("tracking_no"));
        parcel.setRecipientPhone(rs.getString("recipient_phone"));
        parcel.setExpressCompany(rs.getString("express_company"));
        parcel.setShelfLocation(rs.getString("shelf_location"));
        parcel.setStatus(rs.getString("status"));
        parcel.setInboundTime(toLocalDateTime(rs.getTimestamp("inbound_time")));
        parcel.setOutboundTime(toLocalDateTime(rs.getTimestamp("outbound_time")));
        parcel.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        parcel.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return parcel;
    };

    public ParcelMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Parcel insert(Parcel parcel) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO parcel
                      (tracking_no, recipient_phone, express_company, shelf_location, status, inbound_time, outbound_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, parcel.getTrackingNo());
            ps.setString(2, parcel.getRecipientPhone());
            ps.setString(3, parcel.getExpressCompany());
            ps.setString(4, parcel.getShelfLocation());
            ps.setString(5, parcel.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(parcel.getInboundTime()));
            if (parcel.getOutboundTime() == null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(parcel.getOutboundTime()));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            parcel.setId(key.longValue());
        }
        return findById(parcel.getId()).orElse(parcel);
    }

    public Optional<Parcel> findByTrackingNo(String trackingNo) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM parcel WHERE tracking_no = ?",
                    rowMapper,
                    trackingNo
            ));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Parcel> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM parcel WHERE id = ?",
                    rowMapper,
                    id
            ));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Parcel> findWaitingByPhone(String phone) {
        return jdbcTemplate.query("""
                SELECT * FROM parcel
                WHERE recipient_phone = ? AND status = ?
                ORDER BY inbound_time ASC, id ASC
                """, rowMapper, phone, ParcelStatus.WAITING_PICKUP.name());
    }

    public long count(ParcelPageRequest request) {
        QueryParts queryParts = buildFilter(request);
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parcel" + queryParts.whereClause(),
                Long.class,
                queryParts.params().toArray());
    }

    public List<Parcel> page(ParcelPageRequest request) {
        QueryParts queryParts = buildFilter(request);
        List<Object> params = new ArrayList<>(queryParts.params());
        params.add(request.normalizedPageSize());
        params.add((request.normalizedPage() - 1) * request.normalizedPageSize());
        return jdbcTemplate.query("""
                SELECT * FROM parcel
                %s
                ORDER BY inbound_time DESC, id DESC
                LIMIT ? OFFSET ?
                """.formatted(queryParts.whereClause()), rowMapper, params.toArray());
    }

    public Optional<Parcel> updatePickup(Long id, LocalDateTime outboundTime) {
        int updated = jdbcTemplate.update("""
                UPDATE parcel
                SET status = ?, outbound_time = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = ?
                """, ParcelStatus.PICKED_UP.name(), outboundTime, id, ParcelStatus.WAITING_PICKUP.name());
        if (updated == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    private QueryParts buildFilter(ParcelPageRequest request) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (hasText(request.phone())) {
            conditions.add("recipient_phone = ?");
            params.add(request.phone().trim());
        }
        if (hasText(request.trackingNo())) {
            conditions.add("tracking_no LIKE ?");
            params.add("%" + request.trackingNo().trim() + "%");
        }
        if (hasText(request.status())) {
            conditions.add("status = ?");
            params.add(request.status().trim());
        }
        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new QueryParts(whereClause, params);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record QueryParts(String whereClause, List<Object> params) {
    }
}
