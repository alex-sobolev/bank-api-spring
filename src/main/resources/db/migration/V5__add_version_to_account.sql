 -- add version to ACCOUNT table for optimistic locking
 ALTER TABLE account ADD COLUMN version INT NOT NULL DEFAULT 0;
