package net.lzzy.networkdisk.feedbackComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;



import java.io.File;
import java.util.List;


public class SendEmailAsync extends AsyncTask<String, Void, Boolean> {
    private String email;
    private List<File> files;
    private String subject;
    private String host;
    private String sender;
    private String name;
    private String username;
    private String password;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public SendEmailAsync(Context context, String email, List<File> files, String subject, String host, String sender, String name, String username, String password) {
        this.email = email;
        this.files = files;
        this.subject = subject;
        this.host = host;
        this.sender = sender;
        this.name = name;
        this.username = username;
        this.password = password;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... string) {
        EmailUtil mailUtil = new EmailUtil(host, sender, name, username, password);
        return mailUtil.send(email, subject, string[0], files);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            Toast.makeText(context, "反馈成功，感谢您对支持！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "发送失败！", Toast.LENGTH_SHORT).show();
        }

    }

}