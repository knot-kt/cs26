-- Media posts may omit text; the API still requires text or at least one attachment.
ALTER TABLE posts DROP CONSTRAINT posts_content_check;
ALTER TABLE posts ADD CONSTRAINT posts_content_check CHECK (char_length(content) BETWEEN 0 AND 2000);