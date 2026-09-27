-- Chat media references use the same object storage boundary as posts.
CREATE TABLE chat_message_media (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES chat_messages (id) ON DELETE CASCADE,
    kind TEXT NOT NULL CHECK (kind IN ('IMAGE', 'AUDIO')),
    object_key TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
    duration_millis BIGINT,
    position SMALLINT NOT NULL CHECK (position BETWEEN 0 AND 8)
);

CREATE INDEX chat_message_media_idx ON chat_message_media (message_id, position);