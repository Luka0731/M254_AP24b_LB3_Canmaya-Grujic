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
     * Checks if this process instance has already been notified about this event.
     *
     * @param processInstanceId   the Camunda process instance id of the watcher
     * @param ticketmasterEventId the Ticketmaster event ID
     * @return true if already notified, false if new
     */
    public boolean hasSeenEvent(String processInstanceId, String ticketmasterEventId) throws DatabaseException {
        Log.debug("{EventWatchRepository} Checking if event seen: " + ticketmasterEventId + " for process: " + processInstanceId);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM event_watch WHERE process_instance_id = ? AND ticketmaster_event_id = ?"
             )) {
            stmt.setString(1, processInstanceId);
            stmt.setString(2, ticketmasterEventId);
            boolean seen = stmt.executeQuery().next();
            Log.debug("{EventWatchRepository} Event " + ticketmasterEventId + " seen: " + seen);
            return seen;
        } catch (Exception e) {
            throw new DatabaseException("hasSeenEvent", e.getMessage());
        }
    }

    /**
     * Marks an event as seen for this specific process instance.
     *
     * @param processInstanceId   the Camunda process instance id of the watcher
     * @param ticketmasterEventId the Ticketmaster event ID
     */
    public void markEventAsSeen(String processInstanceId, String ticketmasterEventId) throws DatabaseException {
        Log.debug("{EventWatchRepository} Marking event as seen: " + ticketmasterEventId + " for process: " + processInstanceId);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO event_watch (process_instance_id, ticketmaster_event_id, already_notified) VALUES (?, ?, true)"
             )) {
            stmt.setString(1, processInstanceId);
            stmt.setString(2, ticketmasterEventId);
            stmt.executeUpdate();
            Log.debug("{EventWatchRepository} Event marked: " + ticketmasterEventId);
        } catch (Exception e) {
            throw new DatabaseException("markEventAsSeen", e.getMessage());
        }
    }
}