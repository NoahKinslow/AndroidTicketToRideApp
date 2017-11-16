package tewesday.androidtickettorideapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;


public class AuthenticationActivity extends AppCompatActivity
{
    private String TAG = AuthenticationActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private String mUsernameText;
    private String mPasswordText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_authentication);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            //updateUI(currentUser);
            Toast.makeText(AuthenticationActivity.this, "Signed in as" + mAuth.getCurrentUser().getDisplayName(),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            Button signInButton = findViewById(R.id.signInButton);
            signInButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TextView usernameTextView = findViewById(R.id.usernameText);
                    TextView passwordTextView = findViewById(R.id.passwordText);
                    mUsernameText = usernameTextView.getText().toString();
                    mPasswordText = passwordTextView.getText().toString();
                    signIntoAccount(mUsernameText, mPasswordText);
                }
            });
            Button signUpButton = findViewById(R.id.signUpButton);
            signUpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TextView usernameTextView = findViewById(R.id.usernameText);
                    TextView passwordTextView = findViewById(R.id.passwordText);
                    mUsernameText = usernameTextView.getText().toString();
                    mPasswordText = passwordTextView.getText().toString();
                    createAccount(mUsernameText, mPasswordText);
                }
            });
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void createAccount(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            setResult(RESULT_OK);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            setResult(RESULT_CANCELED);
                        }
                        finish();

                        // ...
                    }
                });
    }

    public void signIntoAccount(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

}
