CREATE TABLE announcements (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL CHECK (char_length(title) BETWEEN 1 AND 200),
    body TEXT NOT NULL CHECK (char_length(body) BETWEEN 1 AND 10000),
    deep_link TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_reads (
    user_id UUID NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    announcement_id UUID NOT NULL REFERENCES announcements (id) ON DELETE CASCADE,
    read_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, announcement_id)
);

CREATE INDEX announcements_created_idx ON announcements (created_at DESC, id DESC);