package mvc.screens;

import JavaFX.Alerts;
import gateway.LoginGateway;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mvc.AppMain;
import mvc.model.Session;
import mvc.model.User;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import pw_hash.HashUtils;
import springboot.services.UserController;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger();

    @FXML
    private TextField userName;
    @FXML
    private PasswordField userPass;

    @FXML
    void login(ActionEvent event) {
        String login = userName.getText();
        String hashedPassword = HashUtils.getCryptoHash(userPass.getText(), "SHA-256");
        User user = new User(login, hashedPassword);
        LOGGER.info(login + " " + hashedPassword);

        // check login with LoginGateway
        // login will throw an exception if it fails
        Session session = null;
        try {
            //receive 200
            session = LoginGateway.login(login, hashedPassword);
            //session = userController.userLogin(user).getBody();
        } catch(UnauthorizedException e) { //receive 401
            Alerts.infoAlert("Login failed!", "Either your username or your password is incorrect.");
            return;
        } catch(UnknownException e1) {
            Alerts.infoAlert("Login failed!", "Something bad happened: " + e1.getMessage());
            return;
        }
        
        // if ok. transition to people list
        MainController.getInstance().setSession(session);
        MainController.getInstance().switchView(ScreenType.PERSONLIST, 0);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userName.setText("ragnar");
        userPass.setText("flapjacks");
    }
}
