CREATE TABLE installations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    installation_id CHAR(36) NOT NULL,
    machine_hash CHAR(64) NOT NULL,
    app_version VARCHAR(32) NOT NULL DEFAULT '',
    os_version VARCHAR(128) NOT NULL DEFAULT '',
    architecture VARCHAR(32) NOT NULL DEFAULT '',
    channel VARCHAR(32) NOT NULL DEFAULT 'commercial',
    license_state VARCHAR(24) NOT NULL DEFAULT 'unlicensed',
    binary_hash CHAR(64) NULL,
    integrity_state VARCHAR(24) NOT NULL DEFAULT 'unknown',
    risk_score INT NOT NULL DEFAULT 0,
    first_seen DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat DATETIME NULL,
    last_ip_hash CHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_installation_id (installation_id),
    KEY ix_machine_hash (machine_hash),
    KEY ix_binary_hash (binary_hash),
    KEY ix_integrity_state (integrity_state),
    KEY ix_last_seen (last_seen),
    KEY ix_license_state (license_state),
    KEY ix_risk_score (risk_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE licenses (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    license_hash CHAR(64) NOT NULL,
    customer VARCHAR(255) NOT NULL DEFAULT '',
    edition VARCHAR(64) NOT NULL DEFAULT 'Pro',
    status VARCHAR(24) NOT NULL DEFAULT 'active',
    expiry_at DATETIME NULL,
    machine_binding_hash CHAR(64) NULL,
    max_devices INT UNSIGNED NOT NULL DEFAULT 1,
    activation_count INT UNSIGNED NOT NULL DEFAULT 0,
    risk_score INT NOT NULL DEFAULT 0,
    first_activated_at DATETIME NULL,
    last_seen DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_license_hash (license_hash),
    KEY ix_status (status),
    KEY ix_customer (customer),
    KEY ix_last_seen (last_seen),
    KEY ix_risk_score (risk_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE license_devices (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    license_id BIGINT UNSIGNED NOT NULL,
    installation_id CHAR(36) NOT NULL,
    machine_hash CHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'active',
    activated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_license_machine (license_id, machine_hash),
    KEY ix_installation_id (installation_id),
    KEY ix_machine_hash (machine_hash),
    KEY ix_device_status (status),
    CONSTRAINT fk_license_devices_license FOREIGN KEY (license_id) REFERENCES licenses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE release_builds (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    app_version VARCHAR(32) NOT NULL,
    channel VARCHAR(32) NOT NULL DEFAULT 'commercial',
    exe_sha256 CHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'allowed',
    enforce_integrity TINYINT(1) NOT NULL DEFAULT 0,
    notes VARCHAR(255) NOT NULL DEFAULT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_release_hash (app_version,channel,exe_sha256),
    KEY ix_release_version (app_version,channel),
    KEY ix_release_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE security_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    installation_id CHAR(36) NULL,
    license_id BIGINT UNSIGNED NULL,
    event_type VARCHAR(80) NOT NULL,
    severity VARCHAR(16) NOT NULL DEFAULT 'low',
    risk_delta INT NOT NULL DEFAULT 0,
    metadata_json JSON NULL,
    ip_hash CHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_event_type (event_type),
    KEY ix_severity (severity),
    KEY ix_created_at (created_at),
    KEY ix_installation_id (installation_id),
    KEY ix_license_id (license_id),
    CONSTRAINT fk_security_events_license FOREIGN KEY (license_id) REFERENCES licenses(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_audit (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    admin_user VARCHAR(128) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(40) NOT NULL DEFAULT '',
    target_id VARCHAR(128) NOT NULL DEFAULT '',
    metadata_json JSON NULL,
    ip_hash CHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_admin_user (admin_user),
    KEY ix_created_at (created_at),
    KEY ix_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
