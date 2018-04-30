package com.midas.fileprovider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.midas.fileprovider.utils.FileProvider7;
import com.midas.fileprovider.utils.MPermissionUtils;
import com.midas.fileprovider.utils.PhotoUtils;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Dell
 * @time 2018/4/27 10:01
 * @description: 安卓6.0 权限处理 7.0 FileProvider
 */
public class MainActivity extends AppCompatActivity {

    private CircleImageView mCircleImg;
    private Button mCameraBtn, mGalleryBtn;

    private static final int CODE_CAMERA_REQUEST = 0x110;//拍照
    private static final int CODE_GALLERY_REQUEST = 0x220;// 从相册中选择
    private static final int CODE_CROP_REQUEST = 0x330; // 裁剪后的图片
    private final int PERMISSION_REQUEST = 12700;//存储权限code 不能为负值,也不能大于16位bit值65536
    private File file = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");
    private File fileCrop = new File(Environment.getExternalStorageDirectory().getPath() + "/crop_photo.jpg");
    private Uri imageUri;
    private Uri cropImageUri;
    private String[] mPermissionArr;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleImg = (CircleImageView) findViewById(R.id.circleImg);
        mCameraBtn = (Button) findViewById(R.id.cameraBtn);
        mGalleryBtn = (Button) findViewById(R.id.galleryBtn);
        initView();
    }

    /**
     * init
     */
    private void initView() {
        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainPermissions("camera");
            }
        });
        mGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainPermissions("gallery");
            }
        });
    }

    /**
     * 获取权限
     */
    public void obtainPermissions(final String flag) {
        mPermissionArr = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        MPermissionUtils.requestPermissionsResult(this, PERMISSION_REQUEST, mPermissionArr, new MPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                switch (flag) {
                    case "camera":
                        //只需改变这一行
                        imageUri = FileProvider7.getUriForFile(getApplicationContext(), file);
                        PhotoUtils.takePicture(MainActivity.this, imageUri, CODE_CAMERA_REQUEST);
                        break;
                    case "gallery":
                        PhotoUtils.openPic(MainActivity.this, CODE_GALLERY_REQUEST);
                        break;
                }

            }

            @Override
            public void onPermissionDenied() {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("需打开权限再使用\n设置路径：设置->应用->Android7->权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                // 根据包名打开对应的设置界面
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //拍照完成回调
                case CODE_CAMERA_REQUEST:
                    cropImageUri = Uri.fromFile(fileCrop);
                    PhotoUtils.cropImageUri(this, imageUri, cropImageUri, 1, 1, 480, 480, CODE_CROP_REQUEST);
                    break;
                //访问相册完成回调
                case CODE_GALLERY_REQUEST:
                    cropImageUri = Uri.fromFile(fileCrop);
                    Uri newUri = Uri.parse(PhotoUtils.getPath(this, data.getData()));
                    newUri = FileProvider7.getUriForFile(getApplicationContext(), new File(newUri.getPath()));
                    PhotoUtils.cropImageUri(this, newUri, cropImageUri, 1, 1, 480, 480, CODE_CROP_REQUEST);
                    break;
                case CODE_CROP_REQUEST:
                    mBitmap = PhotoUtils.getBitmapFromUri(cropImageUri, this);
                    mCircleImg.setImageBitmap(mBitmap);
                    if (mBitmap != null) {
                        mCircleImg.setImageBitmap(mBitmap);
                    }
                    break;
            }
        }
    }

    /**
     * 权限回调
     */
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
