-- Add foreign keys to databases created after Enrollment switched to scalar IDs.
-- Existing AC1 PostgreSQL schemas already have these constraints; the guards
-- make this migration safe to repeat. No table or row is dropped.
BEGIN;
DO $$
DECLARE
  course_column smallint;
  student_column smallint;
BEGIN
  SELECT attnum INTO course_column FROM pg_attribute
    WHERE attrelid = 'public.enrollment'::regclass AND attname = 'course_id';
  SELECT attnum INTO student_column FROM pg_attribute
    WHERE attrelid = 'public.enrollment'::regclass AND attname = 'student_id';

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conrelid = 'public.enrollment'::regclass
      AND confrelid = 'public.course'::regclass
      AND contype = 'f' AND conkey = ARRAY[course_column]
  ) THEN
    ALTER TABLE public.enrollment ADD CONSTRAINT fk_enrollment_course_b1
      FOREIGN KEY (course_id) REFERENCES public.course(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conrelid = 'public.enrollment'::regclass
      AND confrelid = 'public.student'::regclass
      AND contype = 'f' AND conkey = ARRAY[student_column]
  ) THEN
    ALTER TABLE public.enrollment ADD CONSTRAINT fk_enrollment_student_b1
      FOREIGN KEY (student_id) REFERENCES public.student(id);
  END IF;
END $$;
COMMIT;
