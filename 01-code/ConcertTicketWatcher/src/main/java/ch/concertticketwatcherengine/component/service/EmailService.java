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
        this.senderEmail    = Config.get("email.sender");
        this.senderPassword = Config.get("email.password");
        this.session        = createMailSession();
    }

    public void sendTicketsAvailableEmail(String to, String eventName,
                                          String eventUrl, String price) throws Exception {
        String subject = "🎟️ Tickets available: " + eventName;
        String body    = "Great news! Tickets for " + eventName + " are now available.\n\n"
                       + "Price: " + price + "\n"
                       + "Buy here: " + eventUrl + "\n\n"
                       + "Get them before they sell out!";
        send(to, subject, body);
    }

    public void sendPresaleReminderEmail(String to, String eventName,
                                          String eventUrl, String presaleDate) throws Exception {
        String subject = "🎟️⏰ Tickets dropping soon: " + eventName;
        String body    = "Heads up! Tickets for " + eventName + " go on sale at " + presaleDate + ".\n\n"
                       + "Get ready here: " + eventUrl + "\n\n"
                       + "Tickets drop in ~15 minutes!";
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
    }
}