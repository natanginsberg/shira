package com.function.karaoke.hardware;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.activities.Model.enums.LoginState;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 101;
    private static final String TAG = "signed in";
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private AuthenticationDriver authenticationDriver;
    private UserInfo user;
    private Observer<Boolean> gettingNewUserSucceeded;
    private SignInViewModel signInViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticationDriver = new AuthenticationDriver();
        signInViewModel = ViewModelProviders.of(this).get(SignInViewModel.class);
        setContentView(R.layout.activity_sign_in);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("701925650903-cmh7htvq718jbea95a0ukbkmlttqb56b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        String id = authenticationDriver.getUserUid();
        setUpGettingNewUserSucceeded();
        signInViewModel.getUserFromDatabase().observe(this, gettingNewUserSucceeded);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
            signInViewModel.firebaseAuthWithGoogle(account.getIdToken());
            user = new UserInfo(account.getEmail(), account.getDisplayName(), authenticationDriver.getUserUid());

            signInViewModel.addNewUserToDatabase(user);
//            signInViewModel.addNewUserToDatabase();
            returnToMain();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void checkToSeeIfUserIsInDatabase() {
//        signInViewModel.isUserInDB();
    }

    private void returnToMain() {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra("User", user);
//            onActivityResult(0, RESULT_OK, intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setUpGettingNewUserSucceeded() {
        gettingNewUserSucceeded = success -> {
            if (success) {
                if ((user = signInViewModel.getUser()) != null) {
                    signInViewModel.setLoginState(LoginState.FINISH);
                    returnToMain();
                } else {
                    signInViewModel.createNewUser();
                    signInViewModel.setLoginState(LoginState.NEW_USER_SIGN_UP);
                }
            } else {
                Toast.makeText(this, "unable to sign in", Toast.LENGTH_LONG).show();
                signInViewModel.setLoginState(LoginState.NOT_SIGN_IN);
            }
//            updateAccordingToLoginState();
        };
    }


}