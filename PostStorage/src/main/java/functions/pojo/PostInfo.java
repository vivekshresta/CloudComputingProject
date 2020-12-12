package functions.pojo;

public class PostInfo {
    private String username;
    private String post;

    public PostInfo(String username, String post) {
        this.username = username;
        this.post = post;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
