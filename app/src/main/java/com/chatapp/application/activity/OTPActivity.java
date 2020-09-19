package com.chatapp.application.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.chatapp.application.R;
import com.chatapp.application.customloadingdialog.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    private static final String TAG = "OTPActivity";

    String numberVerification;
    PhoneAuthProvider.ForceResendingToken resendToken;


    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //declaring firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();



        final String phoneNumber = getIntent().getStringExtra("getFullPhoneNumber");



        final CustomLoadingDialog loadingDialog = new CustomLoadingDialog(OTPActivity.this);
        loadingDialog.startLoadingDialog();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendPhoneNumberVerificationCode(phoneNumber);

                loadingDialog.dismissDialog();
            }
        }, 6500);


//        sign_up.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String code = phNumberCode.getText().toString().trim();
//                if (code.isEmpty() || code.length() < 6) {
//                    phNumberCode.setError("Enter correct code");
//                    phNumberCode.requestFocus();
//                    return;
//                }
//
//                verifyCode(code);
//            }
//        });
    }



    private void sendPhoneNumberVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,                 // Phone number to verify
                60,                  // Timeout duration
                TimeUnit.SECONDS,       // Unit of timeout
                this,           // Activity (for callback binding)
                mCallbacks);            // OnVerificationStateChangedCallbacks
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
//                Log.w(TAG, "onVerificationFailed", e);
            Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId, token);

            Log.d(TAG, "onCodeSent:" + verificationId);

            //Save verification ID and resending token so we can use them later
            numberVerification = verificationId;
            resendToken = token;
        }
    };




    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(numberVerification, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            String getUserId = firebaseAuth.getCurrentUser().getUid();
                            databaseReference.child("Users").child(getUserId).setValue("");


                            Intent intent = new Intent(OTPActivity.this, ProfileSetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                            finish();

                        } else {
                        Toast.makeText(OTPActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}