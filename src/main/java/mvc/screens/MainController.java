package mvc.screens;

import gateway.PersonGateway;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import mvc.AppMain;
import mvc.model.FetchResults;
import mvc.model.Person;
import mvc.model.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Controller
public class MainController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static MainController instance = null;
    private ArrayList<Person> personList;

    private Session session;

    private PersonGateway personGateway;

    private FetchResults fetchResults;

    @FXML
    private BorderPane rootPane;

    private MainController() {
        personGateway = new PersonGateway();
    }

    @FXML
    public void switchView(ScreenType screenType, Object... args) {
        switch (screenType) {
            case PERSONLIST:
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/person_list.fxml"));
                fetchResults = personGateway.fetchPeople(session.getToken(), 0);
                personList = (ArrayList<Person>) fetchResults.getPeople();
                loader.setController(new PersonListController(personList, session, personGateway, fetchResults));
                Parent rootNode = null;
                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case LOGIN:
                loader = new FXMLLoader(this.getClass().getResource("/login.fxml"));
                loader.setController(new LoginController());
                rootNode = null;
                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case PERSONDETAIL:
                loader = new FXMLLoader(this.getClass().getResource("/audit_trail.fxml"));
                // TODO: need to give the controller a person object that is passed into switchView
                if(!(args[0] instanceof Person)) {
                    throw new IllegalArgumentException("Hey that's not a person! " + args[0].toString());
                }
                loader.setController(new PersonDetailController((Person) args[0], session));
                rootNode = null;
                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        switchView(ScreenType.LOGIN);
    }

    public static MainController getInstance() {
        if(instance == null)
            instance = new MainController();
        return instance;
    }

    // accessors

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


}
