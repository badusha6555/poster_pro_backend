-- Replaces the placeholder thumbnail for "Diamond Drop Earrings" with a real
-- image uploaded to MinIO under the public-read templates/ prefix
-- (see posterpro-minio bucket policy: anonymous s3:GetObject on templates/*).
UPDATE templates
SET thumbnail_url = 'http://localhost:9000/posterpro/templates/2/thumbnail.jpg'
WHERE title = 'Diamond Drop Earrings';
