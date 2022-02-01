package hu.codelens.sharecenter.internal;

import hu.codelens.sharecenter.JShareCenter;
import hu.codelens.sharecenter.JShareCenterMediaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.*;

public class DefaultJShareCenter implements JShareCenter {

    private final HttpsClient client;
    private final DocumentBuilderFactory documentBuilderFactory;
    private boolean loggedIn;

    public DefaultJShareCenter(String host) {
        this.client = new HttpsClient(host);
        this.loggedIn = false;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
    }

    @Override
    public boolean login(String username, String base64password) {
        HttpsClient.Response response = client.send(client.createRequest("/cgi-bin/login_mgr.cgi",
            Map.of("cmd", "login", "username", username, "pwd", base64password, "ssl", 1,
                "ssl_port", 443, "port", 443, "f_type", 1, "f_username", "")));

        if (response.getCode() == 302 || response.getCode() == 200) {
            String location = response.getHeader("location");
            if (location.contains("/web/home.html?v=")) {
                loggedIn = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public void logout(String username) {
        checkLoggedIn("logout");
        HttpsClient.Response response = client.send(client.createRequest("/cgi-bin/login_mgr.cgi?cmd=logout&username=" + username));
        if (response.getCode() == 302 || response.getCode() == 200) {
            loggedIn = false;
        }
    }

    @Override
    public Optional<Boolean> checkMediaScanRunning() {
        checkLoggedIn("CheckRunningAvPrescan");

        HttpRequest request = client.createRequest("/cgi-bin/app_mgr.cgi", Map.of("cmd", "CGI_SQLDB_Stop_Finish"));
        return getResponse(request).map(text -> text.equals("1"));
    }

    @Override
    public Optional<Boolean> startMediaScan(JShareCenterMediaPath mediaPath) {
        checkLoggedIn("CheckRunningAvPrescan");

        HttpRequest request = client.createRequest("/cgi-bin/app_mgr.cgi",
            Map.of("cmd", "UPnP_AV_Server_Prescan", "f_dir", mediaPath.getInternalPath()));
        return getResponse(request).map(text -> text.equals("1"));
    }

    @Override
    public Optional<Boolean> checkMediaScanFinished() {
        checkLoggedIn("CheckRunningAvPrescanFinished");

        HttpRequest request = client.createRequest("/cgi-bin/app_mgr.cgi", Map.of("cmd", "UPnP_AV_Server_Prescan_Finished"));
        return getResponse(request).map(text -> text.equals("1"));
    }

    @Override
    public Optional<Integer> checkMediaScanProgress() {
        checkLoggedIn("CheckRunningAvPrescanProgress");

        HttpRequest request = client.createRequest("/cgi-bin/app_mgr.cgi", Map.of("cmd", "UPnP_AV_Server_Get_SQLDB_State"));
        HttpsClient.Response response = client.send(request);

        if (response.getCode() == HttpURLConnection.HTTP_OK) {
            Optional<Document> parsedDocument = parseXml(response.getBody());
            if (parsedDocument.isPresent()) {
                Document document = parsedDocument.get();
                Element documentElement = document.getDocumentElement();
                if (documentElement.getTagName().equals("config")) {
                    NodeList childNodes = documentElement.getChildNodes();
                    if (childNodes.getLength() == 2) {
                        Node resNode = childNodes.item(0);
                        if (resNode.getNodeName().equals("db_stste")) {
                            try {
                                return Optional.of(Integer.parseInt(resNode.getTextContent()));
                            } catch (NumberFormatException e) {
                                // ignored
                                return Optional.empty();
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Collection<JShareCenterMediaPath>> getMediaServerPathList() {
        checkLoggedIn("AvServerPathList");

        HttpRequest request = client.createRequest("/cgi-bin/app_mgr.cgi",
            Map.of("cmd", "UPnP_AV_Server_Path_List", "page", 1, "rp", 10,
                "query", "", "qtype", "", "f_field", "false", "user", ""));
        HttpsClient.Response response = client.send(request);

        if (response.getCode() == 200) {
            return parseAvServerPathListXml(response.getBody());
        }

        return Optional.empty();
    }

    private Optional<Document> parseXml(String xml) {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not create XML document builder", e);
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes())) {
            return Optional.ofNullable(documentBuilder.parse(is));
        } catch (IOException | SAXException e) {
            // ignored
            return Optional.empty();
        }
    }

    private Optional<Collection<JShareCenterMediaPath>> parseAvServerPathListXml(String xmlBody) {
        Optional<Document> parsedDocument = parseXml(xmlBody);
        if (parsedDocument.isPresent()) {
            Document document = parsedDocument.get();
            List<JShareCenterMediaPath> paths = new ArrayList<>();

            NodeList rows = document.getElementsByTagName("row");
            for (int rowIdx = 0; rowIdx < rows.getLength(); rowIdx++) {
                Node row = rows.item(rowIdx);
                NodeList cells = row.getChildNodes();
                // todo: check if there are proper count of cell tags
                paths.add(new DefaultJShareCenterMediaPath(cells.item(1).getTextContent(), cells.item(4).getTextContent()));
            }

            return Optional.of(paths);
        }

        return Optional.empty();
    }

    private Optional<String> getResponse(HttpRequest request) {
        HttpsClient.Response response = client.send(request);

        if (response.getCode() == 200) {
            Optional<Document> parsedDocument = parseXml(response.getBody());
            if (parsedDocument.isPresent()) {
                Document document = parsedDocument.get();
                Element documentElement = document.getDocumentElement();
                if (documentElement.getTagName().equals("config")) {
                    NodeList childNodes = documentElement.getChildNodes();
                    if (childNodes.getLength() == 1) {
                        Node resNode = childNodes.item(0);
                        if (resNode.getNodeName().equals("res")) {
                            return Optional.ofNullable(resNode.getTextContent());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void checkLoggedIn(String operation) {
        if (!loggedIn) {
            throw new IllegalStateException("Operation (" + operation + ") cannot be done, JShareCenter is not logged in");
        }
    }

}
