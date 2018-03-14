package net.lzzy.networkdisk.utils;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;

import net.lzzy.networkdisk.R;

/**
 * 计时类
 */
public class TimeCountUtils extends CountDownTimer {
    private static final int TIME = 1000;
    private Button button;
    private EditText editText;

    public TimeCountUtils(long millisInFuture, Button button, EditText editText) {
        super(millisInFuture, TIME);
        this.button = button;
        this.editText = editText;
    }

    @Override
    public void onFinish() {// 计时完毕
        editText.setEnabled(true);
        button.setEnabled(true);
        button.setBackgroundResource(R.drawable.btn_bg_activate);
        button.setText("重新获取");

    }

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    public void onTick(long millisUntilFinished) {// 计时过程

        button.setText("已发送(" + millisUntilFinished / TIME + ")");

    }


}