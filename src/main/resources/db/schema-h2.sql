CREATE TABLE IF NOT EXISTS parcel (
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
);

CREATE INDEX IF NOT EXISTS idx_phone_status ON parcel (recipient_phone, status);
CREATE INDEX IF NOT EXISTS idx_status_inbound_time ON parcel (status, inbound_time);
