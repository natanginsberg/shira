package com.function.karaoke.interaction.activities.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.function.karaoke.interaction.activities.Model.enums.LoginState;
import com.function.karaoke.interaction.storage.AuthenticationDriver;
import com.function.karaoke.interaction.storage.DatabaseDriver;
import com.function.karaoke.interaction.storage.UserService;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SignInViewModel extends ViewModel {

    private static final String TAG = SignInViewModel.class.getSimpleName();

    private UserInfo user;
    private final DatabaseDriver databaseDriver;
    private final AuthenticationDriver authenticationDriver;
    private LoginState loginState;

    private Boolean mAccessingDatabase;
    private final UserService userService;
    private final int NUMBER_OF_FREE_SHARES = 1;

    public SignInViewModel() {
        mAccessingDatabase = false;
        databaseDriver = new DatabaseDriver();
        authenticationDriver = new AuthenticationDriver();
        userService = new UserService(databaseDriver, authenticationDriver);

        loginState = LoginState.NEW_USER_SIGN_UP;
        if (authenticationDriver.isSignIn()) {
            loginState = LoginState.SIGN_IN_GET_USER;
        } else {
            loginState = LoginState.NOT_SIGN_IN;
        }
    }

    public UserInfo getUser() {
        return user;
    }

    public void createNewUser() {
        user = new UserInfo(authenticationDriver.getUserUid());
    }

    public void addNewUserToDatabase() {
        userService.addUserToDatabase(user);
    }

    public void addNewUserToDatabase(UserInfo userInfo) {
        userService.addUserToDatabase(userInfo);
    }

    public Boolean getmAccessingDatabase() {
        return mAccessingDatabase;
    }

    public LoginState getLoginState() {
        return loginState;
    }

    public void setLoginState(LoginState loginState) {
        this.loginState = loginState;
    }

    public LiveData<Boolean> signInWithGoogle(GoogleSignInAccount account) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authenticationDriver.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    // Sign in success, update ui with the signed-in user's information
                    // If sign in fails, display a message to the user.
                    success.setValue(task.isSuccessful());
                });
        return success;
    }


    public MutableLiveData<Boolean> getUserFromDatabase() {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        final List<UserInfo> documentsList = new LinkedList<>();
        mAccessingDatabase = true;
        Query getUserQuery = databaseDriver.getCollectionReferenceByName(UserService.COLLECTION_USERS_NAME).whereEqualTo(UserService.UID, authenticationDriver.getUserUid());
        getUserQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    user = null;
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentsList.add(document.toObject(UserInfo.class));
                    }
                    user = documentsList.get(0);
                }
                success.setValue(true);
            } else {
                success.setValue(false);
            }
            mAccessingDatabase = false;
        });

        return success;
    }


    public void isUserInDatabase(DatabaseListener databaseListener) {
        mAccessingDatabase = true;
        Query getUserQuery = databaseDriver.getCollectionReferenceByName(UserService.COLLECTION_USERS_NAME).whereEqualTo(UserService.UID, authenticationDriver.getUserUid());
        getUserQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    databaseListener.isInDatabase(false);
                } else {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        user = (document.toObject(UserInfo.class));
                    }
                    databaseListener.isInDatabase(true);
                }
            } else {
                databaseListener.failedToSearchDatabase();
            }
            mAccessingDatabase = false;
        });
    }

    public void firebaseAuthWithGoogle(String idToken, SuccessFailListener successFailListener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authenticationDriver.getAuth().signInWithCredential(credential).addOnSuccessListener(authResult -> {
            FirebaseUser user = authenticationDriver.getAuth().getCurrentUser();
            successFailListener.onSuccess(user);

        });
    }

    public void createGuestId(OnCompleteListener<AuthResult> listener) {
        authenticationDriver.createGuestId(listener);
    }

    private void updateUI(FirebaseUser user) {
    }

    public interface DatabaseListener {
        void isInDatabase(boolean inDatabase);

        void failedToSearchDatabase();
    }

    public interface SuccessFailListener {
        void onSuccess(FirebaseUser firebaseUser);

        void onFailure();
    }

}