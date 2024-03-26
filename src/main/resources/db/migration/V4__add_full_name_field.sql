ALTER TABLE CUSTOMER ADD COLUMN full_name TEXT NOT NULL DEFAULT '';

UPDATE CUSTOMER SET full_name = first_name || ' ' || last_name;

ALTER TABLE CUSTOMER ALTER COLUMN FULL_NAME drop DEFAULT;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_customer_full_name ON CUSTOMER USING gin (full_name gin_trgm_ops);
