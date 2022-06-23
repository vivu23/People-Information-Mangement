package springboot.services;

import mvc.model.Session;
import mvc.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import pw_hash.HashUtils;
import springboot.repositories.SessionRepository;
import springboot.repositories.UserRepository;

import java.util.UUID;

@RestController
public class UserController {
    private static final Logger logger = LogManager.getLogger();

    private UserRepository userRepository;
    private SessionRepository sessionRepository;

    public static String TOKEN;
    public static User USER;

    public UserController(UserRepository userRepository, SessionRepository sessionRepository){
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Session> userLogin(@RequestBody String loginInfo) {
        JSONObject jsonObj = new JSONObject(loginInfo);
        User user = new User();
        user = user.convertUserJSONObject(jsonObj);
        String userLogin = user.getUserLogin();
        String password = user.getUserPassword();
        User existingUser = userRepository.findByuserLogin(userLogin);
        if(existingUser == null){
            logger.info("User does not exist!");
            return new ResponseEntity<>(null, HttpStatus.valueOf(401));
        }
        else{
            Session session = new Session();
            if(!existingUser.getUserPassword().equals(password)) {
                logger.info("Wrong password!");
                return new ResponseEntity<>(null, HttpStatus.valueOf(401));
            }
            else{
                final String uuid = UUID.randomUUID().toString().replace("-", "");
                session.setToken(HashUtils.getCryptoHash(uuid, "SHA-256"));
                session.setUser_id(existingUser);
                sessionRepository.save(session);
                TOKEN = session.getToken();
                setTOKEN(TOKEN);
                setUSER(existingUser);
                logger.info(TOKEN);
                return new ResponseEntity<>(session, HttpStatus.valueOf(200));
            }
        }
    }

    public static User getUSER() {
        return USER;
    }

    public static void setUSER(User USER) {
        UserController.USER = USER;
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) { UserController.TOKEN = TOKEN; }
}
