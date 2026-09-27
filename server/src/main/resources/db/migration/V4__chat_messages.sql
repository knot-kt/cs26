-- Client message IDs make retries idempotent for each sender and conversation.
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    client_message_id TEXT NOT NULL,
    content TEXT NOT NULL CHECK (char_length(content) BETWEEN 1 AND 4000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (conversation_id, sender_id, client_message_id)
);

CREATE INDEX chat_messages_history_idx ON chat_messages (conversation_id, created_at, id);