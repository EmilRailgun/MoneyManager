package com.example.moneymanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moneymanager.R;
import com.example.moneymanager.constant.HuaweiConstant;
import com.example.moneymanager.constant.SharedPrefConstant;
import com.example.moneymanager.methods.SharedMethods;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class SignupActivity extends AppCompatActivity {
    public static final String ACTIVITY_NAME = "SignupActivity";

    ImageButton ib_back;
    Button btn_signup;
    TextView tv_signin, tv_error_username, tv_error_email, tv_error_password, tv_error_repassword;
    EditText et_username, et_email, et_password, et_repassword;
    LinearLayout ll_huawei;
    AccountAuthParams authParams;
    AccountAuthService service;

    private void getViews(){
        ib_back = findViewById(R.id.signup_ib_back);
        btn_signup = findViewById(R.id.signup_btn_signup);
        tv_signin = findViewById(R.id.signup_tv_signin);
        et_username = findViewById(R.id.signup_et_username);
        et_email = findViewById(R.id.signup_et_email);
        et_password = findViewById(R.id.signup_et_password);
        et_repassword = findViewById(R.id.signup_et_repassword);
        tv_error_username = findViewById(R.id.signup_tv_error_username);
        tv_error_email = findViewById(R.id.signup_tv_error_email);
        tv_error_password = findViewById(R.id.signup_tv_error_password);
        tv_error_repassword = findViewById(R.id.signup_tv_error_repassword);
        ll_huawei = findViewById(R.id.signup_ll_signin_huawei);
    }


    private void displayFormError(EditText et, TextView tv_error, String error, boolean[] errors){
        //Thanh border bottom EditText
        et.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_form_error)));
        tv_error.setText(error);
        tv_error.setVisibility(View.VISIBLE);
        errors[0] = true;
    }

    private void removeFormError(EditText et, TextView tv_error){
        et.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey_main)));
        tv_error.setText("");
        tv_error.setVisibility(View.GONE);
    }

    private void setEventListener(){
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
            }
        });

        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //L???y t??n activity g???i trang n??y
                String callingActivity = getIntent().getStringExtra("source");

                //Tr??nh m??? ??i m??? l???i
                //N???u trang n??y ???????c m??? t??? Signin => ch??? c???n tho??t activity hi??n t???i ????? quay l???i Signin
                if(callingActivity.equals(SigninActivity.ACTIVITY_NAME)){
                    finish();
                    overridePendingTransition(0,0);
                }
                //N???u trang n??y ???????c m??? t??? FirstLoad => M??? m???i trang Signin
                else if(callingActivity.equals(FirstLoadingActivity.ACTIVITY_NAME)){
                    Intent signinIntent = new Intent(SignupActivity.this, SigninActivity.class);
                    signinIntent.putExtra("source", ACTIVITY_NAME);
                    startActivity(signinIntent);
                    overridePendingTransition(0,0);
                }
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                String repassword = et_repassword.getText().toString();

                boolean[] errors = {false};

                //Check username
                if(username.isEmpty())
                    displayFormError(et_username, tv_error_username, "T??n t??i kho???n kh??ng ???????c tr???ng", errors);
                else if(username.length() < 5)
                    displayFormError(et_username, tv_error_username, "T??i kho???n kh??ng ???????c d?????i 5 k?? t???", errors);
                else
                    removeFormError(et_username, tv_error_username);


                //Check email
                if(email.isEmpty())
                    displayFormError(et_email, tv_error_email, "Email kh??ng ???????c tr???ng", errors);
                else if(email.indexOf('@') == -1)
                    displayFormError(et_email, tv_error_email, "Email kh??ng h???p l???", errors);
                else
                    removeFormError(et_email, tv_error_email);

                //Check password
                if(password.isEmpty())
                    displayFormError(et_password, tv_error_password, "M???t kh???u kh??ng ???????c tr???ng", errors);
                else if(password.length() < 5)
                    displayFormError(et_password, tv_error_password, "M???t kh???u kh??ng ???????c d?????i 5 k?? t???", errors);
                else
                    removeFormError(et_password, tv_error_password);

                //Check repassword
                if(!repassword.equals(password))
                    displayFormError(et_repassword, tv_error_repassword, "M???t kh???u x??c nh???n kh??ng tr??ng kh???p", errors);
                else
                    removeFormError(et_repassword, tv_error_repassword);

                if(!errors[0]){
                    //L???y danh s??ch user
                    //User ???????c l??u d?????i d???ng key-value: key d???ng "username_1", "password_2", .. v???i 1,2 l?? s??? th??? t??? user
                    SharedPreferences sharedPreferencesUsersList = getSharedPreferences(SharedPrefConstant.USERS_LIST, MODE_PRIVATE);
                    SharedPreferences.Editor usersListEditor = sharedPreferencesUsersList.edit();

                    //L???y t???ng user
                    SharedPreferences sharedPreferencesUsersTotal = getSharedPreferences(SharedPrefConstant.USERS_TOTAL, MODE_PRIVATE);
                    SharedPreferences.Editor usersTotalEditor = sharedPreferencesUsersTotal.edit();

                    int usersTotal = sharedPreferencesUsersTotal.getInt(SharedPrefConstant.USERS_TOTAL_VALUE, 0);

                    boolean usernameFound = false;
                    //Check tr??ng username
                    for(int i=1; i<=usersTotal; ++i){
                        if(username.equals(sharedPreferencesUsersList.getString(String.format("%s_%d", SharedPrefConstant.USER_USERNAME, i), ""))){
                            usernameFound = true;
                            break;
                        }
                    }

                    if(usernameFound){
                        displayFormError(et_username, tv_error_username, "T??n t??i kho???n ???? c?? ng?????i ?????t", errors);
                    }
                    else{
                        //T??ng t???ng user v?? ghi v??o danh s??ch
                        usersTotal++;

                        usersTotalEditor.putInt(SharedPrefConstant.USERS_TOTAL_VALUE, usersTotal);
                        usersTotalEditor.apply();

                        //L??u th??ng tin user
                        usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_USERNAME, usersTotal), username);
                        //L???y lu??n username l??m t??n v?? n???u ????ng k?? th??ng th?????ng
                        usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_WALLET_NAME, usersTotal), username);
                        usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_EMAIL, usersTotal), email);
                        usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_PASSWORD, usersTotal), password);
                        usersListEditor.putBoolean(String.format("%s_%d", SharedPrefConstant.USER_IS_HUAWEI, usersTotal), false);
                        usersListEditor.apply();

                        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    }
                }
            }
        });

        ll_huawei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
                service = AccountAuthManager.getService(SignupActivity.this, authParams);
                startActivityForResult(service.getSignInIntent(), HuaweiConstant.HUAWEI_AUTHORIZATION_CODE);
            }
        });
    }


    //Ph???n request code == HUAWEI_AUTHORIZATION_CODE tr??ng v???i file b??n signin
    // nh??? s???a c??? 2 file n???u thay ?????i
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Process the authorization result to obtain the authorization code from AuthAccount.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HuaweiConstant.HUAWEI_AUTHORIZATION_CODE) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                AuthAccount authAccount = authAccountTask.getResult();

                //L???y username = unionId do n?? c?? v??? l?? duy nh???t v?? k thay ?????i
                String username = authAccount.getUnionId();
                //L???y t??n v??
                String walletName = authAccount.getFamilyName() + " " + authAccount.getGivenName();

                //L???y danh s??ch user
                //User ???????c l??u d?????i d???ng key-value: key d???ng "username_1", "password_2", .. v???i 1,2 l?? s??? th??? t??? user
                SharedPreferences sharedPreferencesUsersList = getSharedPreferences(SharedPrefConstant.USERS_LIST, MODE_PRIVATE);
                SharedPreferences.Editor usersListEditor = sharedPreferencesUsersList.edit();

                //L???y t???ng user
                SharedPreferences sharedPreferencesUsersTotal = getSharedPreferences(SharedPrefConstant.USERS_TOTAL, MODE_PRIVATE);
                SharedPreferences.Editor usersTotalEditor = sharedPreferencesUsersTotal.edit();
                int usersTotal = sharedPreferencesUsersTotal.getInt(SharedPrefConstant.USERS_TOTAL_VALUE, 0);

                //Bi???n ki???m tra xem c?? t??m ???????c user ????ng nh???p v???i user trong danh s??ch kh??ng
                boolean usernameFound = false;
                //Check tr??ng username
                for(int i=1; i<=usersTotal; ++i){
                    if(username.equals(sharedPreferencesUsersList.getString(String.format("%s_%d", SharedPrefConstant.USER_USERNAME, i), ""))){
                        //L???y file l??u th??ng tin user ??ang ????ng nh???p
                        SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
                        SharedPreferences.Editor signingInEditor = sharedPreferencesSigningIn.edit();

                        //Set status ????ng nh???p th??nh true
                        signingInEditor.putString(SharedPrefConstant.SIGNING_IN_STATUS, SharedPrefConstant.SIGNING_IN_STATUS_VALUE);

                        //Set username ????ng nh???p
                        signingInEditor.putString(SharedPrefConstant.SIGNING_IN_USERNAME, username);

                        //Set name ????ng nh???p
                        signingInEditor.putString(SharedPrefConstant.SIGNING_IN_WALLET_NAME, walletName);

                        //Set isHuawei ????ng nh???p
                        signingInEditor.putBoolean(SharedPrefConstant.SIGNING_IN_IS_HUAWEI, true);

                        //L??u thay ?????i file
                        signingInEditor.apply();

                        //T??m th???y user trong danh s??ch
                        usernameFound = true;

                        Intent setMoneyIntent = new Intent(this, SetMoneyActivity.class);
                        startActivity(setMoneyIntent);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    }
                }
                if (usernameFound == false) {
                    //T??ng t???ng user v?? ghi v??o danh s??ch
                    usersTotal++;
                    usersTotalEditor.putString(SharedPrefConstant.USERS_TOTAL_VALUE, String.valueOf(usersTotal));
                    usersTotalEditor.apply();

                    //L??u th??ng tin user
                    usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_USERNAME, usersTotal), username);
                    //L???y lu??n username l??m t??n v?? n???u ????ng k?? th??ng th?????ng
                    usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_WALLET_NAME, usersTotal), walletName);
                    //C?? l?? huawei
                    usersListEditor.putString(String.format("%s_%d", SharedPrefConstant.USER_IS_HUAWEI, usersTotal), "true");
                    //L??u file
                    usersListEditor.apply();

                    //T??? ?????ng ????ng nh???p

                    //L???y file l??u th??ng tin user ??ang ????ng nh???p
                    SharedPreferences sharedPreferencesSigningIn = getSharedPreferences(SharedPrefConstant.SIGNING_IN, MODE_PRIVATE);
                    SharedPreferences.Editor signingInEditor = sharedPreferencesSigningIn.edit();

                    //Set status ????ng nh???p th??nh true
                    signingInEditor.putString(SharedPrefConstant.SIGNING_IN_STATUS, SharedPrefConstant.SIGNING_IN_STATUS_VALUE);

                    //Set username ????ng nh???p
                    signingInEditor.putString(SharedPrefConstant.SIGNING_IN_USERNAME, username);

                    //Set name ????ng nh???p
                    signingInEditor.putString(SharedPrefConstant.SIGNING_IN_WALLET_NAME, walletName);

                    //Set isHuawei ????ng nh???p
                    signingInEditor.putBoolean(SharedPrefConstant.SIGNING_IN_IS_HUAWEI, true);

                    //L??u file
                    signingInEditor.apply();


                    Intent setMoneyIntent = new Intent(this, SetMoneyActivity.class);
                    startActivity(setMoneyIntent);
                    overridePendingTransition(0,0);
                    finish();
                }
            } else {
                // ????ng nh???p th???t b???i
                Toast.makeText(this, "????ng nh???p th???t b???i!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getViews();
        setEventListener();
    }
}
