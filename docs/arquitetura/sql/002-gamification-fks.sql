-- Run after the application creates the new tables. No rows are removed.
-- Scalar IDs keep Java modules independent; PostgreSQL protects their references.
DO $$
DECLARE rule record;
BEGIN
    FOR rule IN SELECT * FROM (VALUES
        ('wallet_entry', 'student_id', 'student', 'id', 'fk_wallet_student'),
        ('ranking_entry', 'student_id', 'student', 'id', 'fk_ranking_student'),
        ('completion_progress', 'student_id', 'student', 'id', 'fk_progress_student'),
        ('completion_progress', 'enrollment_id', 'enrollment', 'id', 'fk_progress_enrollment'),
        ('student_badge', 'student_id', 'student', 'id', 'fk_badge_student'),
        ('reward_redemption', 'student_id', 'student', 'id', 'fk_redemption_student')
    ) AS definitions(table_name, column_name, target_table, target_column, constraint_name)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_constraint
                       WHERE conname = rule.constraint_name AND conrelid = to_regclass(rule.table_name)) THEN
            EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES %I (%I)',
                rule.table_name, rule.constraint_name, rule.column_name, rule.target_table, rule.target_column);
        END IF;
    END LOOP;
END $$;
