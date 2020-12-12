package functions.cache;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import functions.helper.Constants;
import functions.pojo.UserInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Cache {
    private static final String projectID = "vivekshresta-bandaru";
    private static final String USER_INFO = "UserInfo";
    private static final String FRIENDS_INFO = "FriendsInfo";

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

    public String registerUser(String username, String password, String firstName, String lastName) {
        DocumentReference docRef = db.collection(USER_INFO).document(username);
        Map<String, Object> data = new HashMap<>();
        data.put("password", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("username", username);
        docRef.set(data);
        System.out.println("New user added to FireStore db: " + username);

        return "Success";
    }

    public String verifyUser(String username, String password) {
        DocumentReference docRef = db.collection(USER_INFO).document(username);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists() && password.equals(document.getData().get("password")))
                return Constants.SUCCESS;
            else
                return Constants.FAIL;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Constants.FAIL;
    }

    public void addNewFriends(String username, String friendUserNames) {
        DocumentReference docRef = db.collection(FRIENDS_INFO).document(username);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                docRef.update("friends", data.get("friends") + ", " + friendUserNames);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("friends", friendUserNames);
                docRef.set(data);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Set<UserInfo> getNewFriends(String username) {
        Set<UserInfo> users = new HashSet<>(getAllUsers());
        users.remove(new UserInfo(username, "", "", ""));

        DocumentReference docRef = db.collection(FRIENDS_INFO).document(username);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists()) {
                String friendsStr = (String) document.getData().get("friends");
                String[] friends = friendsStr.split(", ");
                for(String friend : friends)
                    users.remove(new UserInfo(friend, "", "", ""));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return users;
    }

    private List<UserInfo> getAllUsers() {
        List<UserInfo> result = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = db.collection(USER_INFO).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                UserInfo userInfo = new UserInfo((String)document.get("username"), (String)document.get("password"),
                        (String)document.get("firstName"), (String)document.get("lastName"));
                result.add(userInfo);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<UserInfo> getCurrentFriends(String username) {
        List<UserInfo> currentFriends = new ArrayList<>();
        DocumentReference docRef = db.collection(FRIENDS_INFO).document(username);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        try {
            List<UserInfo> allUsers = getAllUsers();
            DocumentSnapshot document = futureSnapshot.get();
            if (document.exists()) {
                String[] friends = ((String) document.getData().get("friends")).split(", ");
                currentFriends = matchUsers(allUsers, friends);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return currentFriends;
    }

    private List<UserInfo> matchUsers(List<UserInfo> allUsers, String[] friends) {
        List<UserInfo> result = new ArrayList<>();

        for(String friend : friends)
            for(UserInfo userInfo : allUsers)
                if(userInfo.getUsername().equals(friend))
                    result.add(userInfo);

        return result;
    }
}
