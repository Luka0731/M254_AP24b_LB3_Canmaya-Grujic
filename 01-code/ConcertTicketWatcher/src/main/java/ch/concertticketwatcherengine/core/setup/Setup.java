package ch.concertticketwatcherengine.core.setup;

import ch.concertticketwatcherengine.component.fetcher.GeolocationFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterEventFetcher;
import ch.concertticketwatcherengine.component.fetcher.TicketmasterTicketFetcher;
import ch.concertticketwatcherengine.component.model.Event;
import ch.concertticketwatcherengine.component.model.Ticket;
import ch.concertticketwatcherengine.component.model.User;
import ch.concertticketwatcherengine.component.repository.UserRepository;
import ch.concertticketwatcherengine.component.service.ConcertTaskService;
import ch.concertticketwatcherengine.component.service.InviteeTaskService;
import ch.concertticketwatcherengine.component.service.TicketTaskService;
import ch.concertticketwatcherengine.component.handler.ConcertTaskHandler;
import ch.concertticketwatcherengine.component.handler.InviteeTaskHandler;
import ch.concertticketwatcherengine.component.handler.TicketTaskHandler;
import ch.concertticketwatcherengine.core.generic.Repository;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;

public class Setup {

    static public void setup() {
        // |----- class creation and injection -----|
        // repositories
        UserRepository userRepository = new UserRepository();
        // fetchers
        GeolocationFetcher geolocationFetcher = new GeolocationFetcher();
        TicketmasterEventFetcher ticketmasterEventFetcher = new TicketmasterEventFetcher();
        TicketmasterTicketFetcher ticketmasterTicketFetcher = new TicketmasterTicketFetcher();
        // services
        ConcertTaskService concertTaskService = new ConcertTaskService(geolocationFetcher, ticketmasterEventFetcher);
        InviteeTaskService inviteeTaskService = new InviteeTaskService();
        TicketTaskService ticketTaskService = new TicketTaskService(ticketmasterTicketFetcher);
        // handlers
        ExternalTaskHandler concertTaskHandler = new ConcertTaskHandler(concertTaskService);
        ExternalTaskHandler inviteeTaskHandler = new InviteeTaskHandler(inviteeTaskService);
        ExternalTaskHandler ticketTaskHandler = new TicketTaskHandler(ticketTaskService);



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



        // |----- db setup -----|
        Repository.setDatabaseCredentials("jdbc:postgresql://localhost:5432/concertdb", "admin", "admin");
    }
}