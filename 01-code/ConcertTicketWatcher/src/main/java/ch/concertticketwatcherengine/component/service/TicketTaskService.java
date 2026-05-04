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
    private static final long MAX_WAIT_YEARS  = 2;
    private static final long MAX_CHECKS = MAX_WAIT_YEARS * ThreadUtil.DAYS_IN_YEAR * ThreadUtil.HOURS_IN_DAY / CHECK_INTERVAL_HOURS;
    private final TicketmasterTicketFetcher ticketFetcher;
    private final EmailService emailService;

    public TicketTaskService(TicketmasterTicketFetcher ticketFetcher, EmailService emailService) {
        this.ticketFetcher = ticketFetcher;
        this.emailService  = emailService;
    }

    @Override
    public void execute() {
        String eventId   = (String)  receivedData.getOrDefault("eventId", "");
        String eventName = (String)  receivedData.getOrDefault("eventName", "");
        String eventUrl  = (String)  receivedData.getOrDefault("eventUrl", "");

        List<String> recipients = buildRecipientList();
        if (recipients.isEmpty()) return;

        String presaleDate = null;
        for (long attempt = 0; attempt < MAX_CHECKS; attempt++) {
            Ticket ticket = fetchTicket(eventId);
            if (ticket == null) {
                if (attempt < MAX_CHECKS - 1) ThreadUtil.sleepHours(CHECK_INTERVAL_HOURS);
                continue;
            }
            if (presaleDate == null && ticket.getPresaleDate() != null) {
                presaleDate = ticket.getPresaleDate();
                schedulePresaleReminder(presaleDate, eventName, eventUrl, recipients);
            }
            if (ticket.isAvailable()) {
                sendTicketAvailableEmails(recipients, eventName, eventUrl, ticket.getPrice());
                return;
            }
            if (attempt < MAX_CHECKS - 1) ThreadUtil.sleepHours(CHECK_INTERVAL_HOURS);
        }

        throw new RuntimeException("{TicketTaskService} Ticket watcher timed out after " + MAX_WAIT_YEARS + " years for: " + eventName);
    }



    // |----- helper methods -----|

    private List<String> buildRecipientList() {
        String  userEmail = (String)  receivedData.getOrDefault("email",    "");
        boolean isGoing   = (Boolean) receivedData.getOrDefault("isGoing",  false);
        Object raw = receivedData.get("inviteeList");
        List<Map<String, String>> invitees = raw != null ? (List<Map<String, String>>) raw : List.of();

        List<String> recipients = new ArrayList<>();

        if (isGoing && !userEmail.isBlank()) recipients.add(userEmail);

        for (Map<String, String> invitee : invitees) {
            boolean inviteeIsGoing = Boolean.parseBoolean(invitee.getOrDefault("isGoing", "false"));
            String  inviteeEmail   = invitee.getOrDefault("email", "");
            if (inviteeIsGoing && !inviteeEmail.isBlank()) recipients.add(inviteeEmail);
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

    private void schedulePresaleReminder(String presaleDate, String eventName, String eventUrl, List<String> recipients) {
        new Thread(() -> {
            try {
                LocalDateTime presale      = LocalDateTime.parse(presaleDate, DateTimeFormatter.ISO_DATE_TIME);
                LocalDateTime reminderTime = presale.minusMinutes(15);
                LocalDateTime now          = LocalDateTime.now();

                if (reminderTime.isAfter(now)) {
                    ThreadUtil.sleepMillis(Duration.between(now, reminderTime).toMillis());
                    for (String email : recipients) {
                        emailService.sendPresaleReminderEmail(email, eventName, eventUrl, presaleDate);
                    }
                }
            } catch (Exception e) {
                Log.error("{TicketTaskService} Presale reminder failed: " + e.getMessage());
            }
        }).start();
    }

    private void sendTicketAvailableEmails(List<String> recipients, String eventName,
                                            String eventUrl, String price) {
        for (String email : recipients) {
            try {
                emailService.sendTicketsAvailableEmail(email, eventName, eventUrl, price);
            } catch (Exception e) {
                Log.error("{TicketTaskService} Failed to send email to " + email + ": " + e.getMessage());
            }
        }
    }
}