package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import functions.helper.Constants;
import functions.pojo.PostInfo;
import functions.pojo.UserInfo;
import functions.services.PostStorage;
import functions.services.SessionStorage;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class Main implements HttpFunction {

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
            String username = SessionStorage.verifyUser(request);
            addCookie(response, username);
            String result = StringUtils.isEmpty(username) ? getLoginPage() : getAddFriendView(username);
            out.write(result);
        } else if(path.contains("addfriends")) {
            SessionStorage.addNewFriends(request);
            out.write(getPostsView(request));
        } else if(path.contains("addpost")) {
            submitPost(request);
            out.write(getPostsView(request));
        } else if(path.contains("logout")) {
            addCookie(response, "");
            out.write(getHomePage());
        }
    }

    private void submitPost(HttpRequest request) {
        PostStorage.submitPost(request);
    }

    private void addCookie(HttpResponse response, String username) {
        if(!StringUtils.isEmpty(username))
            response.appendHeader("Set-Cookie", Constants.SESSION_COOKIE + "=" + username);
    }

    private String getPostsView(HttpRequest request) {
        try {
            Optional<String> username = SessionStorage.getUsernameFromCookie(request);
            List<UserInfo> friends = SessionStorage.getCurrentFriends(request);
            Map<String, String> usernameToFullName = getUsernameToFullName(friends);
            List<PostInfo> posts = PostStorage.generateTimeline(request, friends);
            StringBuilder sb = new StringBuilder();
            sb.append(getAddPostView());
            for(PostInfo post : posts) {
                String name = username.get().equals(post.getUsername()) ? "Me" : post.getUsername();
                sb.append("<b>").append(usernameToFullName.get(name)).append(":</b><br>");
                sb.append(post.getPostData()).append("<br><br>");
            }

            sb.append(getLogoutView());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return getLoginPage();
        }
    }

    private Map<String, String> getUsernameToFullName(List<UserInfo> friends) {
        Map<String, String> usernameToFullname = new HashMap<>();
        for(UserInfo userInfo : friends)
            usernameToFullname.computeIfAbsent(userInfo.getUsername(), (k) -> userInfo.getFirstName() + " " + userInfo.getLastName());

        return usernameToFullname;
    }

    private String getAddPostView() {
        return "<form action = /frontend-function/addpost method=\"POST\">\n" +
                "  <b><label for=\"username\">Create a new post:</label><br></b>\n" +
                "  <input type=\"text\" id=\"post\" name=\"post\"><br>\n" +
                "  <input type=\"submit\" value=\"Submit\"><br><br>\n" +
                "</form>\n";
    }

    private String getLogoutView() {
        return "<form action = /frontend-function/logout method=\"POST\">\n" +
                "  <input type=\"submit\" value=\"Logout\"><br><br>\n" +
                "</form>\n";
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
                "</form>\n";
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
                "</form>\n";
    }

    private String getAddFriendView(String currentUser) {
        String searchUI = "<form action = /frontend-function/search method=\"POST\">\n" +
                "  <label for=\"search\">Search for a friend:</label><br>\n" +
                "  <input type=\"text\" id=\"query\" name=\"query\"><br><br>\n" +
                "</form>\n";

        List<UserInfo> newFriends = SessionStorage.getNewFriends(currentUser);
//        List<UserInfo> newFriends = new ArrayList<>();
//        newFriends.add(new UserInfo("kartik", "", "Kartik", "Mallajosyula"));
//        newFriends.add(new UserInfo("prashant", "", "prashanth", "sateesh"));
//        newFriends.add(new UserInfo("anshul", "", "Anshul", "Vohra"));
//        newFriends.add(new UserInfo("vansh", "", "Vansh", "Shah"));
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
                "</form>\n";
    }
}
