CREATE TABLE IF NOT EXISTS posts (
    uri VARCHAR(255) PRIMARY KEY,
    author VARCHAR(255),
    content TEXT,
    lang VARCHAR(25),
    timestamp VARCHAR(80),
    sentimentScore NUMERIC(3, 2)
);
