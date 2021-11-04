package com.function.karaoke.interaction.storage;

import androidx.annotation.NonNull;

import com.function.karaoke.interaction.activities.Model.InternetUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InternetUserDatabase {

    private static final String INTERNET_USERS = "internetUsers";


    public static void addUserToDatabase(InternetUser user, AddListener addListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        CollectionReference userCollectionRef = databaseDriver.getCollectionReferenceByName(INTERNET_USERS);
        userCollectionRef.document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        addListener.nameExists();
                    } else {
                        addUser();
                    }
                } else {
                    addListener.onFail();
                }
            }

            private void addUser() {
                userCollectionRef.document(user.getEmail()).set(user).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addListener.onFail();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addListener.onSuccess();
                    }
                });
            }
        });
    }


    public static void deleteUserFromDatabase(String userName, AddListener addListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        CollectionReference userCollectionRef = databaseDriver.getCollectionReferenceByName(INTERNET_USERS);
        userCollectionRef.document(userName).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                addListener.onFail();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addListener.onSuccess();
            }
        });
    }

    public static void editUserInDatabase(InternetUser user, AddListener addListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        CollectionReference userCollectionRef = databaseDriver.getCollectionReferenceByName(INTERNET_USERS);
        userCollectionRef.document(user.getEmail()).set(user).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                addListener.onFail();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addListener.onSuccess();
            }
        });
    }

    public static void getAllSongsInCollection(InternetUserListener internetUserListener) {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        CollectionReference userCollectionRef = databaseDriver.getCollectionReferenceByName(INTERNET_USERS);
        final List<InternetUser> documentsList = new ArrayList<>();
        userCollectionRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            documentsList.add(document.toObject(InternetUser.class));
                        }
                        internetUserListener.onSuccess(documentsList);
                    }
                }).addOnFailureListener(e -> {
            internetUserListener.onFail();
        });
    }

    public interface AddListener {
        void onSuccess();

        void onFail();

        void nameExists();
    }

    public interface InternetUserListener {
        void onSuccess(List<InternetUser> users);

        void onFail();
    }

}
