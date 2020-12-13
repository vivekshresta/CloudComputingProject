package functions.services;

import com.google.cloud.functions.HttpRequest;
import functions.Client;
import functions.helper.Constants;
import functions.pojo.PostInfo;
import functions.pojo.UserInfo;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PostStorage {

    private static String postStorageURL = "https://us-central1-vivekshresta-bandaru.cloudfunctions.net/timeline-function";

    public static void submitPost(HttpRequest request) {
        Optional<String> username = getUsernameFromCookie(request);
        Optional<String> post = request.getFirstQueryParameter("post");
        if(username.isEmpty() || post.isEmpty())
            return;

        JSONObject json = new JSONObject();
        json.put("username", username.get());
        json.put("post", post.get());

        try {
            Client.getData(postStorageURL, "submitPost", json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<PostInfo> generateTimeline(Optional<String> username, List<UserInfo> friends) throws Exception {
        String friendsStr = listToString(friends);
        if(username.isEmpty())
            throw new Exception("No Cookie");

        JSONObject json = new JSONObject();
        json.put("username", username.get());
        json.put("friends", friendsStr);

        try {
            Map<String, String> map = Client.getData(postStorageURL, "generateTimeline", json);
            if(map.isEmpty())
                return new ArrayList<>();

            List<PostInfo> timeline = new ArrayList<>();
            for(String key : map.keySet()) {
                String[] posts = map.get(key).split(Constants.POST_SEPARATION);
                for(String post : posts)
                    timeline.add(new PostInfo(key, post));
            }

            return timeline;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static String listToString(List<UserInfo> friends) {
        if(friends.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        for(UserInfo friend : friends)
            sb.append(friend.getUsername()).append(", ");
        return sb.replace(sb.length() - 2, sb.length(), "").toString();
    }

    private static Optional<String> getUsernameFromCookie(HttpRequest request) {
        Optional<String> cookie = request.getFirstHeader("Cookie");
        if(cookie.isEmpty())
            return cookie;

        return Optional.of(cookie.get().replaceFirst(Constants.SESSION_COOKIE + "=", ""));
    }
}
