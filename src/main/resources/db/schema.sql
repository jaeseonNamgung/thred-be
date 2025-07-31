USE thRedDb;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS comment_like;
DROP TABLE IF EXISTS community_like;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS community_image;
DROP TABLE IF EXISTS community;
DROP TABLE IF EXISTS chat;
DROP TABLE IF EXISTS chat_part;
DROP TABLE IF EXISTS chat_room;
DROP TABLE IF EXISTS picture;
DROP TABLE IF EXISTS user_asset;
DROP TABLE IF EXISTS thread_use_history;
DROP TABLE IF EXISTS receipt;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS block;
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS fcm_token;
DROP TABLE IF EXISTS user_info_detail;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS user_info;


CREATE TABLE IF NOT EXISTS thRedDb.chat_room
(
    id                 bigint auto_increment
        primary key,
    chat_type          enum ('COMMUNITY', 'TODAY_CARD') not null,
    created_date       datetime(6)                      null,
    last_modified_date datetime(6)                      null
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.product
(
    id                 bigint auto_increment
        primary key,
    price              int          not null,
    quantity_thread    int          not null,
    in_app_product_id  varchar(255) not null,
    title              varchar(255) not null,
    created_date       datetime(6)  null,
    last_modified_date datetime(6)  null
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.report_history
(
    id                  bigint auto_increment
        primary key,
    reported_user_id    bigint       not null,
    reported_user_name  varchar(100) not null,
    reported_user_email varchar(255) not null,
    target_id           bigint       not null,
    reason              varchar(50)  not null,
    result              varchar(50)  not null,
    suspended_date      date         null,
    created_date        datetime     not null,
    last_modified_date  datetime     not null,
    report_type         varchar(50)  not null
);

CREATE TABLE IF NOT EXISTS thRedDb.user_info
(
    id                 bigint auto_increment
        primary key,
    social_id          bigint  not null ,
    user_state         enum ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'BLOCKED') not null,
    username           varchar(255)                                        null,
    role               enum ('ADMIN', 'USER')                              null,
    birth              varchar(255)                                        null,
    gender             enum ('FEMALE', 'MALE')                             null,
    email              varchar(255)                                        null,
    password           varchar(255)                                        null,
    city               varchar(255)                                        null,
    province           varchar(255)                                        null,
    introduce          text                                                null,
    code               varchar(255)                                        null,
    input_code         varchar(255)                                        null,
    partner_gender     enum ('OTHER', 'SAME')                              null,
    phone_number       varchar(255)                                        null,
    certification      tinyint(1) default 0                                null,
    suspended_time     datetime                                            null,
    main_profile       varchar(255)                                        null,
    created_date       datetime(6)                                         null,
    last_modified_date datetime(6)                                         null,
    quit_time          datetime(6)                                         null,
    quit               tinyint(1) default 0                                null
);

CREATE TABLE IF NOT EXISTS thRedDb.block
(
    id              bigint auto_increment
        primary key,
    blocked_user_id bigint      not null,
    blocker_id      bigint      not null,
    created_at      datetime(6) null,
    updated_at      datetime(6) null,
    constraint fk_block_blocked_user
        foreign key (blocked_user_id) references thRedDb.user_info (id),
    constraint fk_block_blocker
        foreign key (blocker_id) references thRedDb.user_info (id)
);

CREATE TABLE IF NOT EXISTS thRedDb.card
(
    id                 bigint auto_increment
        primary key,
    profile_user_id    bigint      null,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null,
    constraint card_ibfk_1
        foreign key (profile_user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.card_open
(
    id                 bigint auto_increment
        primary key,
    opener_id          bigint      not null,
    card_id            bigint      not null,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null,
    constraint fk_card_open_card
        foreign key (card_id) references thRedDb.card (id),
    constraint fk_card_open_opener
        foreign key (opener_id) references thRedDb.user_info (id)
);

CREATE TABLE IF NOT EXISTS thRedDb.chat_room
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.chat_part
(
    id                 bigint auto_increment
        primary key,
    chat_room_id       bigint      null,
    user_id            bigint      null,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null,
    constraint chat_part_ibfk_1
        foreign key (chat_room_id) references thRedDb.chat_room (id),
    constraint chat_part_ibfk_2
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.chat
(
    id                 bigint auto_increment
        primary key,
    chat_part_id       bigint               null,
    read_status        tinyint(1) default 0 null,
    message            text                 null,
    created_date       datetime(6)          null,
    last_modified_date datetime(6)          null,
    constraint chat_ibfk_1
        foreign key (chat_part_id) references thRedDb.chat_part (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.community
(
    id                 bigint auto_increment
        primary key,
    user_id            bigint           null,
    title              varchar(1000)    null,
    content            text             null,
    is_public_profile  bit default b'0' not null,
    created_date       datetime(6)      null,
    last_modified_date datetime(6)      null,
    constraint community_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.comment
(
    id                 bigint auto_increment
        primary key,
    community_id       bigint               null,
    parent_id          bigint               null,
    user_id            bigint               null,
    content            varchar(1000)        not null,
    created_date       datetime(6)          null,
    last_modified_date datetime(6)          null,
    is_delete          tinyint(1) default 0 not null,
    is_public_profile  tinyint    default 0 not null,
    constraint comment_ibfk_1
        foreign key (community_id) references thRedDb.community (id),
    constraint comment_ibfk_2
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.comment_like
(
    comment_id         bigint      not null,
    user_id            bigint      not null,
    community_id       bigint      not null,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null,
    primary key (comment_id, user_id),
    constraint comment_like_ibfk_1
        foreign key (comment_id) references thRedDb.comment (id),
    constraint comment_like_ibfk_2
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.community_image
(
    id                 bigint auto_increment
        primary key,
    community_id       bigint       null,
    original_file_name varchar(255) null,
    s3_path            varchar(255) null,
    created_date       datetime(6)  null,
    last_modified_date datetime(6)  null,
    constraint community_image_ibfk_1
        foreign key (community_id) references thRedDb.community (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.community_like
(
    community_id       bigint      not null,
    user_id            bigint      not null,
    created_date       datetime(6) null,
    last_modified_date datetime(6) null,
    primary key (community_id, user_id),
    constraint community_like_ibfk_1
        foreign key (community_id) references thRedDb.community (id),
    constraint community_like_ibfk_2
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.fcm_token
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime     null,
    last_modified_date datetime     null,
    token              varchar(255) null,
    user_id            bigint       null,
    constraint FK_FCMTOKEN_ON_USER
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.picture
(
    id                 bigint auto_increment
        primary key,
    user_id            bigint       null,
    original_file_name varchar(255) null,
    s3_path            varchar(255) null,
    created_date       datetime(6)  null,
    last_modified_date datetime(6)  null,
    constraint picture_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.question
(
    id                 bigint auto_increment
        primary key,
    user_id            bigint       null,
    question1          varchar(255) null,
    question2          varchar(255) null,
    question3          varchar(255) null,
    created_date       datetime(6)  null,
    last_modified_date datetime(6)  null,
    constraint question_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.answer
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime null,
    last_modified_date datetime null,
    answer1            longtext null,
    answer2            longtext null,
    answer3            longtext null,
    sender_id          bigint   null,
    receiver_id        bigint   null,
    question_id        bigint   null,
    constraint FK_ANSWER_ON_QUESTION
        foreign key (question_id) references thRedDb.question (id),
    constraint FK_ANSWER_ON_RECEIVER
        foreign key (receiver_id) references thRedDb.user_info (id),
    constraint FK_ANSWER_ON_SENDER
        foreign key (sender_id) references thRedDb.user_info (id)
);

CREATE TABLE IF NOT EXISTS thRedDb.receipt
(
    id                      bigint auto_increment
        primary key,
    user_id                 bigint       null,
    product_id              bigint       null,
    purchase_date           datetime(6)  not null,
    original_transaction_id varchar(255) not null,
    receipt_data            varchar(255) not null,
    transaction_id          varchar(255) not null,
    created_date            datetime(6)  null,
    last_modified_date      datetime(6)  null,
    constraint receipt_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id),
    constraint receipt_ibfk_2
        foreign key (product_id) references thRedDb.product (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.refresh_token
(
    id            bigint auto_increment
        primary key,
    user_id       bigint       null,
    refresh_token varchar(255) null,
    constraint uc_refreshtoken_user
        unique (user_id),
    constraint FK_REFRESHTOKEN_ON_USER
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.report
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime     null,
    last_modified_date datetime     null,
    report_type        varchar(255) not null,
    content            longtext     not null,
    reason             varchar(255) not null,
    reporter_id        bigint       null,
    reported_user_id   bigint       null,
    target_id          bigint       not null,
    evidence_url       varchar(255) not null,
    constraint FK_REPORT_ON_FROM
        foreign key (reporter_id) references thRedDb.user_info (id),
    constraint FK_REPORT_ON_TO
        foreign key (reported_user_id) references thRedDb.user_info (id)
);

CREATE TABLE IF NOT EXISTS thRedDb.review
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime     null,
    last_modified_date datetime     null,
    user_id            bigint       null,
    review_status      varchar(255) null,
    review_type        varchar(255) null,
    reason             varchar(255) null,
    constraint FK_JUDGMENTS_ON_USER
        foreign key (user_id) references thRedDb.user_info (id)
);

CREATE TABLE IF NOT EXISTS thRedDb.thread_use_history
(
    id                      bigint auto_increment
        primary key,
    user_id                 bigint                                                                     null,
    purchase_target_user_id bigint                                                                     null,
    purchase_type           enum ('ANSWER_QUESTION', 'VIEW_PROFILE', 'REFERRAL_CODE', 'THREAD_TOP_UP') not null,
    amount                  int                                                                        not null,
    created_date            datetime(6)                                                                null,
    last_modified_date      datetime(6)                                                                null,
    constraint thread_use_history_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.user_asset
(
    id                 bigint auto_increment
        primary key,
    user_id            bigint        null,
    total_thread       int default 0 not null,
    created_date       datetime(6)   null,
    last_modified_date datetime(6)   null,
    constraint user_asset_ibfk_1
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

CREATE TABLE IF NOT EXISTS thRedDb.user_detail
(
    id                 bigint auto_increment
        primary key,
    created_date       datetime     null,
    last_modified_date datetime     null,
    user_id            bigint       null,
    height             int          null,
    drink              varchar(255) null,
    belief             varchar(255) null,
    smoke              varchar(255) null,
    opposite_friends   varchar(255) null,
    mbti               varchar(255) null,
    job                varchar(255) null,
    temperature        int          null,
    constraint uc_details_user
        unique (user_id),
    constraint FK_DETAILS_ON_USER
        foreign key (user_id) references thRedDb.user_info (id)
)
    charset = utf8mb3;

