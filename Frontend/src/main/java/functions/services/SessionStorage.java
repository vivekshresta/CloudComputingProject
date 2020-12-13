package functions.services;

import com.google.cloud.functions.HttpRequest;
import functions.Client;
import functions.helper.Constants;
import functions.pojo.UserInfo;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

public class SessionStorage {
    private static String sessionStorageURL = "https://us-central1-vivekshresta-bandaru.cloudfunctions.net/session-function";

    public static void registerUser(HttpRequest request) throws Exception {
        Optional<String> firstName = request.getFirstQueryParameter("firstName");
        Optional<String> lastName = request.getFirstQueryParameter("lastName");
        Optional<String> username = request.getFirstQueryParameter("username");
        Optional<String> password = request.getFirstQueryParameter("password");

        if(firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty())
            throw new Exception("Register again");

        JSONObject json = new JSONObject();
        json.put("firstName", firstName.get());
        json.put("lastName", lastName.get());
        json.put("username", username.get());
        json.put("password", password.get());

        System.out.println("Registering a user: " + json);

        Client.getData(sessionStorageURL, "registerUser", json);
    }

    public static List<UserInfo> getNewFriends(String currentUser) {
        List<UserInfo> friends = new ArrayList<>();

        try {
            JSONObject json = new JSONObject();
            json.put("username", currentUser);

            Map<String, String> data = Client.getData(sessionStorageURL, "getNewFriends", json);
            for(String username : data.keySet()) {
                String[] name = data.get(username).split(" ");
                friends.add(new UserInfo(username, "", name[0], name[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public static String verifyUser(HttpRequest request) {
        Optional<String> username = request.getFirstQueryParameter("username");
        Optional<String> password = request.getFirstQueryParameter("password");
        if(username.isEmpty() || password.isEmpty())
            return "";

        try {
            JSONObject json = new JSONObject();
            json.put("username", username.get());
            json.put("password", password.get());

            Map<String, String> data = Client.getData(sessionStorageURL, "verifyUser", json);
            String status = data.get(Constants.STATUS);
            return status.equals(Constants.SUCCESS) ? username.get() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void addNewFriends(HttpRequest request) {
        Optional<String> username = getUsernameFromCookie(request);
        if(username.isEmpty())
            return;

        JSONObject json = new JSONObject();
        Set<String> friends = request.getQueryParameters().keySet();
        if(friends.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        for (String friend : friends)
            sb.append(friend).append(",");
        sb.replace(sb.length() - 1, sb.length(), "");
        json.put("username", username.get());
        json.put("newFriends", sb.toString());

        try {
            Client.getData(sessionStorageURL, "addNewFriends", json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<UserInfo> getCurrentFriends(HttpRequest request) {
        List<UserInfo> currentFriends = new ArrayList<>();
        Optional<String> username = getUsernameFromCookie(request);
        if(username.isEmpty())
            return new ArrayList<>();

        JSONObject json = new JSONObject();
        json.put("username", username.get());

        try {
            Map<String, String> data = Client.getData(sessionStorageURL, "getCurrentFriends", json);
            for(String key : data.keySet()) {
                String[] names = data.get(key).split(" ");
                currentFriends.add(new UserInfo(key, "", names[0], names[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return currentFriends;
    }

    public static Optional<String> getUsernameFromCookie(HttpRequest request) {
        Optional<String> cookie = request.getFirstHeader("Cookie");
        if(cookie.isEmpty())
            return cookie;

        return Optional.of(cookie.get().replaceFirst(Constants.SESSION_COOKIE + "=", ""));
    }

}
