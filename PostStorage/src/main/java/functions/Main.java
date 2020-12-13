package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import functions.cache.Cache;
import functions.helper.Constants;
import functions.helper.PostBodyParser;
import functions.pojo.PostInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
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
            case "submitPost":
                out.write(submitPost(postBody.get("username"), postBody.get("post")));
                break;
            case "generateTimeline":
                out.write(generateTimeline(postBody.get("username"), postBody.get("friends")));
                break;
        }
    }

    private String submitPost(String username, String post) {
        StringBuilder json = new StringBuilder();
        json.append("{\n").append("\"status\"").append(":");

        if(StringUtils.isEmpty(username))
            json.append("\"").append(Constants.FAIL).append("\"");
        else
            json.append("\"").append(cache.addNewPost(username, post)).append("\"");

        return json.append("\n}").toString();
    }

    private String generateTimeline(String username, String friendsStr) {
        friendsStr = friendsStr.trim();
        Set<PostInfo> posts = new HashSet<>();
        List<String> friends = new ArrayList<>(Arrays.asList(friendsStr.split(", ")));
        friends.add(username);

        for(String friend : friends) {
            PostInfo postInfo = cache.getPosts(friend);
            if(postInfo != null)
                posts.add(postInfo);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        if(!posts.isEmpty()) {
            for (PostInfo post : posts) {
                json.append("\"").append(post.getUsername()).append("\"").append(":");
                json.append("\"").append(post.getPost()).append("\"").append(",\n");
            }
            json.replace(json.length() - 2, json.length(), "");
        }
        json.append("\n}");

        return json.toString();
    }
}
