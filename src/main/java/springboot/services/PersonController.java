package springboot.services;

import mvc.model.*;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import springboot.repositories.AuditTrailRepository;
import springboot.repositories.PersonRepository;
import springboot.repositories.SessionRepository;
import springboot.repositories.UserRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PersonController{
    private static final Logger logger = LogManager.getLogger();

    private SessionRepository sessionRepository;
    private AuditTrailRepository auditTrailRepository;
    private PersonRepository personRepository;

    public PersonController(PersonRepository personRepository, AuditTrailRepository auditTrailRepository, SessionRepository sessionRepository) {
        this.auditTrailRepository = auditTrailRepository;
        this.personRepository = personRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/people/search/{pattern}")
    public ResponseEntity<List<Person>> fetchPersonWithPattern(@RequestHeader Map<String, String> headers, @PathVariable String pattern){
        List<Person> person = personRepository.findByLastNameStartsWith(pattern);
        // check headers
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)) {
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
        }
        else {
            return new ResponseEntity<>(HttpStatus.valueOf(401));
        }
        return new ResponseEntity<>(person, HttpStatus.valueOf(200));
    }

    @GetMapping("/people")
    public  ResponseEntity<FetchResults> fetchPeople(@RequestHeader Map<String, String> headers,
                                                     @RequestParam(defaultValue = "0") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize , @RequestParam(value ="lastName",required = false) String lastName) {

        FetchResults fetchResults = new FetchResults();
        Pageable paging = PageRequest.of(pageNum, pageSize);
        Page<Person> people = personRepository.findAll(paging);
        logger.info(people);
        // check headers
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)){
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
            // return people list
            else{
                if(people.hasContent()) {
                    fetchResults.setPeople(people.getContent());
                    fetchResults.setPageSize(people.getTotalPages());
                    fetchResults.setNumRows(people.getTotalElements());
                    fetchResults.setCurrentPage(people.getPageable().getPageNumber());
                    return new ResponseEntity<>(fetchResults, HttpStatus.valueOf(200));
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.valueOf(401));
    }

    @GetMapping("/people/{personId}")
    public ResponseEntity<Person> fetchPersonById(@PathVariable long personId, @RequestHeader Map<String, String> headers) {
        // find person using path variable
        Optional<Person> person = personRepository.findById(personId);

        // check headers
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)) {
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
        }
        else {
            return new ResponseEntity<>(HttpStatus.valueOf(401));
        }

        if(person.isPresent())
            return new ResponseEntity<>(person.get(), HttpStatus.valueOf(200));
        else
            return new ResponseEntity<>(HttpStatus.valueOf(404));
    }

    @DeleteMapping("/people/{personId}")
    public ResponseEntity<String> deletePersonById(@PathVariable long personId, @RequestHeader Map<String, String> headers){
        // find person using path variable
        Optional<Person> person = personRepository.findById(personId);

        // check headers
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)) {
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
        }
        else{
            return new ResponseEntity<>(HttpStatus.valueOf(401));
        }

        if(person.isPresent())
        {
            personRepository.deleteById(personId);
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        }
        else
            return new ResponseEntity<>(HttpStatus.valueOf(404));
    }

    @PostMapping("/people")
    @Transactional(rollbackFor = { SQLException.class })
    public ResponseEntity<String> insertPerson(@RequestHeader Map<String, String> headers, @RequestBody String body) throws SQLException {
        // check headers
        User user = UserController.getUSER();
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)) {
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
        }
        else{
            return new ResponseEntity<>(HttpStatus.valueOf(401));
        }

        // convert body to json object
        JSONObject jsonObj = new JSONObject(body);
        JSONObject errorList = new JSONObject();
        boolean validFN = true;
        boolean validLN = true;
        boolean validAge = true;

        // validate fields
        if(jsonObj.has("firstName") && jsonObj.has("lastName") && jsonObj.has("age")) {
            //validate dateOfBirth
            if(jsonObj.getInt("age") < 0){
                errorList.put("age cannot be a negative number", jsonObj.getInt("age"));
                validAge = false;
            }

            //validate firstName
            String firstName = jsonObj.getString("firstName");
            if ((firstName.length() > 100) || (firstName.length() < 1)) {
                validFN = false;
                errorList.put("invalid firstName", firstName);
            }
            //validate lastName
            String lastName = jsonObj.getString("lastName");
            if ((lastName.length() > 100) || (lastName.length() < 1)) {
                validLN = false;
                errorList.put("invalid lastName", lastName);
            }

            // success
            if (validAge && validFN && validLN) {
                Person person = new Person();
                person = Person.fromJSONObjectSpring(jsonObj);
                personRepository.save(person);
                AuditTrail auditTrail = new AuditTrail("added", user, person);
                logger.info(auditTrail);
                auditTrailRepository.save(auditTrail);
                return new ResponseEntity<>(HttpStatus.valueOf(200));
            }
        }
        else {
            errorList.put("Missing or incorrect insert information", "");
        }
        // incorrect fields
        return new ResponseEntity<>(errorList.toString(), HttpStatus.valueOf(400));
    }

    @PutMapping("/people/{personId}")
    @Transactional(rollbackFor = {SQLException.class})
    public ResponseEntity<String> updatePerson(@PathVariable long personId, @RequestHeader Map<String, String> headers, @RequestBody String body) throws SQLException{

        // check headers
        User user = UserController.getUSER();
        String token = UserController.getTOKEN();
        if(headers.containsKey("authorization")) {
            if(!headers.get("authorization").equals(token)) {
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }
        }
        else{
            return new ResponseEntity<>(HttpStatus.valueOf(401));
        }
        // check repository list
        Optional<Person> person = personRepository.findById(personId);

        if(person.isEmpty())
            return new ResponseEntity<>(HttpStatus.valueOf(404));

        // convert body to json object
        JSONObject jsonObj = new JSONObject(body);
        JSONObject errorList = new JSONObject();
        String firstName="", lastName="";
        String oldFName ="", oldLName ="";
        int oldAge = 0, age = 0;
        boolean validFN = true;
        boolean validLN = true;
        boolean validAge = true;

        // validate fields
        if(jsonObj.has("lastName") && jsonObj.has("firstName")) {
                if(jsonObj.has("age")){
                    age = jsonObj.getInt("age");
                    if (age < 0) {
                        errorList.put("Age cannot be a negative value", age);
                        validAge = false;
                    }
                }
                else {
                    age = 0;
                }

            firstName = jsonObj.getString("firstName");
            if ((firstName.length() > 100) || (firstName.length() < 1)) {
                validFN = false;
                errorList.put("invalid firstName", firstName);
            }
            lastName = jsonObj.getString("lastName");
            if ((lastName.length() > 100) || (lastName.length() < 1)) {
                validLN = false;
                errorList.put("invalid lastName", lastName);
            }

            // success
            if (validAge && validFN && validLN) {
                Person updatedPerson = personRepository.findById(personId).orElse(new Person());
                oldAge = updatedPerson.getAge();
                oldFName= updatedPerson.getFirstName();
                oldLName= updatedPerson.getLastName();
                if (jsonObj.has("firstName")) {
                    updatedPerson.setFirstName(firstName);
                }

                if (jsonObj.has("lastName")) {
                    updatedPerson.setLastName(lastName);
                }

                if (jsonObj.has("age")) {
                    if(age != 0) {
                        updatedPerson.setAgeInt(age);
                    }
                }
                LocalDateTime time = LocalDateTime.now();
                updatedPerson.setLast_modified(time);
                personRepository.save(updatedPerson);
                if(!updatedPerson.getFirstName().equals(oldFName)) {
                    logger.info(!updatedPerson.getFirstName().equals(oldFName));
                    String message1 = "First name changed from " + oldFName + " to " + updatedPerson.getFirstName();
                    AuditTrail auditTrail1 = new AuditTrail(message1, user, updatedPerson);
                    logger.info(auditTrail1);
                    auditTrailRepository.save(auditTrail1);
                }

                if(!updatedPerson.getLastName().equals(oldLName)){
                    String message2 = "Last name changed f-rom " + oldLName + " to " + updatedPerson.getLastName();
                    AuditTrail auditTrail2 = new AuditTrail(message2, user, updatedPerson);
                    logger.info(auditTrail2);
                    auditTrailRepository.save(auditTrail2);
                }

                if(updatedPerson.getAge() != oldAge) {
                    String message3 = "Age changed from " + oldAge + " to " + updatedPerson.getAge();
                    AuditTrail auditTrail3 = new AuditTrail(message3, user, updatedPerson);
                    logger.info(auditTrail3);
                    auditTrailRepository.save(auditTrail3);
                }
                return new ResponseEntity<>(HttpStatus.valueOf(200));
            }
        }
        else {
            errorList.put("Bad field name", "");
        }
        // incorrect fields
        return new ResponseEntity<>(errorList.toString(), HttpStatus.valueOf(400));
    }
}
