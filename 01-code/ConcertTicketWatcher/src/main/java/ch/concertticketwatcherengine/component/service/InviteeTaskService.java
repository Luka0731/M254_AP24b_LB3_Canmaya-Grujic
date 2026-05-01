package ch.concertticketwatcherengine.component.service;

import ch.concertticketwatcherengine.core.generic.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InviteeTaskService extends Service {

    private static final String CAMUNDA_BASE = "http://localhost:8080/engine-rest";

    @Override
    public void execute() {
        String raw = (String) receivedData.getOrDefault("inviteeUsernames", "");
        System.out.println("[InviteeTaskService] Raw invitee input: " + raw);

        List<String> validUsernames = new ArrayList<>();

        if (raw != null && !raw.isBlank()) {
            String[] parts = raw.split(",");
            for (String part : parts) {
                String username = part.strip();
                if (!username.isEmpty() && isValidCamundaUser(username)) {
                    validUsernames.add(username);
                    System.out.println("[InviteeTaskService] Valid user: " + username);
                } else {
                    System.out.println("[InviteeTaskService] Skipping unknown user: " + username);
                }
            }
        }

        returnData.put("inviteeList", validUsernames);
        System.out.println("[InviteeTaskService] Final invitee list: " + validUsernames);
    }

    // Checks if a username exists in Camunda by calling the identity REST API
    private boolean isValidCamundaUser(String username) {
        try {
            URL url = new URL(CAMUNDA_BASE + "/user?id=" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code != 200) return false;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // Camunda returns an array (if existing)
            String body = sb.toString().trim();
            return !body.equals("[]");
        } catch (Exception e) {
            System.out.println("[InviteeTaskService] Could not validate user '" + username + "': " + e.getMessage());
            return false;
        }
    }
}