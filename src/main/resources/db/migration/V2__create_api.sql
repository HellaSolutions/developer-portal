CREATE UNIQUE INDEX idx_unique_not_revoked_subscription
    ON subscription (consumer_id, api_id) WHERE status <> 'REVOKED';
