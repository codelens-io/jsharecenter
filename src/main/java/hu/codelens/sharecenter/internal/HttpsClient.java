package hu.codelens.sharecenter.internal;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.Serializable;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

public class HttpsClient {

    public enum Method {
        GET, POST
    }

    public interface Response {
        int getCode();

        String getBody();

        String getHeader(String header);
    }

    private static class ResponseImpl implements Response {
        private final HttpResponse<String> response;

        private ResponseImpl(HttpResponse<String> response) {
            this.response = response;
        }

        @Override
        public int getCode() {
            return response.statusCode();
        }

        @Override
        public String getBody() {
            return response.body();
        }

        @Override
        public String getHeader(String header) {
            return response.headers().firstValue(header).orElse(null);
        }
    }

    private final String host;
    private final HttpClient httpClient;

    public HttpsClient(String host) {
        this.host = host;

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new IgnoreAllX509TrustManager()}, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Could not initialize ShareCenter client", e);
        }

        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("");

        CookieManager cookieManager = new CookieManager();

        httpClient = HttpClient
            .newBuilder()
            .sslContext(sslContext)
            .sslParameters(sslParameters)
            .cookieHandler(cookieManager)
            .build();
    }

    public HttpRequest createRequest(String path) {
        return createRequest(Method.GET, path);
    }

    public HttpRequest createRequest(Method method, String path) {
        return createRequest(method, path, null);
    }

    public HttpRequest createRequest(String path, Map<String, Serializable> data) {
        return createRequest(Method.POST, path, data);
    }

    public HttpRequest createRequest(Method method, String path, Map<String, Serializable> data) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("https://" + host + path));
        if (method == Method.GET) {
            builder = builder.GET();
        } else {
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(dataToString(data)));
        }
        builder.header("Content-Type", "application/x-www-form-urlencoded");
        return builder.build();
    }

    public Response send(HttpRequest request) {
        try {
            return new ResponseImpl(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Could not send request to ShareCenter", e);
        }
    }

    private static String dataToString(Map<String, Serializable> data) {
        StringBuilder stringBuilder = new StringBuilder();
        data.forEach((name, value) -> {
            if (stringBuilder.length() != 0) {
                stringBuilder.append("&");
            }
            stringBuilder.append(name).append("=");
            if (value != null) {
                stringBuilder.append(value);
            }
        });
        return stringBuilder.toString();
    }
}
