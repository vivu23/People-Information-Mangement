package mvc.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "session")
public class Session {
    private static final Logger LOGGER = LogManager.getLogger();

    @Id
    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user_id;

    public Session(){
    }

    public Session(String token, User user_id) {
        this.token = token;
        this.user_id = user_id;
    }

    public static Session convertSessionJSONObject(JSONObject json) {
        try {
            String uInfo = json.get("user_id") + "";
            JSONObject uJson = new JSONObject(uInfo);
            LOGGER.info(uJson);
            Session session = new Session(json.getString("token"), User.convertUserJSONObject(uJson));
            return session;
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to parse session from provided json: " + json.toString());
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser_id() {
        return user_id;
    }

    public void setUser_id(User user_id) {
        this.user_id = user_id;
    }


//TODO: Auto-generate session token using SHA hash

}
