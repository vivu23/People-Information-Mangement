package mvc.screens;

import JavaFX.Alerts;
import gateway.DeleteGateway;
import gateway.PersonGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mvc.model.FetchResults;
import mvc.model.Person;
import mvc.model.Session;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger();

    private ArrayList<Person> personArrayList;
    private Session session;
    private PersonGateway personGateway;
    private FetchResults fetchResults;

    @FXML
    private TextField search;

    @FXML
    private ListView<Person> personListView;

    @FXML
    private Button next;

    @FXML
    private Button prev;

    @FXML
    private Button first;

    @FXML
    private Button last;

    public PersonListController(ArrayList<Person> personArrayList, Session session, PersonGateway personGateway, FetchResults fetchResults) {
        this.personArrayList = personArrayList;
        this.session = session;
        this.personGateway = personGateway;
        this.fetchResults = fetchResults;
    }
    @FXML
    void clickPerson(MouseEvent event) {
        // on double click only
        // 1. get the model that is double clicked (if none, then bail)
        // 2. switch to the editing screen with the model that is selected
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                int index = personListView.getSelectionModel().getSelectedIndex();
                LOGGER.info("READING " + personArrayList.get(index).getFirstName() + " " + personArrayList.get(index).getLastName());
                MainController.getInstance().switchView(ScreenType.PERSONDETAIL, personArrayList.get(index));
            }
        }
    }

    @FXML
    void searchClicked(ActionEvent event){
        String pattern = search.getText();
        if(pattern != ""){
            personArrayList = (ArrayList<Person>) personGateway.fetchWithPattern(session.getToken(), pattern);
            ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
            personListView.setItems(tempList);
        }
        else{
            fetchResults = personGateway.fetchPeople(session.getToken(), 0);
            personArrayList = (ArrayList<Person>) fetchResults.getPeople();
            ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
            personListView.setItems(tempList);
        }
    }

    @FXML
    void addPerson(ActionEvent event) {
        LOGGER.info("add person clicked");
        if (personListView.getSelectionModel().selectedItemProperty().getValue() != null) {
            LOGGER.info("Cannot choose a person to insert!");
        }
        // load the person detail with an empty person
        // call the main controller switch view method
        if (personListView.getSelectionModel().getSelectedItem() == null) {
            MainController.getInstance().switchView(ScreenType.PERSONDETAIL, new Person(0, "", "", ""));
        }
    }

    @FXML
    void firstClicked(ActionEvent event){
        FetchResults fetchResults = new FetchResults();
        fetchResults = personGateway.fetchPeople(session.getToken(), 0);
        personArrayList = (ArrayList<Person>) fetchResults.getPeople();
        ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
        personListView.setItems(tempList);
    }

    @FXML
    void lastClicked(ActionEvent event){
        FetchResults fetchResults = new FetchResults();
        fetchResults = personGateway.fetchPeople(session.getToken(), fetchResults.getPageSize()-1);
        personArrayList = (ArrayList<Person>) fetchResults.getPeople();
        ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
        personListView.setItems(tempList);
    }

    @FXML
    void nextClicked(ActionEvent event){
        fetchResults.setCurrentPage(fetchResults.getCurrentPage() +1);
        fetchResults = personGateway.fetchPeople(session.getToken(), fetchResults.getCurrentPage());
        personArrayList = (ArrayList<Person>) fetchResults.getPeople();
        ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
        personListView.setItems(tempList);
    }

    @FXML
    void prevClicked(ActionEvent event){
        fetchResults.setCurrentPage(fetchResults.getCurrentPage() -1);
        FetchResults fetchResults = new FetchResults();
        fetchResults = personGateway.fetchPeople(session.getToken(), fetchResults.getCurrentPage());
        personArrayList = (ArrayList<Person>) fetchResults.getPeople();
        ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);
        personListView.setItems(tempList);
    }
    @FXML
    void deletePerson(ActionEvent event) {
        LOGGER.info("delete person clicked");

        // load the person detail with an empty person
        // call the main controller switch view method
        if (personListView.getSelectionModel().selectedItemProperty().getValue() != null)
        {
            try {
                //receive 200
                Long id = personListView.getSelectionModel().selectedItemProperty().getValue().getId();
                String firstName = personListView.getSelectionModel().selectedItemProperty().getValue().getFirstName();
                int index = personArrayList.indexOf(personListView.getSelectionModel().selectedItemProperty().getValue());
                Session session = this.session;
                LOGGER.info(session);
                DeleteGateway.delete(id.toString(),session);
                LOGGER.info("DELETING " + personArrayList.get(index).getFirstName() + " " +personArrayList.get(index).getLastName());
                personArrayList.remove(index);
                ObservableList<Person> temp = FXCollections.observableArrayList();
                for(Person person : personArrayList){
                    temp.add(person);
                    //temp.add(person.getFirstName() + " " +person.getLastName());
                }
                personListView.setItems(temp);

            } catch(UnauthorizedException e) { //receive 401
                Alerts.infoAlert("delete failed!", "Either your username or your password is incorrect.");
                return;
            } catch(UnknownException e1) {
                Alerts.infoAlert("delete failed!", " 404 Something bad happened: " + e1.getMessage());
                return;
            }
        }
        else {
            Alerts.infoAlert("Invalid input", "Please choose a person to delete");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. turn plain ol arraylist of models into an ObservableArrayList
        if(fetchResults.getCurrentPage() == fetchResults.getPageSize()-1){
            next.setDisable(true);
        }
        if(fetchResults.getCurrentPage()-1 < 0){
            prev.setDisable(true);
        }

        ObservableList<Person> tempList = FXCollections.observableArrayList(personArrayList);

        // 2. plug the observable array list into the list
        //personListView.setItems(tempList);
        personListView.setItems(tempList);
    }
}
