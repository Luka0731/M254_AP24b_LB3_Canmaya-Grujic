package ch.concertticketwatcherengine.component.fetcher;

import ch.concertticketwatcherengine.component.model.CamundaUser;
import ch.concertticketwatcherengine.core.generic.Fetcher;
import org.json.JSONObject;
import java.util.Map;

public class CamundaUserFetcher extends Fetcher<CamundaUser> {

    @Override
    protected String buildUrl(Map<String, String> filters) {
        return "http://localhost:8080/engine-rest/user/" + filters.get("username") + "/profile";
    }

    @Override
    protected CamundaUser mapResponseJsonToModel(StringBuilder response) throws Exception {
        JSONObject profile = new JSONObject(response.toString());
        String email = profile.optString("email", "");
        if (email.isBlank()) return null;

        CamundaUser user = new CamundaUser();
        user.setUsername(profile.optString("id", ""));
        user.setEmail(email);
        return user;
    }
}