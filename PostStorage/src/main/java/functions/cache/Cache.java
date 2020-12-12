package functions.cache;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import functions.helper.Constants;
import functions.pojo.PostInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Cache {
    private static final String projectID = "vivekshresta-bandaru";
    private static final String USER_INFO = "UserInfo";
    private static final String FRIENDS_INFO = "FriendsInfo";
    private static final String POSTS_INFO = "PostsInfo";

    private Firestore db;

    public Cache() {
        FirestoreOptions firestoreOptions;
        try {
            firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                    .setProjectId(projectID)
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();
            db = firestoreOptions.getService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String addNewPost(String username, String post) {
        DocumentReference docRef = db.collection(POSTS_INFO).document(username);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                docRef.update("posts", data.get("posts") + Constants.POST_SEPARATION + post);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("posts", post);
                docRef.set(data);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Constants.FAIL;
        }

        return Constants.SUCCESS;
    }

    public PostInfo getPosts(String friend) {
        DocumentReference docRef = db.collection(POSTS_INFO).document(friend);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists())
                return new PostInfo(friend, (String) document.getData().get("posts"));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
