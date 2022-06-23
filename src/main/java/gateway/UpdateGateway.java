package gateway;

import mvc.model.Session;
import myexceptions.BadRequestException;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UpdateGateway {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void update(Session session, long id, String firstName, String lastName, int age) throws UnauthorizedException {
        String token = session.getToken();
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        String url = "http://localhost:8080/people/" + id;
        try {
            httpclient = HttpClients.createDefault();
            HttpPut putRequest = new HttpPut(url);

            if(token != null && token.length() > 0)
                putRequest.setHeader("Authorization", token);

            // use this for submitting form data as raw json
            JSONObject formData = new JSONObject();
            if(firstName !="") {
                formData.put("firstName", firstName);
            }
            if (lastName !=""){
                formData.put("lastName", lastName);

            }
            if(age != 0){
                formData.put("age", age);
            }
            String formDataString = formData.toString();

            StringEntity reqEntity = new StringEntity(formDataString);
            putRequest.setEntity(reqEntity);
            //execute the post request
            response = httpclient.execute(putRequest);
            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    // use org.apache.http.util.EntityUtils to read json as string
                    String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    EntityUtils.consume(entity);
                    return;
                case 400:
                    throw new BadRequestException("update failed!");
                case 401:
                    throw new UnauthorizedException("update failed!");
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
