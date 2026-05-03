package ch.concertticketwatcherengine.core.setup;

import ch.concertticketwatcherengine.component.fetcher.CamundaUserFetcher;
import ch.concertticketwatcherengine.component.fetcher.GeolocationFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterEventFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterTicketFetcher;
import ch.concertticketwatcherengine.component.repository.EventWatchRepository;
import ch.concertticketwatcherengine.component.service.ConcertTaskService;
import ch.concertticketwatcherengine.component.service.EmailService;
import ch.concertticketwatcherengine.component.service.InviteeTaskService;
import ch.concertticketwatcherengine.component.service.TicketTaskService;
import ch.concertticketwatcherengine.component.handler.ConcertTaskHandler;
import ch.concertticketwatcherengine.component.handler.InviteeTaskHandler;
import ch.concertticketwatcherengine.component.handler.TicketTaskHandler;
import ch.concertticketwatcherengine.core.generic.Repository;
import ch.concertticketwatcherengine.core.util.Log;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;

public class Setup {

    static public void setup() {

        // |----- db setup -----|
        Repository.setDatabaseCredentials(
            "jdbc:postgresql://localhost:5432/concertdb",
            "admin",
            "admin"
        );
        Log.success("DB setup done");



        // |----- components setup -----|
        EventWatchRepository eventWatchRepository = new EventWatchRepository();
        // fetchers
        GeolocationFetcher geolocationFetcher = new GeolocationFetcher();
        TicketmasterEventFetcher ticketmasterEventFetcher = new TicketmasterEventFetcher();
        TicketmasterTicketFetcher ticketmasterTicketFetcher = new TicketmasterTicketFetcher();
        CamundaUserFetcher camundaUserFetcher = new CamundaUserFetcher();
        // services
        EmailService emailService = new EmailService();
        ConcertTaskService concertTaskService = new ConcertTaskService(geolocationFetcher, ticketmasterEventFetcher, eventWatchRepository);
        InviteeTaskService inviteeTaskService = new InviteeTaskService(camundaUserFetcher);
        TicketTaskService ticketTaskService = new TicketTaskService(ticketmasterTicketFetcher, emailService);
        // handlers
        ExternalTaskHandler concertTaskHandler = new ConcertTaskHandler(concertTaskService);
        ExternalTaskHandler inviteeTaskHandler = new InviteeTaskHandler(inviteeTaskService);
        ExternalTaskHandler ticketTaskHandler  = new TicketTaskHandler(ticketTaskService);
        Log.success("Components setup done");



        // |----- camunda setup -----|
        ExternalTaskClient camundaClient = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .build();
        camundaClient.subscribe("fetch-events")
                .handler(concertTaskHandler)
                .open();
        camundaClient.subscribe("validate-invitees")
                .handler(inviteeTaskHandler)
                .open();
        camundaClient.subscribe("fetch-ticket-availability")
                .handler(ticketTaskHandler)
                .open();
        Log.success("Camunda setup done");
        Log.success("All setup done");
    }
}