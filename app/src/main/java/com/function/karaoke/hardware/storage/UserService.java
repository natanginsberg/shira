package com.function.karaoke.hardware.storage;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class manage sign up and getting the user object.
 */
public class UserService extends ViewModel {
    private static final int NUMBER_OF_FREE_SHARES = 1;
    private static final String SHARES = "shares";
    private static final String TYPE = "subscriptionType";
    private DatabaseDriver databaseDriver;
    private AuthenticationDriver authenticationDriver;
    private CollectionReference usersCollectionRef;
    public static final String COLLECTION_USERS_NAME = "users";
    public static final String UID = "id";
    private static final String TAG = UserService.class.getSimpleName();
    private UserInfo user;
    private DocumentReference userDocument;

    public UserService(DatabaseDriver databaseDriver, AuthenticationDriver authenticationDriver) {
        this.databaseDriver = databaseDriver;
        this.authenticationDriver = authenticationDriver;
        usersCollectionRef = databaseDriver.getCollectionReferenceByName(COLLECTION_USERS_NAME);
    }

    public void getUserFromDatabase(SignInViewModel.FreeShareListener freeShareListener) {

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
                    freeShareListener.hasFreeAcquisition(user.getShares() < NUMBER_OF_FREE_SHARES);
                }
            } else {

            }
        });

//        return success;
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
        Map<String, Object> data = new HashMap<>();
        data.put(TYPE, type );
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


    public void addOneToUserShares(UserUpdateListener userUpdateListener) {
        Map<String, Object> data = new HashMap<>();
        data.put(SHARES, user.getShares() + 1);
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

    public interface UserUpdateListener {
        void onSuccess();

        void onFailure();
    }
}