-- Media metadata references object storage; binary content remains outside PostgreSQL.
CREATE TABLE post_media (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    kind TEXT NOT NULL CHECK (kind IN ('IMAGE', 'AUDIO')),
    object_key TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
    duration_millis BIGINT,
    position SMALLINT NOT NULL CHECK (position BETWEEN 0 AND 8)
);

CREATE INDEX post_media_post_idx ON post_media (post_id, position);