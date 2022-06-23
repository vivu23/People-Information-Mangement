package gateway;

import mvc.model.AuditTrail;
import mvc.model.FetchResults;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import mvc.model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PersonGateway {
    private static final String URL = "http://localhost:8080";
    private static final Logger logger = LogManager.getLogger();

    private String wsURL;
    private String sessionId;

    public PersonGateway(){
    }

    public PersonGateway(String url, String sessionId) {
        this.sessionId = sessionId;
        this.wsURL = url;
    }

    public FetchResults fetchPeople(String token, int pageNum) throws UnauthorizedException, UnknownException {
        List<Person> people = new ArrayList<>();
        FetchResults fetchResults = new FetchResults();
        try {
            String response = executeGetRequest(URL + "/people?pageNum=" + pageNum, token);
            JSONObject peopleList = new JSONObject(response);
            fetchResults = FetchResults.fromJSONObject(peopleList);
        } catch(RuntimeException e) {
            throw new UnknownException(e);
        }
        return fetchResults;
    }
    public Person fetchPerson(String token, long id) throws UnauthorizedException, UnknownException {
        Person person = new Person();
        try {
            String response = executeGetRequest(URL + "/people/" + id, token);
            JSONObject json = new JSONObject(response);
            person  = Person.fromJSONObjectWithTime(json);
            logger.info(person);
        } catch(RuntimeException e) {
            throw new UnknownException(e);
        }
        return person;
    }

    private String executeGetRequest(String url, String token) throws UnauthorizedException, UnknownException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpGet getRequest = new HttpGet(url);

            //check token and set authorization header
            if(token != null && token.length() > 0)
                getRequest.setHeader("Authorization", token);

            response = httpclient.execute(getRequest);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    return getStringFromResponse(response);
                case 401:
                    throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());
                default:
                    throw new UnknownException(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnauthorizedException(e);

        } finally {
            try {
                if(response != null) {
                    response.close();
                }
                if(httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new UnauthorizedException(e);
            }
        }
    }

    private String getStringFromResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        EntityUtils.consume(entity);
        return strResponse;
    }

    //TODO: Add a function to fetch Audit Trail

    public List<Person> fetchWithPattern(String token, String lastName) throws UnauthorizedException, UnknownException {
        List<Person> results = new ArrayList<>();
        try {
            String response = executeGetRequest(URL + "/people/search/" + lastName, token);
            JSONArray peopleList = new JSONArray(response);
            for (Object person : peopleList) {
                results.add(Person.fromJSONObject((JSONObject) person));
            }
        } catch (RuntimeException e) {
            throw new UnknownException(e);
        }
        return results;
    }

    public List<AuditTrail> fetchAuditTrail(String token, long id) throws UnauthorizedException, UnknownException {
        List<AuditTrail> auditTrails = new ArrayList<>();

        try {
            String response = executeGetRequest(URL + "/people/"+ id + "/audittrail", token);
            JSONArray auditTrailList = new JSONArray(response);
            for (Object auditTrail : auditTrailList) {
                auditTrails.add(AuditTrail.fromJSONObjectToAuditTrail((JSONObject) auditTrail));
            }
        } catch(RuntimeException e) {
            throw new UnknownException(e);
        }
        return auditTrails;
    }

}
