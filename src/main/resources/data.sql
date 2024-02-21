-- 더미 데이터

-- 회원 2명
INSERT INTO member_table (account, password, is_email_verified, email) VALUES
('admin', 'password', false, 'cookie_31'),
('user', '1234', true, 'suwonsuwon');

-- 회원 프로필 roomid 배정
INSERT INTO profile_table (nickname, mbti, intro, room_id) VALUES
('nick', 'INTJ', 'This is an intro text.', '1234'),
('noname', 'ENTP', 'Another intro here.', '1234');