CREATE TABLE IF NOT EXISTS event_watch (
    id SERIAL PRIMARY KEY,
    process_instance_id VARCHAR(255) NOT NULL,
    ticketmaster_event_id VARCHAR(255) NOT NULL,
    already_notified BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (process_instance_id, ticketmaster_event_id)
);