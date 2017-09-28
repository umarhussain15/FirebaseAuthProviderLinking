package umarhussain.com.firebaseauthproviderlinking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoggedIn extends AppCompatActivity {

    private static final int RC_SIGN_IN = 755;

    @BindView(R.id.buttonLogout)
    Button buttonLogout;

    @BindView(R.id.buttonEmail)
    Button buttonEmail;

    @BindView(R.id.buttonFacebook)
    Button buttonFacebook;

    @BindView(R.id.buttonGoogle)
    Button buttonGoogle;

    @BindView(R.id.editTextEmail)
    EditText editTextEmail;

    @BindView(R.id.editTextPassword)
    EditText editTextPassword;

    @BindView(R.id.textViewUserDirect)
    TextView textViewResult;

    @BindView(R.id.textViewProviders)
    TextView textViewProviders;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser firebaseUser;

    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;

    private String TAG ="LoggedIn Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser= firebaseAuth.getCurrentUser();

        // initialize state of the UI
        setButtonsAndLogs(firebaseUser.getProviderData());

        // reload user to dump user provider data.
        firebaseUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                StringBuilder stringBuilder = new StringBuilder();
                for (UserInfo userInfo : firebaseUser.getProviderData()) {
                    stringBuilder.append("PROVIDER: "+userInfo.getProviderId()+", Id"+userInfo.getUid() + ", email: " + userInfo.getEmail() + ", emailVerified: " + userInfo.isEmailVerified() + "\n\n");
                }
                Log.d(TAG,stringBuilder.toString());
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        // facebook setup
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess");

                AuthCredential emailAuthCredential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                // linking the facebook to the signin user.
                firebaseUser.linkWithCredential(emailAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            buttonFacebook.setText("Unlink Facebook");
                            // after account linking silently logout facebook since its not the primary method for current session login
                            LoginManager.getInstance().logOut();
                            setButtonsAndLogs(task.getResult().getUser().getProviderData());
                        }
                    }
                });
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }

    @OnClick({R.id.buttonEmail, R.id.buttonFacebook, R.id.buttonGoogle,R.id.buttonLogout})
    public void onClick(View view) {

        // linking and un-linking of providers based on the value of button (bad practice but needed for quick build)

        switch (view.getId()) {
            case R.id.buttonEmail:
                if (buttonEmail.getText().toString().equals("Unlink Email")) {
                    firebaseUser.unlink(EmailAuthProvider.PROVIDER_ID).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                buttonEmail.setText("Link Email");
                                setButtonsAndLogs(task.getResult().getUser().getProviderData());
                            }

                        }
                    });
                } else {

                    if (editTextEmail.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches()) {
                        Toast.makeText(LoggedIn.this, "Invalid Email",
                                Toast.LENGTH_SHORT).show();
                    } else if (editTextPassword.getText().toString().isEmpty() || editTextPassword.getText().toString().length() < 6) {
                        Toast.makeText(LoggedIn.this, "Password length less than 6 characters",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        AuthCredential emailAuthCredential = EmailAuthProvider.getCredential(editTextEmail.getText().toString(),
                                editTextPassword.getText().toString());
                        firebaseUser.linkWithCredential(emailAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    buttonEmail.setText("Unlink Email");
                                    setButtonsAndLogs(task.getResult().getUser().getProviderData());
                                }
                            }
                        });
                    }

                }

                break;
            case R.id.buttonFacebook:
                if (buttonFacebook.getText().toString().equals("Unlink Facebook")) {
                    firebaseUser.unlink(FacebookAuthProvider.PROVIDER_ID).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                buttonFacebook.setText("Link Facebook");
                                setButtonsAndLogs(task.getResult().getUser().getProviderData());
                            }

                        }
                    });
                } else {
                    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                }

                break;
            case R.id.buttonGoogle:
                // unlink
                if (buttonGoogle.getText().toString().equals("Unlink Google")) {
                    firebaseUser.unlink(GoogleAuthProvider.PROVIDER_ID).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                buttonGoogle.setText("Link Google");
                                setButtonsAndLogs(task.getResult().getUser().getProviderData());
                            }

                        }
                    });
                } else {
                    signInGoogle();
                }
                break;

            case R.id.buttonLogout:
                firebaseAuth.signOut();
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallbacks<Status>() {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        startActivity(new Intent(LoggedIn.this,MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {

                    }
                });
                break;
        }
    }

    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess() + ". " + result.getStatus().toString());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            Log.d(TAG, "handleSignInResult:" + result.getStatus().toString());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseUser.linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallbacks<Status>() {
                                @Override
                                public void onSuccess(@NonNull Status status) {
                                    buttonGoogle.setText("Unlink Google");
                                    setButtonsAndLogs(task.getResult().getUser().getProviderData());
                                }

                                @Override
                                public void onFailure(@NonNull Status status) {
                                    buttonGoogle.setText("Unlink Google");
                                    setButtonsAndLogs(task.getResult().getUser().getProviderData());
                                }
                            });
                        }

                    }
                });
    }

    private void setButtonsAndLogs(List<? extends UserInfo> providerData){

        // putting data to the text views for debugging

        StringBuilder stringBuilder = new StringBuilder();
        for (UserInfo userInfo : providerData) {
            stringBuilder.append("PROVIDER: "+userInfo.getProviderId()+", Id"+userInfo.getUid() + ", email: " + userInfo.getEmail() + ", emailVerified: " + userInfo.isEmailVerified() + "\n\n");
        }
        textViewProviders.setText(stringBuilder);
        textViewResult.setText("PROVIDER: "+firebaseUser.getProviderId()+"currentUser.getUid: "+firebaseUser.getUid()+", currentUser.getEmail: "+
                firebaseUser.getEmail()+
                ", currentUser.isVerified: "+
                firebaseUser.isEmailVerified()+"\n");

        // setting states of the buttons

        List<String> listProvider = firebaseUser.getProviders();

        if (listProvider.contains(EmailAuthProvider.PROVIDER_ID)) {

            setButton(buttonEmail,"Unlink Email",listProvider.size());
        }
        if (listProvider.contains(FacebookAuthProvider.PROVIDER_ID)) {

            setButton(buttonFacebook,"Unlink Facebook",listProvider.size());
        }
        if (listProvider.contains(GoogleAuthProvider.PROVIDER_ID)) {
            setButton(buttonGoogle,"Unlink Google",listProvider.size());
        }

    }

    private void setButton(Button button, String text, int listSize){
        button.setText(text);
        if(listSize==1){
            button.setEnabled(false);
        }
    }

}
