package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import functions.cache.Cache;
import functions.helper.Constants;
import functions.helper.PostBodyParser;
import functions.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

public class Main implements HttpFunction {
    private Cache cache = new Cache();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> postBody = PostBodyParser.getPostBody(request);
        String functionality = postBody.get("functionality");
        if(functionality == null)
            return;

        BufferedWriter out = response.getWriter();
        functionality = functionality.trim();

        switch (functionality) {
            case "registerUser":
                out.write(registerUser(postBody.get("username"), postBody.get("password"), postBody.get("firstName"), postBody.get("lastName")));
                break;
            case "getNewFriends":
                out.write(getNewFriends(postBody.get("username")));
                break;
            case "verifyUser":
                out.write(verifyUser(postBody.get("username"), postBody.get("password")));
                break;
            case "addNewFriends":
                out.write(addNewUsers(postBody.get("username"), postBody.get("newFriends")));
                break;
            case "getCurrentFriends":
                out.write(getCurrentFriends(postBody.get("username")));
                break;
        }
    }

    private String getCurrentFriends(String username) {
        return buildFriendsJson(cache.getCurrentFriends(username));
    }

    private String getNewFriends(String username) {
        return buildFriendsJson(cache.getNewFriends(username));
    }

    private String buildFriendsJson(Collection<UserInfo> friends) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        for(UserInfo user : friends)
            json.append("\"").append(user.getUsername()).append("\":\"").
                    append(user.getFirstName()).append(" ").append(user.getLastName()).append("\",\n");
        json.replace(json.length() - 2, json.length(), "");
        json.append("\n}");

        return json.toString();
    }

    private String addNewUsers(String username, String newFriends) {
        cache.addNewFriends(username, newFriends);
        JSONObject result = new JSONObject();
        result.put("status", Constants.SUCCESS);

        return result.toJSONString();
    }

    private String registerUser(String username, String password, String firstName, String lastName) {
        StringBuilder json = new StringBuilder();
        json.append("{\n").append("\"status\"").append(":");

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)) {
            json.append("\"Fail\"");
        } else {
            cache.registerUser(username, password, firstName, lastName);
            json.append("\"Success\"");
        }

        return json.append("\n}").toString();
    }

    private String verifyUser(String username, String password) {
        StringBuilder json = new StringBuilder();
        json.append("{\n").append("\"status\"").append(":");

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
            json.append("\"").append(Constants.FAIL).append("\"");
        else
            json.append("\"").append(cache.verifyUser(username, password)).append("\"");

        return json.append("\n}").toString();
    }
}
