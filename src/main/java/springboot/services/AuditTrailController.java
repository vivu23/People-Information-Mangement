package springboot.services;

import mvc.model.AuditTrail;
import mvc.model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import springboot.repositories.AuditTrailRepository;
import springboot.repositories.PersonRepository;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuditTrailController {
    private static final Logger logger = LogManager.getLogger();

    private AuditTrailRepository auditTrailRepository;
    private PersonRepository personRepository;

    public AuditTrailController(AuditTrailRepository auditTrailRepository, PersonRepository personRepository) {
        this.auditTrailRepository = auditTrailRepository;
        this.personRepository = personRepository;
    }

    @GetMapping("/people/{personId}/audittrail")
    public ResponseEntity<List<AuditTrail>> fetchAuditTrail(@PathVariable long personId, @RequestHeader Map<String, String> headers) {
        // get the list of person models from the orm's repository

        Optional<Person> person = personRepository.findById(personId);
        List<AuditTrail> auditTrails = auditTrailRepository.findAllByPersonId(person.get());
            // check headers
            String token = UserController.getTOKEN();
            if (headers.containsKey("authorization")) {
                if (!headers.get("authorization").equals(token)) {
                    return new ResponseEntity<>(HttpStatus.valueOf(401));
                }
                // return people list
                else {
                    return new ResponseEntity<>(auditTrails, HttpStatus.valueOf(200));
                }
            }
            return new ResponseEntity<>(HttpStatus.valueOf(401));
    }
}
