-- Emails are now trimmed and lower-cased on registration and login. Bring existing
-- rows in line so those users can still log in. Fails loudly (rather than with an
-- opaque unique-constraint error) if two accounts collide once normalised, so the
-- duplicates can be merged or removed by hand first.
DO $$
DECLARE
    dup RECORD;
BEGIN
    FOR dup IN
        SELECT lower(btrim(email)) AS normalized, count(*) AS n
        FROM users
        GROUP BY lower(btrim(email))
        HAVING count(*) > 1
    LOOP
        RAISE EXCEPTION 'Cannot normalize user emails: % accounts share %', dup.n, dup.normalized;
    END LOOP;
END $$;

UPDATE users
SET email = lower(btrim(email))
WHERE email <> lower(btrim(email));
