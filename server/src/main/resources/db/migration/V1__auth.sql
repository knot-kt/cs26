-- Authentication persistence boundary. Access tokens are stored as hashes, never plaintext.
CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    phone_e164 TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_code_challenges (
    id UUID PRIMARY KEY,
    phone_e164 TEXT NOT NULL,
    code_hash BYTEA NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    resend_available_at TIMESTAMPTZ NOT NULL,
    attempts_remaining SMALLINT NOT NULL CHECK (attempts_remaining BETWEEN 0 AND 5),
    consumed_at TIMESTAMPTZ
);

CREATE INDEX auth_code_challenges_phone_idx
    ON auth_code_challenges (phone_e164, expires_at);

CREATE TABLE auth_sessions (
    access_token_hash BYTEA PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX auth_sessions_user_idx
    ON auth_sessions (user_id, expires_at);
