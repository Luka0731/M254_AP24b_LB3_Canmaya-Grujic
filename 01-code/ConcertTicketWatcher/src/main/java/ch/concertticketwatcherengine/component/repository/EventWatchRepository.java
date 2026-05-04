package ch.concertticketwatcherengine.component.repository;

import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.core.exception.DatabaseException;
import ch.concertticketwatcherengine.core.generic.Repository;
import ch.concertticketwatcherengine.core.util.Log;
import java.sql.*;

public class EventWatchRepository extends Repository<Event> {

    @Override
    protected Event mapSqlRowToModel(ResultSet rs) {
        return null;
    }

    /**
     * Checks if this event has already been seen and the user notified.
     *
     * @param ticketmasterEventId the Ticketmaster event ID
     * @return true if already notified, false if new
     */
    public boolean hasSeenEvent(String ticketmasterEventId) throws DatabaseException {
        Log.debug("{EventWatchRepository} Checking if event seen: " + ticketmasterEventId);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM event_watch WHERE ticketmaster_event_id = ?"
             )) {
            stmt.setString(1, ticketmasterEventId);
            ResultSet rs   = stmt.executeQuery();
            boolean  seen  = rs.next();
            Log.debug("{EventWatchRepository} Event " + ticketmasterEventId + " seen: " + seen);
            return seen;
        } catch (Exception e) {
            throw new DatabaseException("hasSeenEvent", e.getMessage());
        }
    }

    /**
     * Marks an event as seen so the user won't be notified again.
     *
     * @param ticketmasterEventId the Ticketmaster event ID
     */
    public void markEventAsSeen(String ticketmasterEventId) throws DatabaseException {
        Log.debug("{EventWatchRepository} Marking event as seen: " + ticketmasterEventId);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO event_watch (ticketmaster_event_id, already_notified) VALUES (?, true)"
             )) {
            stmt.setString(1, ticketmasterEventId);
            stmt.executeUpdate();
            Log.debug("{EventWatchRepository} Event marked: " + ticketmasterEventId);
        } catch (Exception e) {
            throw new DatabaseException("markEventAsSeen", e.getMessage());
        }
    }
}
