package net.lzzy.networkdisk.activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.feedbackComponents.DeviceInfo;
import net.lzzy.networkdisk.feedbackComponents.SendEmailAsync;
import net.lzzy.networkdisk.feedbackComponents.SystemLog;
import net.lzzy.networkdisk.feedbackComponents.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 借鉴https://github.com/webianks/EasyFeedback
 */

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_WITH_INFO = "with_info";
    public static final String KEY_EMAIL = "email";
    public String LOG_TO_STRING = SystemLog.extractLogToString();
    private EditText editText;
    private String emailId;
    private final int REQUEST_APP_SETTINGS = 321;
    private final int REQUEST_PERMISSIONS = 123;
    private String deviceInfo;
    private boolean withInfo;
    private int PICK_IMAGE_REQUEST = 125;
    private String realPath;
    private ImageView selectedImageView;
    private LinearLayout selectContainer;
    private String details;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_layout);

        init();


    }

    @SuppressLint("StringFormatInvalid")
    private void init() {

        editText = findViewById(
                R.id.editText);

        TextView info = findViewById(
                R.id.info_legal);
        FrameLayout selectImage = findViewById(
                R.id.selectImage);
        Button submitSuggestion = findViewById(
                R.id.submitSuggestion);
        selectedImageView = findViewById(
                R.id.selectedImageView);
        selectContainer = findViewById(
                R.id.selectContainer);
        Toolbar toolbar = findViewById(
                R.id.feedback_layout_toolbar);
        setSupportActionBar(toolbar);

        submitSuggestion.setOnClickListener(this);
        selectImage.setOnClickListener(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
        emailId = getIntent().getStringExtra(KEY_EMAIL);
        withInfo = getIntent().getBooleanExtra(KEY_WITH_INFO, false);
        details = getIntent().getStringExtra("details");
        deviceInfo = DeviceInfo.getAllDeviceInfo(this, false);

        if (withInfo) {

            CharSequence infoFeedbackStart = getResources().getString(
                    R.string.info_fedback_legal_start);
            SpannableString deviceInfo = new SpannableString(getResources().getString(
                    R.string.info_fedback_legal_system_info));
            deviceInfo.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    new AlertDialog.Builder(FeedbackActivity.this)
                            .setTitle(
                                    R.string.info_fedback_legal_system_info)
                            .setMessage(FeedbackActivity.this.deviceInfo)
                            .setPositiveButton(
                                    R.string.Ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
            }, 0, deviceInfo.length(), 0);
            CharSequence infoFeedbackAnd = getResources().getString(
                    R.string.info_fedback_legal_and);
            SpannableString systemLog = new SpannableString(getResources().getString(
                    R.string.info_fedback_legal_log_data));
            systemLog.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    new AlertDialog.Builder(FeedbackActivity.this)
                            .setTitle(
                                    R.string.info_fedback_legal_log_data)
                            .setMessage(FeedbackActivity.this.LOG_TO_STRING)
                            .setPositiveButton(
                                    R.string.Ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
            }, 0, systemLog.length(), 0);
            CharSequence infoFeedbackEnd = getResources().getString(
                    R.string.info_fedback_legal_will_be_sent, "技术员的邮箱");
            Spanned finalLegal = (Spanned) TextUtils.concat(infoFeedbackStart, deviceInfo, infoFeedbackAnd, systemLog, infoFeedbackEnd);


            info.setText(finalLegal);
            info.setMovementMethod(LinkMovementMethod.getInstance());

        } else
            info.setVisibility(View.GONE);

    }


    public void selectImage() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                return;

            } else
                //already granted
                selectPicture();


        } else {
            //normal process
            selectPicture();
        }


    }

    private void selectPicture() {

        realPath = null;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(
                R.string.select_picture_title)), PICK_IMAGE_REQUEST);
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(
                        R.string.Ok, okListener)
                .setNegativeButton(
                        R.string.cancel, null)
                .create()
                .show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSIONS:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    selectPicture();

                } else {
                    // Permission Denied
                    showMessageOKCancel("You need to allow access to SD card to select images.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    goToSettings();

                                }
                            });

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }


    private void goToSettings() {

        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_SETTINGS) {

            if (hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                //Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                selectPicture();

            } else {

                showMessageOKCancel("You need to allow access to SD card to select images.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                goToSettings();

                            }
                        });

            }
        } else if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK && data != null && data.getData() != null) {

            realPath = Utils.getPath(this, data.getData());

            selectedImageView.setImageBitmap(Utils.decodeSampledBitmap(realPath,
                    selectedImageView.getWidth(), selectedImageView.getHeight()));

            selectContainer.setVisibility(View.GONE);

            Toast.makeText(this, getString(
                    R.string.click_again), Toast.LENGTH_SHORT).show();


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission))
                return false;
        return true;
    }


    @SuppressLint("StringFormatInvalid")
    public void sendEmail(String body) {


        List<File> files = new ArrayList<>();


        if (withInfo) {
            File deviceInfoUri = createFileFromString(deviceInfo, getString(
                    R.string.file_name_device_info));
            files.add(deviceInfoUri);

            File logUri = createFileFromString(LOG_TO_STRING, getString(
                    R.string.file_name_device_log));
            if (details != null) {
                body = "反馈内容：{" + body + "} <br> 用户信息：" + details;
            }
            files.add(logUri);
        }

        if (realPath != null) {
            File img = new File(realPath);
            files.add(img);
        }
        // new EmailUtil("smtp.qq.com", "1844383457@qq.com", "反馈", "1844383457@qq.com", "pegdkhqvftnmdbbj")

        new SendEmailAsync(FeedbackActivity.this, emailId, files, "反馈", "smtp.qq.com", "1844383457@qq.com", "反馈", "1844383457@qq.com", "pegdkhqvftnmdbbj")
                .execute(body);

    }


    private File createFileFromString(String text, String name) {
        File file = new File(getExternalCacheDir(), name);
        //create the file if it didn't exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, false to overrite to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
            buf.write(text);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submitSuggestion:
                String suggestion = editText.getText().toString();
                if (suggestion.trim().length() > 0) {
                    sendEmail(suggestion);
                    finish();
                } else
                    editText.setError(getString(R.string.please_write));
                break;
            case R.id.selectImage:
                selectImage();
                break;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
