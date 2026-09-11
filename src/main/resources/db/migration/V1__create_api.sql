CREATE SEQUENCE api_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE api
(
    id            BIGINT       NOT NULL PRIMARY KEY,
    uuid          UUID         NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL UNIQUE,
    base_path     VARCHAR(255) NOT NULL UNIQUE,
    status        VARCHAR(20)  NOT NULL,
    owner_team    VARCHAR(255) NOT NULL,
    open_api_spec TEXT,
    created_at    TIMESTAMP WITH TIME ZONE,
    updated_at    TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE consumer_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE consumer
(
    id            BIGINT       NOT NULL PRIMARY KEY,
    uuid          UUID         NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    organisation  VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE,
    updated_at    TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE subscription_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE subscription
(
    id            BIGINT       NOT NULL PRIMARY KEY,
    uuid          UUID         NOT NULL UNIQUE,
    consumer_id   BIGINT       NOT NULL,
    api_id        BIGINT       NOT NULL,
    plan          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_subscription_consumer FOREIGN KEY (consumer_id)
    REFERENCES consumer(id),

    CONSTRAINT fk_subscription_api FOREIGN KEY (api_id)
    REFERENCES api(id)
);

CREATE SEQUENCE apikey_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE api_key
(
    id              BIGINT       NOT NULL PRIMARY KEY,
    uuid            UUID         NOT NULL UNIQUE,
    key_hash         VARCHAR(255) NOT NULL UNIQUE,
    prefix          VARCHAR(255) NOT NULL UNIQUE,
    subscription_id BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE,
    expires_at      TIMESTAMP WITH TIME ZONE,
    revoked_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_api_key_subscription FOREIGN KEY (subscription_id)
    REFERENCES subscription(id)
);



