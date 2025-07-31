USE `thRedDb`;

-- User 데이터 삽입 (ChatPart와 연관되므로 미리 추가)
-- member 테이블에 데이터 삽입
INSERT INTO member (
    id, username, role, birth, gender, email, password, city, province,
    introduce, code, input_code, partner_gender, phone_number, block,
    certification, main_profile, created_date, last_modified_date, quit
)
VALUES
    (1, 'JohnDoe', 'USER', '1990-05-15', 'MALE', 'johndoe@example.com', 'password123',
     'New York', 'NY', 'Hello, I am John!', 'CODE123',null, 'OTHER',
     '123-456-7890', FALSE, TRUE, '/images/john_main.jpg', NOW(), NOW(),false),
    (2, 'JaneDoe', 'USER', '1992-07-20', 'FEMALE', 'janedoe@example.com', 'password456',
     'Los Angeles', 'CA', 'Hi, I am Jane.', 'CODE456',null, 'OTHER',
     '987-654-3210', FALSE, TRUE, '/images/jane_main.jpg', NOW(), NOW(),false),
    (3, 'AliceSmith', 'USER', '1991-03-10', 'FEMALE', 'alice@example.com', 'password789',
     'San Francisco', 'CA', 'Love exploring new things!', 'CODE789', null,'OTHER',
     '111-222-3333', FALSE, TRUE, '/images/alice.jpg', NOW(), NOW(),false),
    (4, 'BobBrown', 'USER', '1988-12-25', 'MALE', 'bob@example.com', 'password012',
     'Chicago', 'IL', 'Tech enthusiast.', 'CODE012', null,'OTHER',
     '222-333-4444', FALSE, TRUE, '/images/bob.jpg', NOW(), NOW(),false),
    (5, 'CharlieDavis', 'USER', '1995-08-14', 'MALE', 'charlie@example.com', 'password345',
     'Seattle', 'WA', 'I enjoy hiking and photography.', 'CODE345', null,'OTHER',
     '333-444-5555', FALSE, TRUE, '/images/charlie.jpg', NOW(), NOW(),false),
    (6, 'DianaEvans', 'USER', '1993-06-22', 'FEMALE', 'diana@example.com', 'password678',
     'Austin', 'TX', 'Fitness lover!', 'CODE678', null,'OTHER',
     '444-555-6666', FALSE, TRUE, '/images/diana.jpg', NOW(), NOW(),false),
    (7, 'EdwardFoster', 'USER', '1987-11-30', 'MALE', 'edward@example.com', 'password901',
     'Denver', 'CO', 'Avid reader and traveler.', 'CODE901',null, 'OTHER',
     '555-666-7777', FALSE, TRUE, '/images/edward.jpg', NOW(), NOW(),false),
    (8, 'FionaGreen', 'USER', '1994-04-18', 'FEMALE', 'fiona@example.com', 'password234',
     'Miami', 'FL', 'Love spending time on the beach.', 'CODE234',null, 'OTHER',
     '666-777-8888', FALSE, TRUE, '/images/fiona.jpg', NOW(), NOW(),false),
    (9, 'GeorgeHarris', 'USER', '1996-01-05', 'MALE', 'george@example.com', 'password567',
     'Boston', 'MA', 'Aspiring musician.', 'CODE567', null,'OTHER',
     '777-888-9999', FALSE, TRUE, '/images/george.jpg', NOW(), NOW(),false),
    (10, 'HannahJones', 'USER', '1992-09-27', 'FEMALE', 'hannah@example.com', 'password890',
     'Dallas', 'TX', 'Animal lover and volunteer.', 'CODE890', null,'OTHER',
     '888-999-0000', FALSE, TRUE, '/images/hannah.jpg', NOW(), NOW(),false),
    (11, 'IsaacKing', 'USER', '1997-07-13', 'MALE', 'isaac@example.com', 'password1234',
     'Atlanta', 'GA', 'Love playing video games.', 'CODE1234', null,'OTHER',
     '999-000-1111', FALSE, TRUE, '/images/isaac.jpg', NOW(), NOW(),false),
    (12, 'JuliaLopez', 'USER', '1990-02-08', 'FEMALE', 'julia@example.com', 'password5678',
     'Phoenix', 'AZ', 'Aspiring chef.', 'CODE5678', null,'OTHER',
     '000-111-2222', FALSE, TRUE, '/images/julia.jpg', NOW(), NOW(),false),
    (13, 'KevinMartin', 'USER', '1989-10-20', 'MALE', 'kevin@example.com', 'password9101',
     'Las Vegas', 'NV', 'Poker enthusiast.', 'CODE9101', null,'OTHER',
     '111-222-3334', FALSE, TRUE, '/images/kevin.jpg', NOW(), NOW(),false),
    (14, 'LaraNelson', 'USER', '1991-05-17', 'FEMALE', 'lara@example.com', 'password2345',
     'San Diego', 'CA', 'Yoga instructor.', 'CODE2345', null,'OTHER',
     '222-333-4445', FALSE, TRUE, '/images/lara.jpg', NOW(), NOW(),false),
    (15, 'MichaelOwen', 'USER', '1993-12-01', 'MALE', 'michael@example.com', 'password6789',
     'Houston', 'TX', 'Outdoor adventurer.', 'CODE6789', null,'OTHER',
     '333-444-5556', FALSE, TRUE, '/images/michael.jpg', NOW(), NOW(),false),
    (16, 'NataliePerez', 'USER', '1994-08-08', 'FEMALE', 'natalie@example.com', 'password9102',
     'Portland', 'OR', 'Digital artist.', 'CODE9102', null,'OTHER',
     '444-555-6667', FALSE, TRUE, '/images/natalie.jpg', NOW(), NOW(),false),
    (17, 'OliverQuinn', 'USER', '1996-03-29', 'MALE', 'oliver@example.com', 'password1235',
     'Salt Lake City', 'UT', 'Sports enthusiast.', 'CODE1235', null,'OTHER',
     '555-666-7778', FALSE, TRUE, '/images/oliver.jpg', NOW(), NOW(),false),
    (18, 'PaulaRoberts', 'USER', '1988-11-19', 'FEMALE', 'paula@example.com', 'password5679',
     'San Antonio', 'TX', 'Food blogger.', 'CODE5679', null,'OTHER',
     '666-777-8889', FALSE, TRUE, '/images/paula.jpg', NOW(), NOW(),false),
    (19, 'QuinnStewart', 'USER', '1989-06-12', 'MALE', 'quinn@example.com', 'password9103',
     'Tampa', 'FL', 'Fitness trainer.', 'CODE9103', null,'OTHER',
     '777-888-9990', FALSE, TRUE, '/images/quinn.jpg', NOW(), NOW(),false),
    (20, 'RachelTaylor', 'USER', '1995-02-25', 'FEMALE', 'rachel@example.com', 'password2346',
     'Orlando', 'FL', 'Travel blogger.', 'CODE2346', null,'OTHER',
     '888-999-0001', FALSE, TRUE, '/images/rachel.jpg', NOW(), NOW(),false),
    (21, 'admin', 'ADMIN', '1995-02-25', null, 'admin', 'password2346',
     'Orlando', 'FL', 'Travel blogger.', 'CODE2346', null,'OTHER',
     '888-999-0001', FALSE, TRUE, '/images/rachel.jpg', NOW(), NOW(),false);

-- Details 테이블 데이터 삽입
INSERT INTO userDetail (id, created_date, last_modified_date, user_id, height, drink, belief, smoke, opposite_friends, mbti, job, temperature)
VALUES
    (1, NOW(), NOW(), 1, 175, 'ENJOY', 'CHRISTIANITY', 'NONE', 'A_FEW', 'INFJ', 'STUDENT', 67),
    (2, NOW(), NOW(), 2, 160, 'LIKE', 'NON_RELIGIOUS', 'NO_SMOKE', 'A_LOT', 'ENTP', 'STUDENT', 89),
    (3, NOW(), NOW(), 3, 168, 'SOMETIMES', 'BUDDHISM', 'NONE', 'A_LITTLE_BIT', 'ISFP', 'OFFICE_WORKER', 72),
    (4, NOW(), NOW(), 4, 182, 'DRINKER', 'ROMAN_CATHOLICISM', 'ELECTRONIC_CIGARETTE', 'NO', 'INTJ', 'JOB_SEEKER', 55),
    (5, NOW(), NOW(), 5, 178, 'NO', 'WON_BUDDHISM', 'SMOKER', 'A_FEW', 'ESTJ', 'STUDENT', 91),
    (6, NOW(), NOW(), 6, 165, 'LIKE', 'CHRISTIANITY', 'NO_SMOKE', 'A_LOT', 'ENTJ', 'PART_TIME', 47),
    (7, NOW(), NOW(), 7, 170, 'REQUIRED', 'NON_RELIGIOUS', 'NONE', 'A_LITTLE_BIT', 'ISTP', 'STUDENT', 60),
    (8, NOW(), NOW(), 8, 162, 'SOMETIMES', 'BUDDHISM', 'NONE', 'A_LOT', 'ESFP', 'STUDENT', 88),
    (9, NOW(), NOW(), 9, 174, 'DRINKER', 'ROMAN_CATHOLICISM', 'SMOKER', 'A_FEW', 'INFP', 'OFFICE_WORKER', 74),
    (10, NOW(), NOW(), 10, 159, 'NO', 'ELSE', 'NONE', 'NO', 'ESTP', 'JOB_SEEKER', 92),
    (11, NOW(), NOW(), 11, 181, 'LIKE', 'CHRISTIANITY', 'NO_SMOKE', 'A_FEW', 'ENTP', 'STUDENT', 68),
    (12, NOW(), NOW(), 12, 166, 'ENJOY', 'NON_RELIGIOUS', 'ELECTRONIC_CIGARETTE', 'A_LOT', 'ISFJ', 'OFFICE_WORKER', 84),
    (13, NOW(), NOW(), 13, 180, 'SOMETIMES', 'BUDDHISM', 'NONE', 'A_LITTLE_BIT', 'ISTJ', 'PART_TIME', 79),
    (14, NOW(), NOW(), 14, 158, 'DRINKER', 'ROMAN_CATHOLICISM', 'NO_SMOKE', 'A_LOT', 'ENTJ', 'STUDENT', 46),
    (15, NOW(), NOW(), 15, 172, 'NO', 'WON_BUDDHISM', 'NONE', 'NO', 'ESFJ', 'STUDENT', 63),
    (16, NOW(), NOW(), 16, 164, 'LIKE', 'CHRISTIANITY', 'NO_SMOKE', 'A_FEW', 'INTP', 'OFFICE_WORKER', 59),
    (17, NOW(), NOW(), 17, 177, 'REQUIRED', 'NON_RELIGIOUS', 'NONE', 'A_LOT', 'ESTJ', 'JOB_SEEKER', 73),
    (18, NOW(), NOW(), 18, 167, 'SOMETIMES', 'BUDDHISM', 'ELECTRONIC_CIGARETTE', 'A_FEW', 'ESFP', 'PART_TIME', 80),
    (19, NOW(), NOW(), 19, 176, 'DRINKER', 'ROMAN_CATHOLICISM', 'SMOKER', 'A_LITTLE_BIT', 'INFJ', 'STUDENT', 85),
    (20, NOW(), NOW(), 20, 160, 'ENJOY', 'WON_BUDDHISM', 'NONE', 'A_LOT', 'ISFP', 'STUDENT', 62),
    (21, NOW(), NOW(), 21, 182, 'NO', 'ELSE', 'NO_SMOKE', 'NO', 'INTJ', 'OFFICE_WORKER', 95);

-- Picture 테이블 데이터 삽입
INSERT INTO picture (id, s3_path, original_file_name, user_id, created_date, last_modified_date)
VALUES
    (1, '/images/john_picture1.jpg', 'john_picture1.jpg', 1, NOW(), NOW()),
    (2, '/images/jane_picture1.jpg', 'jane_picture1.jpg', 2, NOW(), NOW());

-- ChatRoom 데이터 삽입
INSERT INTO chat_room (id, chat_type, created_date, last_modified_date) VALUES
                                                                            (1, 'TODAY_CARD', NOW(), NOW()), -- UserA와 UserB의 1대1 채팅방
                                                                            (2, 'COMMUNITY', NOW(), NOW()), -- UserA와 UserB의 그룹 채팅방
                                                                            (3, 'TODAY_CARD', NOW(), NOW()); -- UserC와 UserD의 1대1 채팅방

-- ChatPart 데이터 삽입 (ChatRoom과 User 연결)
INSERT INTO chat_part (id, user_id, chat_room_id, created_date, last_modified_date) VALUES
                                                                                        (1, 1, 1, NOW(), NOW()), -- UserA의 ChatPart
                                                                                        (2, 2, 1, NOW(), NOW()), -- UserB의 ChatPart
                                                                                        (3, 1, 2, NOW(), NOW()), -- UserA의 ChatPart
                                                                                        (4, 2, 2, NOW(), NOW()), -- UserB의 ChatPart
                                                                                        (5, 3, 2, NOW(), NOW()), -- UserC의 ChatPart
                                                                                        (6, 4, 2, NOW(), NOW()), -- UserD의 ChatPart
                                                                                        (7, 3, 3, NOW(), NOW()), -- UserC의 ChatPart
                                                                                        (8, 4, 3, NOW(), NOW()); -- UserD의 ChatPart

-- Chat 데이터 삽입 (다양한 메시지 추가)
INSERT INTO chat (id, message, read_status, chat_part_id, created_date, last_modified_date) VALUES
                                                                                            -- ChatRoom 1: UserA와 UserB의 대화
                                                                                            (1, 'Hello, UserB!', FALSE, 1, '2024-11-30 10:30:00', '2024-11-30 10:30:00'),
                                                                                            (2, 'Hi, UserA! How are you?', FALSE, 2, '2024-11-30 10:32:00', '2024-11-30 10:32:00'),
                                                                                            (3, 'I am doing well, thanks. How about you?', TRUE, 1, '2024-11-30 10:35:00', '2024-11-30 10:35:00'),
                                                                                            (4, 'I am great. Just finished some work.', TRUE, 2, '2024-11-30 10:37:00', '2024-11-30 10:37:00'),
                                                                                            (5, 'That sounds productive!', FALSE, 1, '2024-11-30 10:40:00', '2024-11-30 10:40:00'),
                                                                                            (6, 'Do you want to grab lunch later?', FALSE, 1, '2024-11-30 10:45:00', '2024-11-30 10:45:00'),
                                                                                            (7, 'Sure! What time works for you?', TRUE, 2, '2024-11-30 10:47:00', '2024-11-30 10:47:00'),
                                                                                            (8, 'How about 1 PM?', FALSE, 1, '2024-11-30 10:50:00', '2024-11-30 10:50:00'),

                                                                                            -- ChatRoom 2: UserA, UserB, UserC, UserD의 그룹 대화
                                                                                            (9, 'Hey everyone, good morning!', FALSE, 3, '2024-11-30 09:00:00', '2024-11-30 09:00:00'),
                                                                                            (10, 'Good morning, UserA!', FALSE, 4, '2024-11-30 09:02:00', '2024-11-30 09:02:00'),
                                                                                            (11, 'Good morning! How’s everyone doing?', FALSE, 5, '2024-11-30 09:05:00', '2024-11-30 09:05:00'),
                                                                                            (12, 'All good here. What about you?', TRUE, 6, '2024-11-30 09:07:00', '2024-11-30 09:07:00'),
                                                                                            (13, 'I am doing well, just planning my weekend.', FALSE, 3, '2024-11-30 09:10:00', '2024-11-30 09:10:00'),
                                                                                            (14, 'Any good plans?', FALSE, 4, '2024-11-30 09:12:00', '2024-11-30 09:12:00'),
                                                                                            (15, 'Thinking of going hiking. Anyone wants to join?', FALSE, 3, '2024-11-30 09:15:00', '2024-11-30 09:15:00'),
                                                                                            (16, 'Count me in!', FALSE, 5, '2024-11-30 09:18:00', '2024-11-30 09:18:00'),
                                                                                            (17, 'Sounds fun. I will check my schedule.', FALSE, 6, '2024-11-30 09:20:00', '2024-11-30 09:20:00'),

                                                                                            -- ChatRoom 3: UserC와 UserD의 대화
                                                                                            (18, 'Hi, UserD! Are you free this weekend?', FALSE, 7, '2024-11-29 15:00:00', '2024-11-29 15:00:00'),
                                                                                            (19, 'Hey, UserC! I think I am. What’s up?', FALSE, 8, '2024-11-29 15:02:00', '2024-11-29 15:02:00'),
                                                                                            (20, 'I was thinking of going to the new cafe downtown. Wanna join?', FALSE, 7, '2024-11-29 15:05:00', '2024-11-29 15:05:00'),
                                                                                            (21, 'Sure! I heard it’s really good.', FALSE, 8, '2024-11-29 15:07:00', '2024-11-29 15:07:00'),
                                                                                            (22, 'Great! Let’s meet at 3 PM on Saturday.', FALSE, 7, '2024-11-29 15:10:00', '2024-11-29 15:10:00'),
                                                                                            (23, 'Perfect. See you then!', FALSE, 8, '2024-11-29 15:12:00', '2024-11-29 15:12:00');

-- 샘플 커뮤니티 데이터 (community)
INSERT INTO community (
    id, title, content, is_public_profile, user_id, created_date, last_modified_date
)
VALUES
    (1, 'Community Post 1', 'This is the first community post.', TRUE, 1, '2024-11-30 10:30:00', '2024-11-30 10:30:00'),
    (2, 'Community Post 2', 'This is the second community post.',  FALSE, 2, '2024-11-30 10:45:00', '2024-11-30 10:45:00'),
    (3, 'Community Post 3', '커뮤니티 게시글 테스트입니다.',  FALSE, 3, '2024-11-30 11:30:00', '2024-11-30 11:30:00'),
    (4, 'Community Post 4', '커뮤니티 게시글 테스트입니다.....',  FALSE, 4, '2024-11-30 11:45:00', '2024-11-30 11:45:00');

-- 샘플 커뮤니티 이미지 데이터 (community_image)
INSERT INTO community_image (
    id, original_file_name,s3_path, community_id, created_date, last_modified_date
)
VALUES
    (1, '/images/community1_img1.jpg', 'community1_img1.jpg', 1, NOW(), NOW()),
    (2, '/images/community1_img2.jpg', 'community1_img2.jpg', 1, NOW(), NOW()),
    (3, '/images/community2_img1.jpg', 'community2_img1.jpg', 2, NOW(), NOW());

-- 댓글 데이터 삽입 (comment)
INSERT INTO comment (
    id, content, community_id, parent_id, user_id, created_date, last_modified_date
)
VALUES
    -- 부모 댓글 1 (Community Post 1)
    (1, 'Parent comment 1 on Post 1.', 1, NULL, 1, '2024-12-01 10:00:00', '2024-12-01 10:00:00'),

    -- 자식 댓글 1-1 (부모 댓글 1에 연결)
    (2, 'Child comment 1 under Parent comment 1.', 1, 1, 2, '2024-12-01 10:15:00', '2024-12-01 10:15:00'),

    -- 자식 댓글 1-2 (부모 댓글 1에 연결)
    (3, 'Child comment 2 under Parent comment 1.', 1, 1, 1, '2024-12-01 10:30:00', '2024-12-01 10:30:00'),

    -- 부모 댓글 2 (Community Post 1)
    (4, 'Parent comment 2 on Post 1.', 1, NULL, 2, '2024-12-01 11:00:00', '2024-12-01 11:00:00'),

    -- 자식 댓글 2-1 (부모 댓글 2에 연결)
    (5, 'Child comment 1 under Parent comment 2.', 1, 4, 1, '2024-12-01 11:15:00', '2024-12-01 11:15:00'),

    -- 부모 댓글 3 (Community Post 2)
    (6, 'Parent comment 1 on Post 2.', 2, NULL, 2, '2024-12-01 12:00:00', '2024-12-01 12:00:00'),

    -- 자식 댓글 3-1 (부모 댓글 3에 연결)
    (7, 'Child comment 1 under Parent comment 1 on Post 2.', 2, 6, 1, '2024-12-01 12:15:00', '2024-12-01 12:15:00'),

    -- 부모 댓글 4 (Community Post 2)
    (8, 'Parent comment 2 on Post 2.', 2, NULL, 1, '2024-12-01 13:00:00', '2024-12-01 13:00:00');

-- CommunityLike 테이블 데이터 삽입
INSERT INTO community_like (
    community_id, user_id, created_date, last_modified_date
)
VALUES
    -- User 1이 Community 1을 좋아요
    (1, 1, '2024-12-02 09:00:00', '2024-12-02 09:00:00'),
    (1, 2, '2024-12-02 09:05:00', '2024-12-02 09:00:00'),
    (1, 3, '2024-12-02 09:10:00','2024-12-02 09:00:00'),
    (1, 4, '2024-12-02 09:15:00','2024-12-02 09:00:00'),
    (1, 5, '2024-12-02 09:20:00','2024-12-02 09:00:00'),
    (1, 6, '2024-12-02 09:25:00','2024-12-02 09:00:00'),
    (1, 7, '2024-12-02 09:30:00','2024-12-02 09:00:00'),
    (1, 8, '2024-12-02 09:35:00','2024-12-02 09:00:00'),
    (1, 9, '2024-12-02 09:40:00','2024-12-02 09:00:00'),
    (1, 10, '2024-12-02 09:45:00','2024-12-02 09:00:00'),
    (1, 11, '2024-12-02 09:50:00','2024-12-02 09:00:00'),
    (1, 12, '2024-12-02 09:55:00','2024-12-02 09:00:00'),
    (1, 13, '2024-12-02 10:00:00','2024-12-02 09:00:00'),
    (1, 14, '2024-12-02 10:05:00','2024-12-02 09:00:00'),
    (1, 15, '2024-12-02 10:10:00','2024-12-02 09:00:00'),
    (1, 16, '2024-12-02 10:15:00','2024-12-02 09:00:00'),
    (1, 17, '2024-12-02 10:20:00','2024-12-02 09:00:00'),
    (1, 18, '2024-12-02 10:25:00','2024-12-02 09:00:00'),
    (1, 19, '2024-12-02 10:30:00','2024-12-02 09:00:00'),
    (1, 20, '2024-12-02 10:35:00','2024-12-02 09:00:00'),
    -- User 1이 Community 2를 좋아요
    (2, 1, '2024-12-02 09:10:00','2024-12-02 09:00:00'),

    -- User 2가 Community 2를 좋아요
    (2, 2, '2024-12-02 09:15:00','2024-12-02 09:00:00');

-- 댓글 좋아요 데이터 삽입 (comment_like)
INSERT INTO comment_like (
    comment_id, user_id, created_date, last_modified_date
)
VALUES
    -- Parent Comment 1 좋아요
    (1, 1, '2024-12-01 10:10:00', '2024-12-02 09:00:00'),
    (1, 2, '2024-12-01 10:12:00', '2024-12-02 09:00:00'),

    -- Child Comment 1-1 좋아요
    (2, 1, '2024-12-01 10:20:00', '2024-12-02 09:00:00'),

    -- Parent Comment 2 좋아요
    (4, 2, '2024-12-01 11:10:00', '2024-12-02 09:00:00'),
    (4, 1, '2024-12-01 11:12:00', '2024-12-02 09:00:00'),

    -- Parent Comment 3 (Post 2) 좋아요
    (6, 1, '2024-12-01 12:10:00', '2024-12-02 09:00:00'),

    -- Child Comment 3-1 좋아요
    (7, 2, '2024-12-01 12:20:00', '2024-12-02 09:00:00'),
    (7, 1, '2024-12-01 12:22:00', '2024-12-02 09:00:00');

-- Insert data into the `Product` table
INSERT INTO product (
    id, in_app_product_id, title, price, quantity_thread, created_date, last_modified_date
)
VALUES
    (1, 'com.thred.premium1', 'Premium Threads Pack 1', 9, 50, '2024-12-01 13:00:00', '2024-12-01 13:00:00'),
    (2, 'com.thred.premium2', 'Premium Threads Pack 2', 15, 100, '2024-12-01 13:10:00', '2024-12-01 13:10:00');

-- Insert data into the `Receipt` table
INSERT INTO receipt (
    id, transaction_id, original_transaction_id, receipt_data, purchase_date, user_id, product_id, created_date, last_modified_date
)
VALUES
    (1, 'TXN12345', 'TXN67890', 'sample-receipt-data-1', '2024-12-02 14:00:00', 1, 1, '2024-12-02 14:30:00', '2024-12-02 14:30:00'),
    (2, 'TXN54321', 'TXN09876', 'sample-receipt-data-2', '2024-12-02 15:00:00', 2, 2, '2024-12-02 15:30:00', '2024-12-02 15:30:00');

-- Insert data into the `ThreadUseHistory` table
INSERT INTO thread_use_history (
    id, used_thread, product_name, user_id, created_date, last_modified_date
)
VALUES
    (1, 20, 'Premium Threads Pack 1', 1,  '2024-12-03 16:00:00', '2024-12-03 16:00:00'),
    (2, 40, 'Premium Threads Pack 2', 1, '2024-12-03 16:20:00', '2024-12-03 16:20:00'),
    (3, 30, 'Premium Threads Pack 2', 2, '2024-12-03 16:30:00', '2024-12-03 16:30:00'),
    (4, 25, 'Premium Threads Pack 1', 1, '2024-12-03 16:40:00', '2024-12-03 16:40:00'),
    (5, 35, 'Premium Threads Pack 3', 1, '2024-12-03 16:50:00', '2024-12-03 16:50:00'),
    (6, 15, 'Premium Threads Pack 1', 1, '2024-12-03 17:00:00', '2024-12-03 17:00:00'),
    (7, 50, 'Premium Threads Pack 4', 1, '2024-12-03 17:10:00', '2024-12-03 17:10:00'),
    (8, 20, 'Premium Threads Pack 2', 1, '2024-12-03 17:20:00', '2024-12-03 17:20:00'),
    (9, 30, 'Premium Threads Pack 3', 1, '2024-12-03 17:30:00', '2024-12-03 17:30:00'),
    (10, 40, 'Premium Threads Pack 4', 1, '2024-12-03 17:40:00', '2024-12-03 17:40:00'),
    (11, 10, 'Premium Threads Pack 1', 1, '2024-12-03 17:50:00', '2024-12-03 17:50:00'),
    (12, 45, 'Premium Threads Pack 2', 1, '2024-12-03 18:00:00', '2024-12-03 18:00:00'),
    (13, 60, 'Premium Threads Pack 5', 1, '2024-12-03 18:10:00', '2024-12-03 18:10:00');

-- Insert data into the `UserAsset` table
INSERT INTO user_asset (
    id, total_thread, user_id, created_date, last_modified_date
)
VALUES
    (1, 50, 1 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (2, 100, 2, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (3, 0, 3 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (4, 0, 4, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (5, 0, 5 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (6, 0, 6, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (7, 0, 7 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (8, 0, 8, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (9, 0, 9 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (10, 0, 10, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (11, 0, 11 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (12, 0, 12, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (13, 0, 13 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (14, 0, 14, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (15, 0, 15 , '2024-12-04 17:00:00', '2024-12-04 17:00:00'),
    (16, 0, 16, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (17, 0, 17, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (18, 0, 18, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (19, 0, 19, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (20, 0, 20, '2024-12-04 17:30:00', '2024-12-04 17:30:00'),
    (21, 0, 21, '2024-12-04 17:30:00', '2024-12-04 17:30:00');

insert into fcm_token(id, created_date, last_modified_date, token, user_id) values
                                                                                (1, '2024-12-04 17:30:00', '2024-12-04 17:30:00', 'cazMSPIQRBGjf9bWgyhB4q:APA91bEWqifIhDwhebUvC3k-0TT02o9--sv4Rx2IczIQPwXUEVcHJigZGvjQzhNElGk0bOtrbQDin7Vjp4uLbwx-JuUZLhC4AKXMH7GsPHdGFVCoVdw-x-k', 1),
                                                                                (2, '2024-12-04 17:30:00', '2024-12-04 17:30:00', 'cazMSPIQRBGjf9bWgyhB4q:APA91bEWqifIhDwhebUvC3k-0TT02o9--sv4Rx2IczIQPwXUEVcHJigZGvjQzhNElGk0bOtrbQDin7Vjp4uLbwx-JuUZLhC4AKXMH7GsPHdGFVCoVdw-x-k', 2);
