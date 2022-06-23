package mvc.model;

import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "audit_trail")
public class AuditTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "change_msg", nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User user_id;

    @ManyToOne
    @JoinColumn(name = "personId")
    private Person personId;

    @Column(name ="when_occured")
    private String time;

    public AuditTrail(String message, User user_id, Person personId) {
        this.message = message;
        this.user_id = user_id;
        this.personId = personId;
    }

    public AuditTrail(String message, User user_id, String time) {
        this.message = message;
        this.user_id = user_id;
        this.time = time;
    }

    public AuditTrail(int id, String message, User user_id, Person personId, String time) {
        this.id = id;
        this.message = message;
        this.user_id = user_id;
        this.personId = personId;
        this.time = time;
    }

    public AuditTrail() {

    }

    public static AuditTrail fromJSONObjectToAuditTrail(JSONObject json) {
        try {
            String uInfo = json.get("user_id") + "";
            JSONObject uJson = new JSONObject(uInfo);
            String pInfo = json.get("personId") + "";
            JSONObject pJson = new JSONObject(pInfo);
            Person person = Person.fromJSONObject(pJson);
            User user = User.convertUserJSONObject(uJson);
            AuditTrail auditTrail = new AuditTrail(json.getInt("id"), json.getString("message"), user, person, json.getString("time"));
            return auditTrail;
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to parse person from provided json: " + json.toString());
        }
    }

    @Override
    public String toString() {
        return "AuditTrail{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", user_id=" + user_id +
                ", personId=" + personId +
                ", time='" + time + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser_id() {
        return user_id;
    }

    public void setUser_id(User user_id) {
        this.user_id = user_id;
    }

    public Person getPersonId() {
        return personId;
    }

    public void setPersonId(Person personId) {
        this.personId = personId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        String username = user_id.getUserLogin();
        return username;
    }

}

