package functions.pojo;

public class PostInfo {
    private String username;
    private String postData;

    public PostInfo(String username, String postData) {
        this.username = username;
        this.postData = postData;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }
}
