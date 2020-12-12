package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import functions.cache.Cache;
import functions.cache.UIContent;
import functions.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main implements HttpFunction {
    private String frequencyGeneratorURL = "https://us-central1-vivekshresta-bandaru.cloudfunctions.net/frequency-function";
    private String histogramGeneratorURL = "https://us-central1-vivekshresta-bandaru.cloudfunctions.net/histogram_function";
    private Cache cache = new Cache();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        response.appendHeader("Content-Type", "text/html; charset=UTF-8");
        BufferedWriter out = response.getWriter();

        String path = request.getPath();
        System.out.println(path);
        if(path.equals("/")) {
            out.write(getHomePage());
        } else if(path.contains("login")) {
            out.write(getLoginPage());
        } else if(path.contains("signup")) {
            out.write(getSignupPage());
        } else if(path.contains("register")) {
            try {
                registerUser(request);
                out.write(getHomePage());
            } catch (Exception exception) {
                exception.printStackTrace();
                out.write(getSignupPage());
            }
        } else if(path.contains("dashboard")) {
            //String username = SessionStorage.verifyUser(request);
            String username = "vivek";
            String result = StringUtils.isEmpty(username) ? getLoginPage() : getAddFriendView(username);
            out.write(result);
        } else if(path.contains("addfriends")) {
            SessionStorage.addNewFriends(request);
            out.write(getPostsView());
        }
    }

    private String getPostsView() {
        return null;
    }

    private void registerUser(HttpRequest request) throws Exception {
        SessionStorage.registerUser(request);
    }

    private String getHomePage() {
        return "<form action = /frontend-function/login method=\"POST\">\n" +
                "\t\t<input type=\"submit\" value=\"Login\" name=\"Login\" id=\"login\"/>\n" +
                "</form>\n" +
                "\n" +
                "<form action = /frontend-function/signup method=\"POST\">\n" +
                "\t\t<input type=\"submit\" value=\"Signup\" name=\"Signup\" id=\"signup\"/>\n" +
                "</form>";
    }

    private String getSignupPage() {
        return "<form action = /frontend-function/register method=\"POST\">\n" +
                "  <label for=\"firstName\">First name:</label><br>\n" +
                "  <input type=\"text\" id=\"firstName\" name=\"firstName\"><br>\n" +
                "  <label for=\"lastName\">Last name:</label><br>\n" +
                "  <input type=\"text\" id=\"lastName\" name=\"lastName\"><br>\n" +
                "\n" +
                "  <label for=\"username\">User name:</label><br>\n" +
                "  <input type=\"text\" id=\"username\" name=\"username\"><br>\n" +
                "  <label for=\"password\">Password:</label><br>\n" +
                "  <input type=\"text\" id=\"password\" name=\"password\"><br><br>\n" +
                "\n" +
                "  <input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    private String getAddFriendView(String currentUser) {
        String searchUI = "<form action = /frontend-function/search method=\"POST\">\n" +
                "  <label for=\"search\">Search for a friend:</label><br>\n" +
                "  <input type=\"text\" id=\"query\" name=\"query\"><br><br>\n" +
                "</form>";

        //List<UserInfo> newFriends = SessionStorage.getNewFriends(currentUser);
        List<UserInfo> newFriends = new ArrayList<>();
        newFriends.add(new UserInfo("kartik", "", "Kartik", "Addala"));
        newFriends.add(new UserInfo("prashant", "", "prashanth", "sateesh"));
        newFriends.add(new UserInfo("anshul", "", "Anshul", "Vohra"));
        newFriends.add(new UserInfo("vansh", "", "Vansh", "Shah"));
        StringBuilder sb = new StringBuilder(searchUI);
        sb.append("<form action = /frontend-function/addfriends method=\"POST\">\n");
        sb.append("Add friends: <br>\n");
        for(UserInfo userInfo : newFriends) {
            sb.append("<input type=\"checkbox\" id=").append(userInfo.getUsername()).
                    append(" name=").append(userInfo.getUsername()).append(" value=").append(userInfo.getUsername()).append("\n");
            sb.append("<label for=").append(userInfo.getUsername()).append("> ").
                    append(userInfo.getFirstName()).append(" ").append(userInfo.getLastName()).append("</label><br><br>\n");
        }

        sb.append("<input type=\"submit\" value=\"Continue or Skip\">\n");
        sb.append("</form>");

        return sb.toString();
    }

    private String getLoginPage() {
        return "<form action = /frontend-function/dashboard method=\"POST\">\n" +
                "  <label for=\"username\">User name:</label><br>\n" +
                "  <input type=\"text\" id=\"username\" name=\"username\"><br>\n" +
                "  <label for=\"password\">Password:</label><br>\n" +
                "  <input type=\"text\" id=\"password\" name=\"password\"><br><br>\n" +
                "  <input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    private String getFinalPage(String url) {
        StringBuilder result = new StringBuilder(getHomePage()).append("\n");

        try {
            long initialTime = System.currentTimeMillis();
            UIContent uiContent = getUIContent(url);
            result.append("<body>\n").append("<img src=\"").append(uiContent.getImageURL()).append("\" alt=\"Histogram image\">\n")
                    .append("<br>")
                    .append("\t<div>The time taken to generate the frequency distribution and histogram: ").append(System.currentTimeMillis() - initialTime).append(" ms</div>\n")
                    .append("<br>")
                    .append("\t<div><b>Frequency distribution for sentence lengths:</b> (Format - Sentence length: Number of occurrences)\" </div>\n")
                    .append("\t<div>").append(uiContent.getAggregatedFrequencies()).append("</div>\n")
                    .append("</body>");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private UIContent getUIContent(String url) throws IOException {
        UIContent uiContent = cache.getUIContent(url);
        if(uiContent == null) {
            JSONObject json = new JSONObject();
            json.put("url", url);
            Map<String, String> frequencies = Client.getData(frequencyGeneratorURL, "", json);

            json.put("frequencies", frequencies.get("frequencies"));
            String imageURL = Client.getData(histogramGeneratorURL, "", json).get("histogramURL").trim();
            String aggregatedFrequencies = frequencies.get("aggregatedFrequencies");

            cache.addUIContent(url, imageURL, aggregatedFrequencies);
            uiContent = new UIContent(imageURL, aggregatedFrequencies);
        }

        return uiContent;
    }
}
