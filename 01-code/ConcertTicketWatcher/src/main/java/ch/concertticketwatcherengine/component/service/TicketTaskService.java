package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.component.fetcher.TicketmasterTicketFetcher;
import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.core.generic.Service;
import ch.concertticketwatcherengine.core.util.Log;
import ch.concertticketwatcherengine.core.util.ThreadUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketTaskService extends Service {

    private static final long CHECK_INTERVAL_HOURS = 6;
    private static final long MAX_WAIT_YEARS = 2;
    private static final long MAX_CHECKS = MAX_WAIT_YEARS * ThreadUtil.DAYS_IN_YEAR * ThreadUtil.HOURS_IN_DAY / CHECK_INTERVAL_HOURS;
    private final TicketmasterTicketFetcher ticketFetcher;
    private final EmailService emailService;

    public TicketTaskService(TicketmasterTicketFetcher ticketFetcher, EmailService emailService) {
        this.ticketFetcher = ticketFetcher;
        this.emailService = emailService;
    }

    @Override
    public void execute() {
        String eventId = (String) receivedData.getOrDefault("eventId", "");
        String eventName = (String) receivedData.getOrDefault("eventName", "");
        String eventVenue = (String) receivedData.getOrDefault("eventVenue", "");
        String eventCity = (String) receivedData.getOrDefault("eventCity", "");
        String eventDate = (String) receivedData.getOrDefault("eventDate", "");
        String eventUrl = (String) receivedData.getOrDefault("eventUrl", "");

        List<String[]> recipients = buildRecipientList();
        Log.debug("{TicketTaskService} Recipients: " + recipients.size());
        if (recipients.isEmpty()) {
            Log.debug("{TicketTaskService} No recipients — skipping ticket watch");
            return;
        }

        String presaleDate = null;
        for (long attempt = 0; attempt < MAX_CHECKS; attempt++) {
            Log.debug("{TicketTaskService} Ticket poll attempt " + (attempt + 1));
            Ticket ticket = fetchTicket(eventId);
            if (ticket == null) {
                if (attempt < MAX_CHECKS - 1) ThreadUtil.sleepHours(CHECK_INTERVAL_HOURS);
                continue;
            }
            Log.debug("{TicketTaskService} Ticket status — available: " + ticket.isAvailable() + " | price: " + ticket.getPrice());
            if (presaleDate == null && ticket.getPresaleDate() != null) {
                presaleDate = ticket.getPresaleDate();
                schedulePresaleReminder(presaleDate, recipients, eventName, eventVenue, eventCity, eventDate, eventUrl);
            }
            if (ticket.isAvailable()) {
                sendTicketAvailableEmails(recipients, eventName, eventVenue, eventCity, eventDate, eventUrl, ticket.getPrice());
                return;
            }
            if (attempt < MAX_CHECKS - 1) ThreadUtil.sleepHours(CHECK_INTERVAL_HOURS);
        }

        throw new RuntimeException("{TicketTaskService} Ticket watcher timed out after " + MAX_WAIT_YEARS + " years for: " + eventName);
    }



    // |----- helper methods -----|

    // Returns list of [email, userName] pairs
    private List<String[]> buildRecipientList() {
        String userEmail = (String) receivedData.getOrDefault("email", "");
        String userName = (String) receivedData.getOrDefault("userName", "");
        boolean isGoing = Boolean.TRUE.equals(receivedData.get("isGoing"));
        Object raw = receivedData.get("inviteeList");
        List<Map<String, String>> invitees = raw != null ? (List<Map<String, String>>) raw : List.of();

        List<String[]> recipients = new ArrayList<>();

        if (isGoing && !userEmail.isBlank()) {
            Log.debug("{TicketTaskService} Adding user to recipients: " + userEmail);
            recipients.add(new String[]{userEmail, userName});
        }

        for (Map<String, String> invitee : invitees) {
            String inviteeEmail = invitee.getOrDefault("email", "");
            String inviteeName = invitee.getOrDefault("username", "");
            if (!inviteeEmail.isBlank()) {
                Log.debug("{TicketTaskService} Adding invitee to recipients: " + inviteeEmail);
                recipients.add(new String[]{inviteeEmail, inviteeName});
            }
        }

        return recipients;
    }

    private Ticket fetchTicket(String eventId) {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put("eventId", eventId);
            return ticketFetcher.fetch(filters);
        } catch (Exception e) {
            Log.error("{TicketTaskService} Fetch error, retrying next cycle: " + e.getMessage());
            return null;
        }
    }

    private void schedulePresaleReminder(String presaleDate, List<String[]> recipients,
                                         String eventName, String eventVenue, String eventCity,
                                         String eventDate, String eventUrl) {
        new Thread(() -> {
            try {
                LocalDateTime presale = LocalDateTime.parse(presaleDate, DateTimeFormatter.ISO_DATE_TIME);
                LocalDateTime reminderTime = presale.minusMinutes(15);
                LocalDateTime now = LocalDateTime.now();
                if (reminderTime.isAfter(now)) {
                    ThreadUtil.sleepMillis(Duration.between(now, reminderTime).toMillis());
                    for (String[] recipient : recipients) {
                        emailService.sendPresaleReminderEmail(recipient[0], recipient[1],
                            eventName, eventVenue, eventCity, eventDate, eventUrl, presaleDate);
                    }
                }
            } catch (Exception e) {
                Log.error("{TicketTaskService} Presale reminder failed: " + e.getMessage());
            }
        }).start();
    }

    private void sendTicketAvailableEmails(List<String[]> recipients, String eventName, String eventVenue, String eventCity, String eventDate, String eventUrl, String price) {
        for (String[] recipient : recipients) {
            try {
                emailService.sendTicketsAvailableEmail(recipient[0], recipient[1],
                    eventName, eventVenue, eventCity, eventDate, eventUrl, price);
            } catch (Exception e) {
                Log.error("{TicketTaskService} Failed to send email to " + recipient[0] + ": " + e.getMessage());
            }
        }
    }
}