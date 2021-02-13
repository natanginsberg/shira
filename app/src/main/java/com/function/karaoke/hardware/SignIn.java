package com.function.karaoke.hardware;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.activities.Model.enums.LoginState;
import com.function.karaoke.hardware.utils.Checks;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class SignIn {

    private final Context context;
    private final FragmentActivity activity;
    private final LifecycleOwner lifeCycleOwner;
    private final ActivityResultLauncher<Intent> mGetContent;
    private SignInViewModel signInViewModel;
    private GoogleSignInClient mGoogleSignInClient;
    private Observer<Boolean> gettingNewUserSucceeded;
    private UserInfo user;

    public SignIn(FragmentActivity activity, Context context, LifecycleOwner lifecycleOwner, ActivityResultLauncher<Intent> mGetContent) {
        this.activity = activity;
        this.context = context;
        this.lifeCycleOwner = lifecycleOwner;
        this.mGetContent = mGetContent;
    }

    public void openSignIn() {


        signInViewModel = ViewModelProviders.of(activity).

                get(SignInViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("701925650903-cmh7htvq718jbea95a0ukbkmlttqb56b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        mGoogleSignInClient.signOut();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        setUpGettingNewUserSucceeded();
        signIn();
//        signInViewModel.validateCoupon().observe(lifeCycleOwner, gettingNewUserSucceeded);
    }

    private void setUpGettingNewUserSucceeded() {
        gettingNewUserSucceeded = (Boolean success) -> {
            if (success) {
                if ((user = signInViewModel.getUser()) != null) {
                    signInViewModel.setLoginState(LoginState.FINISH);
                } else {
                    signInViewModel.createNewUser();
                    signInViewModel.setLoginState(LoginState.NEW_USER_SIGN_UP);
                }
            } else {
                signInViewModel.setLoginState(LoginState.NOT_SIGN_IN);
            }
//            updateAccordingToLoginState();
        };
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        mGetContent.launch(signInIntent);

    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask, View view, SuccessFailListener resultSuccessFailListener) {
        try {
            signInViewModel = ViewModelProviders.of(activity).get(SignInViewModel.class);
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            signInViewModel.firebaseAuthWithGoogle(account.getIdToken(), new SignInViewModel.SuccessFailListener() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    signInViewModel.isUserInDatabase(new SignInViewModel.DatabaseListener() {

                        @Override
                        public void isInDatabase(boolean inDatabase) {
                            if (inDatabase) {
                                user = signInViewModel.getUser();
                            } else {
                                user = new UserInfo(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                        }

                        @Override
                        public void failedToSearchDatabase() {
                            if (checkForInternet(view)) {
                                user = new UserInfo(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                        }
                    });
                    resultSuccessFailListener.onSuccess();

                }

                @Override
                public void onFailure() {
                    resultSuccessFailListener.onFailure();
                }
            });
        } catch (Exception e) {
//                Toast.makeText(this, context.getResources().getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkForInternet(View view) {
        return Checks.checkForInternetConnection(view, context);
    }

    public UserInfo getUser() {
        return user;
    }
}
