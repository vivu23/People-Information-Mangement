package gateway;

import mvc.model.Session;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InsertGateway {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void insert(Session session, String firstName, String lastName, int age ) throws UnauthorizedException {
        String token = session.getToken();
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            httpclient = HttpClients.createDefault();
            HttpPost postRequest = new HttpPost("http://localhost:8080/people");

            if(token != null && token.length() > 0)
                postRequest.setHeader("Authorization", token);

            // use this for submitting form data as raw json
            JSONObject formData = new JSONObject();
            formData.put("firstName", firstName);
            formData.put("lastName", lastName);
            formData.put("age", age);
            String formDataString = formData.toString();

            StringEntity reqEntity = new StringEntity(formDataString);
            postRequest.setEntity(reqEntity);

            //execute the post request
            response = httpclient.execute(postRequest);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    // use org.apache.http.util.EntityUtils to read json as string
                    String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    EntityUtils.consume(entity);
                    //session = new Session(token, strResponse);
                    return;
                case 401:
                    throw new UnauthorizedException("login failed");
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
}
