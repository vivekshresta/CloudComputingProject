package functions.cache;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Cache {
    private static final String projectID = "vivekshresta-bandaru";
    private static final String collectionID = "UIContent";

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

    public void addUIContent(String url, String imageURL, String aggregatedFrequencies) {
        url = getEncodedURL(url);
        DocumentReference docRef = db.collection(collectionID).document(url);
        Map<String, Object> data = new HashMap<>();
        data.put("imageURL", imageURL);
        data.put("aggregatedFrequencies", aggregatedFrequencies);
        ApiFuture<WriteResult> result = docRef.set(data);
        try {
            System.out.println("FireStore db updated with the url: " + url + ", at : " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public UIContent getUIContent(String url) {
        url = getEncodedURL(url);
        UIContent uiContent = null;
        try {
            DocumentReference docRef = db.collection(collectionID).document(url);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if(document.exists()) {
                Map<String, Object> data = document.getData();
                uiContent = new UIContent((String)data.get("imageURL"), (String)data.get("aggregatedFrequencies"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uiContent;
    }

    private String getEncodedURL(String url) {
        return Base64.getEncoder().encodeToString(url.getBytes());
    }
}
