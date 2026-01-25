CREATE TABLE IF NOT EXISTS players
(
    id
    CHAR
(
    36
) PRIMARY KEY,
    name VARCHAR
(
    255
) NOT NULL,
    hand_cards TEXT,
    status VARCHAR
(
    50
),
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    pushes INT DEFAULT 0
    );
