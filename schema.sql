-- 부원 정보
CREATE TABLE members (
    student_id  VARCHAR(20) PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    phone_number  VARCHAR(20),
    department  VARCHAR(50),
    active_semester  INT NOT NULL DEFAULT 1,
    status  VARCHAR(10) NOT NULL DEFAULT '활동'
);

-- 주차별 활동 기록
CREATE TABLE attendance (
    id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id  VARCHAR(20) NOT NULL,
    semester  VARCHAR(20) NOT NULL,
    week  INT NOT NULL,
    workout_count  INT NOT NULL DEFAULT 0,
    attendance  BOOLEAN NOT NULL DEFAULT FALSE,
    fine  INT NOT NULL DEFAULT 0
);

-- 오운완 인증 날짜
-- attendance 기록이 삭제되면 딸린 날짜들도 같이 삭제되도록 CASCADE
CREATE TABLE workout_dates (
    id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id  BIGINT NOT NULL,
    workout_date  DATE NOT NULL,
    FOREIGN KEY  (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE
);
