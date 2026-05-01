CREATE TABLE IF NOT EXISTS event_watch (
    id SERIAL PRIMARY KEY,
    ticketmaster_event_id VARCHAR(255) NOT NULL,
    already_notified BOOLEAN NOT NULL DEFAULT FALSE
);