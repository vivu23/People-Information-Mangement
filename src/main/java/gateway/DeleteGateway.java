package gateway;

import mvc.model.Session;
import myexceptions.UnauthorizedException;
import myexceptions.UnknownException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DeleteGateway {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void delete(String id, Session session) throws UnauthorizedException {
        String token = session.getToken();
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        String url = "http://localhost:8080/people/" + id;

        try {
            httpclient = HttpClients.createDefault();
            HttpDelete deleteRequest = new HttpDelete(url);

            if(token != null && token.length() > 0)
                deleteRequest.setHeader("Authorization", token);

            //execute the delete request
            response = httpclient.execute(deleteRequest);

            switch(response.getStatusLine().getStatusCode()) {

                case 200:
                    HttpEntity entity = response.getEntity();
                    // use org.apache.http.util.EntityUtils to read json as string
                    String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    EntityUtils.consume(entity);
                    return;

                case 401:
                    throw new UnauthorizedException("delete failed");

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
