package net.lzzy.networkdisk.utils;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import net.lzzy.networkdisk.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImgUtils {

    public static final int TAKE_PHOTO = 1;//拍照
    public static final int CHOOSE_PHOTO = 2;//选择相册
    private static final int REQUEST_CODE_CAMERA = 3;//相机权限请求
    private static final int REQUEST_CODE_ALBUM = 4;//相册权限请求
    public static Uri imageUri;//相机拍照图片保存地址

    /**
     * 选择图片，从图库、相机
     *
     * @param activity 上下文
     */
    public static void choicePhoto(final Activity activity) {
        //采用的是系统Dialog作为选择弹框
        new AlertDialog.Builder(activity).setTitle("上传头像")//设置对话框标题
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 23) {//检查相机权限
                            ArrayList<String> permissions = new ArrayList<>();
                            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                permissions.add(Manifest.permission.CAMERA);
                            }

                            if (permissions.size() == 0) {//有权限,跳转
                                //打开相机-兼容7.0
                                openCamera(activity);
                            } else {
                                activity.requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_CAMERA);
                            }
                        } else {
                            //打开相机-兼容7.0
                            openCamera(activity);
                        }
                    }
                }).setNegativeButton("系统相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //如果有权限申请，请在Activity中onRequestPermissionsResult权限返回里面重新调用openAlbum()
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ALBUM);
                } else {
                    openAlbum(activity);
                }
            }
        }).show();//在按键响应事件中显示此对话框
    }

    /**
     * 打开相机
     * 兼容7.0
     */
    public static void openCamera(Activity activity) {
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(AppUtils.getTmpPath(), "tmp");//output_image.png
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            //Android 7.0系统开始 使用本地真实的Uri路径不安全,使用FileProvider封装共享Uri
            //参数二:fileprovider绝对路径 com.dyb.testcamerademo：项目包名
           // imgUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", outputImage);
            imageUri = FileProvider.getUriForFile(activity, activity.getPackageName()+".fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, TAKE_PHOTO);
    }

    public static void openAlbum(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "选择图片"), CHOOSE_PHOTO);

        //调用系统图库的意图
//        Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
//        choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        activity.startActivityForResult(choosePicIntent, CHOOSE_PHOTO);

        //打开系统默认的软件
        //Intent intent = new Intent("android.intent.action.GET_CONTENT");
        //intent.setType("image/*");
        //activity.startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    /**
     * 启动裁剪
     * http://blog.csdn.net/u011771755/article/details/50676888
     * http://blog.csdn.net/qq_37238649/article/details/78985518
     *
     * @param activity 上下文
     * @param uri      需要裁剪图片的Uri
     */
    public static void startUCrop(Activity activity, Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(activity.getCacheDir(), "tmp"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(60);   //设置裁剪图片的质量（0到100）
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);//设置裁剪出来图片的格式
        options.setMaxBitmapSize(900);//压缩图片
        options.setOvalDimmedLayer(true);//设置是否为圆形裁剪框
        options.setShowCropGrid(false);  //设置是否显示裁剪网格
        options.setShowCropFrame(false); //设置是否显示裁剪边框(true为方形边框)
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);//设置裁剪图片可操作的手势
        options.setToolbarColor(ActivityCompat.getColor(activity, R.color.colorPrimary)); //设置toolbar颜色
        options.setStatusBarColor(ActivityCompat.getColor(activity, R.color.colorPrimary)); //设置状态栏颜色

        UCrop.of(uri, destinationUri)
                .useSourceImageAspectRatio()
                .withAspectRatio(3, 3)
                .withMaxResultSize(600, 600)
                .withOptions(options)
                .start(activity);

    }

    /**
     * 删除临时文件
     */
    public static void deleteTempFile() {
        File tempFile = new File(AppUtils.getTmpPath() + "/tmp.png");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }
}

