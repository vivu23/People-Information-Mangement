package mvc.screens;

import JavaFX.Alerts;
import gateway.InsertGateway;
import gateway.PersonGateway;
import gateway.UpdateGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mvc.model.AuditTrail;
import mvc.model.Person;
import mvc.model.Session;
import mvc.model.User;
import myexceptions.BadRequestException;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PersonDetailController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger();

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;
//
//    @FXML
//    private TextField dateOfBirth;

    @FXML
    private TextField age;

    @FXML
    private TextField id;

    private String time;

    @FXML
    TableColumn<AuditTrail, String> dateCol;

    @FXML
    TableColumn<AuditTrail, String> changeUser;

    @FXML
    TableColumn<AuditTrail, String> message;

    @FXML
    private Tab auditTrail;

    @FXML
    private TableView table;

    @FXML
    private Tab detail;

    @FXML
    private ListView<Person> personListView;

    @FXML
    private ObservableList<AuditTrail> auditInfo = FXCollections.observableArrayList();

    private Person person;
    private Session session;
    private PersonGateway personGateway;

    public PersonDetailController(Person person, Session session) {
        this.person = person;
        this.session = session;
        this.personGateway = new PersonGateway();
    }

    @FXML
    void cancelSave(ActionEvent event) {
        LOGGER.info("Cancel clicked");
        MainController.getInstance().switchView(ScreenType.PERSONLIST, 0);
    }

    @FXML
    void save(ActionEvent event) {
        LOGGER.info("Save clicked");
        LocalDateTime timeB4 = person.getLast_modified();
        boolean newPerson = false;
        if(person.getId() == 0){
            newPerson = true;
        }

        if(firstName.getText() == "" || lastName.getText() == ""){
            LOGGER.debug("Please check information for firstname and lastname");
            Alerts.infoAlert("Check your input", "Please check the information for firstName and lastName");
        }
        if(firstName.getText().length() > 100 || lastName.getText().length() > 100){
            LOGGER.debug("Input cannot exceed 100 characters");
        }
        else {
            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            // insert new person
            if (newPerson) {
                Session session = this.session;

                //inserting new person
                try {
                    //receive 200
                    InsertGateway.insert(session, firstName.getText(), lastName.getText(), Integer.parseInt(age.getText()));
                    LOGGER.info(session);
                } catch (UnauthorizedException e) { //receive 401
                    Alerts.infoAlert("insert failed!", "Either your username or your password is incorrect.");
                    return;
                } catch (UnknownException e1) {
                    Alerts.infoAlert("insert failed!", " 404 Something bad happened: " + e1.getMessage());
                    return;
                }
                LOGGER.info("CREATING " + person.getFirstName() + " " + person.getLastName() + " with " ); //session.getResponse());
                MainController.getInstance().setSession(session);
                MainController.getInstance().switchView(ScreenType.PERSONLIST); //session.getSessionId());
                MainController.getInstance().switchView(ScreenType.PERSONLIST, person);
            }

            //updating existing person
            else {
                String oldFName = person.getFirstName();
                String oldLName = person.getLastName();
                int newAge = Integer.parseInt(age.getText());
                Session session = this.session;
                try {
                    //receive 200
                    UpdateGateway.update(session, person.getId(), firstName.getText(), lastName.getText(), newAge);
                } catch (UnauthorizedException e) { //receive 401
                    Alerts.infoAlert("updated failed!", "Something bad happened" + e.getMessage());
                    return;
                } catch (UnknownException e1) {
                    Alerts.infoAlert("updated failed!", " 404 Something bad happened: " + e1.getMessage());
                    return;
                } catch (BadRequestException e2) {
                    Alerts.infoAlert("update failed", "400 Something bad happend :" + e2.getMessage());
                }
                //Optimistic Locking
                person = personGateway.fetchPerson(session.getToken(), person.getId());
                LocalDateTime timeAfter = person.getLast_modified();
                if(timeAfter != timeB4){
                    Alerts.infoAlert("Save error!", "Please redo your change and try to save again",
                            "Person has been  modified by someone else");
                }
                else{
                    LOGGER.info("User is not CHANGED");
                }
                LOGGER.info("UPDATING " + person.getFirstName() + " " + person.getLastName());
                MainController.getInstance().setSession(session);
                MainController.getInstance().switchView(ScreenType.PERSONLIST); //session.getSessionId());
            }
        }
    }

    @FXML
    void auditTrailClicked(Event event){
        List<AuditTrail> auditTrails = personGateway.fetchAuditTrail(session.getToken(), person.getId());
        for(AuditTrail audittrail : auditTrails){
            auditInfo.add(new AuditTrail(audittrail.getMessage(),audittrail.getUser_id(), audittrail.getTime()));
        }

        dateCol.setCellValueFactory(new PropertyValueFactory<AuditTrail,String>("time"));
        changeUser.setCellValueFactory(new PropertyValueFactory<AuditTrail, String>("username"));
        message.setCellValueFactory(new PropertyValueFactory<AuditTrail, String>("message"));
        table.setItems(auditInfo);
    }

    @FXML
    void detailClicked(Event Event){
        if(detail.isSelected()) {
            firstName.setText(person.getFirstName());
            lastName.setText(person.getLastName());
            age.setText("" + person.getAge());
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // this is where we connect the model data to the GUI components like textfields
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        age.setText("" + person.getAge());
        id.setText("" + person.getId());
    }


}
