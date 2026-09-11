-- templates.thumbnail_url previously stored a full absolute URL hardcoded to
-- http://localhost:9000/posterpro/..., which only resolves for a client that
-- can reach "localhost" on the developer's own machine (desktop, iOS
-- simulator) — it never worked from the Android emulator (where "localhost"
-- means the emulator itself, not the host) or a physical device. The backend
-- now stores a bucket-relative object key here instead and builds the full
-- URL per environment from storage.s3.public-base-url (see TemplateService).
--
-- Rows that already hold a full external URL (the two remaining
-- example.com placeholders, ids 3-4) are left untouched — the backend
-- passes any value already starting with http(s):// straight through.
UPDATE templates
SET thumbnail_url = regexp_replace(thumbnail_url, '^https?://[^/]+/posterpro/', '')
WHERE thumbnail_url ~ '^https?://[^/]+/posterpro/';
