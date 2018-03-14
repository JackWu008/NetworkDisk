package net.lzzy.networkdisk.models;

import android.content.Context;
import android.content.Intent;

import net.lzzy.networkdisk.activitys.FeedbackActivity;


public class Feedback {

    private Context context;
    private String emailId;
    private boolean withSystemInfo;
    private String details;

    public Feedback(Builder builder) {

        this.emailId = builder.emailId;
        this.context = builder.context;
        this.withSystemInfo = builder.withSystemInfo;
        this.details = builder.details;
    }

    public static class Builder {

        private Context context;
        private String emailId;
        private boolean withSystemInfo;
        private String details;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder withEmail(String email) {
            this.emailId = email;
            return this;
        }

        public Builder userInfo(String details) {
            this.details = details;
            return this;
        }

        public Builder withSystemInfo() {
            withSystemInfo = true;
            return this;
        }


        public Feedback build() {
            return new Feedback(this);
        }

    }

    public void start() {

        Intent intent = new Intent(context, FeedbackActivity.class);
        intent.putExtra("email", emailId);
        intent.putExtra("with_info", withSystemInfo);
        intent.putExtra("details", details);
        context.startActivity(intent);


    }

}
