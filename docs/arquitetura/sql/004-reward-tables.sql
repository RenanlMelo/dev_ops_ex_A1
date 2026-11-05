BEGIN;
CREATE TABLE IF NOT EXISTS reward_redemption (
    id varchar(255) PRIMARY KEY,
    request_id varchar(255),
    student_id bigint REFERENCES student(id),
    reward_code varchar(255),
    reward_name varchar(255),
    cost bigint NOT NULL CHECK (cost > 0),
    redeemed_at timestamp with time zone,
    UNIQUE(student_id, request_id)
);
CREATE INDEX IF NOT EXISTS ix_redemption_student ON reward_redemption(student_id, redeemed_at);
COMMIT;
