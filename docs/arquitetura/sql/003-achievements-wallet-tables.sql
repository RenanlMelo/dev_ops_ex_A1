-- PostgreSQL: apply before starting the part 3 application.
BEGIN;
CREATE TABLE IF NOT EXISTS completion_progress (
    enrollment_id bigint PRIMARY KEY REFERENCES enrollment(id),
    student_id bigint REFERENCES student(id),
    grade double precision NOT NULL CHECK (grade BETWEEN 0 AND 10),
    points integer NOT NULL,
    approved boolean NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_progress_student ON completion_progress(student_id);
CREATE TABLE IF NOT EXISTS student_badge (
    id varchar(255) PRIMARY KEY,
    student_id bigint REFERENCES student(id),
    code varchar(255),
    earned_at timestamp with time zone,
    UNIQUE(student_id, code)
);
CREATE INDEX IF NOT EXISTS ix_badge_student ON student_badge(student_id);
CREATE TABLE IF NOT EXISTS wallet_entry (
    reference varchar(255) PRIMARY KEY,
    student_id bigint REFERENCES student(id),
    amount bigint NOT NULL,
    description varchar(255),
    created_at timestamp with time zone
);
CREATE INDEX IF NOT EXISTS ix_wallet_student ON wallet_entry(student_id, created_at);
COMMIT;
