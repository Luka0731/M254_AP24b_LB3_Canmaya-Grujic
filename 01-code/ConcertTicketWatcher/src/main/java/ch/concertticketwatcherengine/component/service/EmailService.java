package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.core.setup.Config;
import ch.concertticketwatcherengine.core.util.Log;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String senderEmail;
    private final String senderPassword;
    private final Session session;

    public EmailService() {
        this.senderEmail = Config.get("email.sender");
        this.senderPassword = Config.get("email.password");
        this.session = createMailSession();
    }

    public void sendTicketsAvailableEmail(String to, String userName, String eventName,
                                          String eventVenue, String eventCity, String eventDate,
                                          String eventUrl, String price) throws Exception {
        String subject = "Tickets are on sale now: " + eventName;
        String body =
            "Heyy " + userName + "!\n\n"
            + "Great news — tickets for the concert you have been watching are now available!\n\n"
            + "--- Concert Details ---\n"
            + "Artist:   " + eventName + "\n"
            + "Venue:    " + eventVenue + "\n"
            + "City:     " + eventCity + "\n"
            + "Date:     " + eventDate + "\n"
            + "Price:    " + price + "\n\n"
            + "Buy your tickets here:\n"
            + eventUrl + "\n\n"
            + "Don't wait too long — they sell out fast!\n\n"
            + "--- Concert Ticket Watcher ---\n"
            + "You received this email because you set up a watcher for this artist.";
        send(to, subject, body);
    }

    public void sendPresaleReminderEmail(String to, String userName, String eventName,
                                          String eventVenue, String eventCity, String eventDate,
                                          String eventUrl, String presaleDate) throws Exception {
        String subject = "Tickets dropping in 15 minutes: " + eventName;
        String body =
            "Heyy " + userName + "!\n\n"
            + "Tickets for the concert you are watching go on presale in about 15 minutes!\n\n"
            + "--- Concert Details ---\n"
            + "Artist:   " + eventName + "\n"
            + "Venue:    " + eventVenue + "\n"
            + "City:     " + eventCity + "\n"
            + "Date:     " + eventDate + "\n"
            + "Sale starts: " + presaleDate + "\n\n"
            + "Get ready here:\n"
            + eventUrl + "\n\n"
            + "--- Concert Ticket Watcher ---\n"
            + "You received this email because you set up a watcher for this artist.";
        send(to, subject, body);
    }

    public void sendWatcherStartedEmail(String to, String userName, String artistName) throws Exception {
        String subject = "Watcher started: " + artistName;
        String body =
            "Hey " + userName + "!\n\n"
            + "Your Concert Ticket Watcher is now active for " + artistName + ".\n\n"
            + "We will notify you as soon as a concert is announced in your area "
            + "and again when tickets go on sale.\n\n"
            + "You don't have to do anything — just sit back and wait!\n\n"
            + "--- Concert Ticket Watcher ---";
        send(to, subject, body);
    }



    // |----- helper methods -----|

    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    private void send(String to, String subject, String body) throws Exception {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
        Log.success("{EmailService} Email sent to: " + to + " | subject: " + subject);
    }
}