-- Create table for Background Jobs tracking
CREATE TABLE IF NOT EXISTS batch_jobs (
                                          id VARCHAR(36) NOT NULL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, RUNNING, COMPLETED, FAILED',
    total_items INT DEFAULT 0,
    processed_items INT DEFAULT 0,
    error_message TEXT,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_job_status ON batch_jobs(status);