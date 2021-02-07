package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class manage sign up and getting the user object.
 */
public class UserService extends ViewModel {
    private static final String DOWNLOADS = "shares";
    private static final String VIEWS = "views";
    private static final String TYPE = "subscriptionType";
    private static final String FREE_SHARES = "freeShares";
    private static final String EXPIRATION_DATE = "expirationDate";
    private static final String COUPONS_USED = "couponsUsed";
    private static final String FREE_SHARE_USED = "freeShares";
    private DatabaseDriver databaseDriver;
    private AuthenticationDriver authenticationDriver;
    private CollectionReference usersCollectionRef;
    public static final String COLLECTION_USERS_NAME = "users";
    public static final String UID = "id";
    private static final String TAG = UserService.class.getSimpleName();
    private UserInfo user;
    private DocumentReference userDocument;
    private final List<String> fields = new ArrayList<>();

    public UserService(DatabaseDriver databaseDriver, AuthenticationDriver authenticationDriver) {
        this.databaseDriver = databaseDriver;
        this.authenticationDriver = authenticationDriver;
        usersCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
    }

    public void getUserFromDatabase(GetUserListener getUserListener) {
        final List<UserInfo> documentsList = new LinkedList<>();
        Query getUserQuery = databaseDriver.getCollectionReferenceByName(UserService.COLLECTION_USERS_NAME).whereEqualTo(UserService.UID, authenticationDriver.getUserUid());
        getUserQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    user = null;
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        this.userDocument = task.getResult().getDocuments().get(0).getReference();
                        documentsList.add(document.toObject(UserInfo.class));
                    }
                    user = documentsList.get(0);
                    getUserListener.user(user);
                }
            } else {

            }
        });

//        return success;
    }

    public void getUser(GetUserListener getUserListener) {
        if (user == null) {
            final List<UserInfo> documentsList = new LinkedList<>();
            Query getUserQuery = databaseDriver.getCollectionReferenceByName(UserService.COLLECTION_USERS_NAME).whereEqualTo(UserService.UID, authenticationDriver.getUserUid());
            getUserQuery.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        user = null;
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            this.userDocument = task.getResult().getDocuments().get(0).getReference();
                            documentsList.add(document.toObject(UserInfo.class));
                        }
                        user = documentsList.get(0);
                        getUserListener.user(documentsList.get(0));
                    }
                } else {

                }
            });
        } else
            getUserListener.user(user);
    }

    public void addUserToDatabase(UserInfo user) {
        usersCollectionRef.add(user).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException("failed to add to firestore");
            }
        });
    }

    public void addSubscriptionType(UserUpdateListener userUpdateListener, int type) {
        if (user == null)
            getUser(new GetUserListener() {
                @Override
                public void user(UserInfo userInfo) {
                    changeType(userUpdateListener, type);
                }
            });
        else
            changeType(userUpdateListener, type);
    }

    private void changeType(UserUpdateListener userUpdateListener, int type) {
        Map<String, Object> data = new HashMap<>();
        data.put(TYPE, type);
        userDocument.update(data).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        userUpdateListener.onSuccess();
                    }
                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userUpdateListener.onFailure();
                    }
                });
    }

    public void signOut() {
        authenticationDriver.getAuth().signOut();
    }


    public void updateUserFields(UserUpdateListener userUpdateListener) {
        if (fields.size() > 0)
            if (user == null)
                getUser(userInfo -> updateFields(userUpdateListener));
            else
                updateFields(userUpdateListener);

    }

    private void updateFields(UserUpdateListener userUpdateListener) {
        Map<String, Object> data = new HashMap<>();
        if (fields.contains(VIEWS))
            data.put(VIEWS, user.getViews() + 1);
        if (fields.contains(DOWNLOADS))
            data.put(DOWNLOADS, user.getShares() + 1);
        if (fields.contains(FREE_SHARE_USED))
            data.put(FREE_SHARE_USED, user.getFreeShares() - 1);
        userDocument.update(data).
                addOnSuccessListener(aVoid -> userUpdateListener.onSuccess()).
                addOnFailureListener(e -> userUpdateListener.onFailure());
        fields.clear();
    }

    public void addFieldToUpdate(String field) {
        if (!fields.contains(field))
            fields.add(field);
    }

    public void changeFreeShares(int freeShares) {
        if (user == null)
            getUser(new GetUserListener() {
                @Override
                public void user(UserInfo userInfo) {
                    changeShares(freeShares);
                }
            });
        else
            changeShares(freeShares);
    }

    private void changeShares(int freeShares) {
        Map<String, Object> data = new HashMap<>();
        data.put(FREE_SHARES, user.getFreeShares() + freeShares);
        data.put(COUPONS_USED, user.getCouponUsed() + 1);
        userDocument.update(data);
    }

    public void changeExpirationDate(String date) {
        if (user == null)
            getUser(new GetUserListener() {
                @Override
                public void user(UserInfo userInfo) {
                    changeDate(date);
                }
            });
        else
            changeShares(user.getFreeShares());
    }

    private void changeDate(String date) {
        Map<String, Object> data = new HashMap<>();
        data.put(EXPIRATION_DATE, date);
        data.put(COUPONS_USED, user.getCouponUsed() + 1);
        userDocument.update(data);
    }

    public interface UserUpdateListener {
        void onSuccess();

        void onFailure();
    }

    public interface GetUserListener {
        void user(UserInfo userInfo);
    }
}