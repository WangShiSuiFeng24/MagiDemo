package com.example.magidemo;

/**
 * Created by Andong Ming on 17/10/2019.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


//import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import android.support.v7.app.AlertDialog;

public class CameraPreview extends Activity {
//public class CameraPreview extends MyCanvas {

    private SurfaceView preview=null;
    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private Camera.Parameters params;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;
    private boolean isRecording;
    private boolean isFlashOn;

    private boolean isPictureEnlarge;

    private MediaRecorder mediaRecorder;
    private static int currentCameraId = 0;
    private Bitmap rotatedBitmap;
    private RelativeLayout captureMedia;
    private FrameLayout editMedia;
    private CircleProgressBar customButton;
    private ImageView switchCameraBtn;
    private ImageView flashButton;

    private ImageView pictureButton;

    private ImageView uploadButton;
    private TextView uploadButtonTxt;
    private ImageView EditCaptureSwitchBtn;
    private LinearLayout editTextBody;
    private ImageView capturedImage;
    private VideoView videoView;
    int VideoSeconds = 1;
    int noti_id;

    private ImageView guide;
    private ImageView timer;

    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;

    public File dir;
    public String defaultVideo;

    private MediaController mediaController;

    //图片放大缩小
    Bitmap bp=null;
    ImageView imageView;
    float scaleWidth;
    float scaleHeight;

    int h;
    boolean num=false;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);
        GetPermission();

        //图片放大缩小
        Display display = getWindowManager().getDefaultDisplay();

        Point size =new Point();
        display.getSize(size);

        // 加载mageView和获得图片的信i息
        imageView = (ImageView) findViewById(R.id.image_View);
        bp = BitmapFactory.decodeResource(getResources(),R.drawable.picture_demo);
        int bitmapWidth = bp.getWidth();
        int bitmapHeight = bp.getHeight();

        // 获得屏幕的宽高
        int screenWidth = size.x;
        int screenHeight = size.y;

        // 计算缩放比，因为如果图片的尺寸超过屏幕，那么就会自动匹配到屏幕的尺寸去显示。
        // 那么，我们就不知道图片实际上在屏幕上显示的宽高，所以先计算需要全部显示的缩放比，
        // 在去计算图片显示时候的实际宽高，然后，才好进行下一步的缩放。
        // 要不然，会导致缩小和放大没效果，或者内存泄漏等等
        scaleWidth = ((float)screenWidth) / bitmapWidth;
        scaleHeight = ((float)screenHeight) / bitmapHeight;
        imageView.setImageBitmap(bp);


        captureMedia = (RelativeLayout) findViewById(R.id.camera_view);
        editMedia = (FrameLayout) findViewById(R.id.edit_media);
        customButton = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        switchCameraBtn = (ImageView) findViewById(R.id.img_switch_camera);
        flashButton = (ImageView) findViewById(R.id.img_flash_control);

        pictureButton = (ImageView) findViewById(R.id.picture_button);

        //uploadButton = (ImageView) findViewById(R.id.upload_media);
        uploadButtonTxt = (TextView) findViewById(R.id.upload_media_txt);
        uploadButtonTxt.setText("");
        editTextBody = (LinearLayout) findViewById(R.id.editTextLayout);
        //selectSticker  = (LinearLayout) findViewById(R.id.select_sticker);
//        ImageView addText = (ImageView) findViewById(R.id.add_text);
//        ImageView addSticker = (ImageView) findViewById(R.id.add_stickers);
        isRecording = false;
        isFlashOn = false;

        isPictureEnlarge = false;

        EditCaptureSwitchBtn = (ImageView) findViewById(R.id.cancel_capture);
        capturedImage = (ImageView) findViewById(R.id.captured_image);
        videoView = (VideoView) findViewById(R.id.captured_video);

        preview=(SurfaceView)findViewById(R.id.preview);
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        noti_id = (int) ((new Date().getTime()/1000L)%Integer.MAX_VALUE);

        guide = (ImageView)findViewById(R.id.img_guide);
        timer = (ImageView)findViewById(R.id.img_timer);

        //setting dir and VideoFile value
        File sdCard = Environment.getExternalStorageDirectory();
        dir = new File(sdCard.getAbsolutePath() + "/Opendp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        defaultVideo =  dir + "/defaultVideo.mp4.nomedia";
        File createDefault = new File(defaultVideo);
        if (!createDefault.isFile()) {
            try {
                FileWriter writeDefault = new FileWriter(createDefault);
                writeDefault.append("yy");
                writeDefault.close();
                writeDefault.flush();
            } catch (Exception ex) {
            }
        }

//        uploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //perform upload function here
//            }
//        });



        customButton.setOnTouchListener(new View.OnTouchListener() {

            private Timer timer = new Timer();
            private long LONG_PRESS_TIMEOUT = 1000;
            private boolean wasLong = false;



            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(getClass().getName(), "touch event: " + event.toString());

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // touch & hold started
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            wasLong = true;
                            // touch & hold was long
                            Log.i("Click","touch & hold was long");
                            VideoCountDown.start();
                            try {
                                startRecording();
                            } catch (IOException e) {
                                String message = e.getMessage();
                                Log.i(null, "Problem " + message);
                                mediaRecorder.release();
                                e.printStackTrace();
                            }
                        }
                    }, LONG_PRESS_TIMEOUT);
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // touch & hold stopped
                    timer.cancel();
                    if(!wasLong){
                        // touch & hold was short
                        Log.i("Click","touch & hold was short");
                        if (isFlashOn && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            params = camera.getParameters();
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(params);

                            camera.autoFocus(new Camera.AutoFocusCallback() {

                                @Override
                                public void onAutoFocus(final boolean success, final Camera camera) {
                                    takePicture();
                                }
                            });

                        } else {
                            takePicture();
                        }
                    } else {
                        stopRecording();
                        VideoCountDown.cancel();
                        VideoSeconds = 1;
                        customButton.setProgressWithAnimation(0);
                        wasLong = false;
                    }
                    timer = new Timer();
                    return true;
                }

                return false;
            }
        });

        //点击preview调用聚焦方法
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FocusCamera();
            }
        });

        //切换摄像机镜头
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        //弹出guide视频对话框
        guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide();
            }
        });

        //弹出timer对话框
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimer();
            }
        });

        //左上角X按钮
        EditCaptureSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCaptureSwitch();
            }
        });

        /*
        editTextBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {showHideEditText();
            }
        });
        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {showHideEditText();
            }
        });
        addSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stickerOptions();
            }
        });
        editMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //StickerView.invalidate();
            }
        });
        */
    }//onCreate

    @SuppressLint("NewApi")
    public void GetPermission() {

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermission(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            finish();
        }
    }

    public static boolean hasPermission(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //倒计时器
    CountDownTimer VideoCountDown = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            VideoSeconds++;
            int VideoSecondsPercentage = VideoSeconds * 10;
            customButton.setProgressWithAnimation(VideoSecondsPercentage);
        }

        @Override
        public void onFinish() {
            stopRecording();
            customButton.setProgress(0);
            VideoSeconds = 0;
        }
    };

    //摄像头Camera聚焦
    public void FocusCamera(){
        if (camera.getParameters().getFocusMode().equals(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        } else {
            camera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(final boolean success, final Camera camera) {
                }
            });
        }
    }

    //拍照片
    private void takePicture() {
        params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        List<Integer> list = new ArrayList<Integer>();
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            Log.i("ASDF", "Supported Picture: " + size.width + "x" + size.height);
            list.add(size.height);
        }

        Camera.Size cs = sizes.get(closest(1080, list));
        Log.i("Width x Height", cs.width+"x"+cs.height);
        params.setPictureSize(cs.width, cs.height); //1920, 1080

        //params.setRotation(90);
        camera.setParameters(params);
        camera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, final Camera camera) {
                Bitmap bitmap;
                Matrix matrix = new Matrix();

                //if (bitmap.getWidth() > bitmap.getHeight()) {
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    matrix.postRotate(90);
                } else {
                    Matrix matrixMirrory = new Matrix();
                    float[] mirrory = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    matrixMirrory.setValues(mirrory);
                    matrix.postConcat(matrixMirrory);
                    matrix.postRotate(90);
                }
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                /*} else {
                    rotatedBitmap = bitmap;
                }*/

                if (rotatedBitmap != null) {
                    //setStickerView(0);
                    capturedImage.setVisibility(View.VISIBLE);
                    capturedImage.setImageBitmap(rotatedBitmap);
                    editMedia.setVisibility(View.VISIBLE);
                    captureMedia.setVisibility(View.GONE);

                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    Log.i("Image bitmap", rotatedBitmap.toString()+"-");
                } else {
                    Toast.makeText(CameraPreview.this, "Failed to Capture the picture. kindly Try Again:",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    //开始拍摄
    protected void startRecording() throws IOException {
        if (camera == null) {
            camera = Camera.open(currentCameraId);
            Log.i("Camera","Camera open");
        }
        params = camera.getParameters();

        if (isFlashOn && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
        }

        mediaRecorder = new MediaRecorder();
        camera.lock();
        camera.unlock();
        // Please maintain sequence of following code.
        // If you change sequence it will not work.
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setPreviewDisplay(previewHolder.getSurface());

        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(setCameraDisplayOrientation(this, currentCameraId, camera));
        }
        mediaRecorder.setVideoEncodingBitRate(3000000);
        mediaRecorder.setVideoFrameRate(30);

        List<Integer> list = new ArrayList<Integer>();

        List<Camera.Size> VidSizes = params.getSupportedVideoSizes();
        if (VidSizes == null) {
            Log.i("Size length", "is null");
            mediaRecorder.setVideoSize(640,480);
        } else {
            Log.i("Size length", "is NOT null");
            for (Camera.Size sizesx : params.getSupportedVideoSizes()) {
                Log.i("ASDF", "Supported Video: " + sizesx.width + "x" + sizesx.height);
                list.add(sizesx.height);
            }
            Camera.Size cs = VidSizes.get(closest(1080, list));
            Log.i("Width x Height", cs.width+"x"+cs.height);
            mediaRecorder.setVideoSize(cs.width,cs.height);
        }

        mediaRecorder.setOutputFile(defaultVideo);
        mediaRecorder.prepare();
        isRecording = true;
        mediaRecorder.start();
    }

    //停止拍摄
    public void stopRecording() {
        if (isRecording) {
            try {
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);

                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                playVideo();
            } catch (RuntimeException stopException) {
                Log.i("Stop Recoding", "Too short video");
                takePicture();
            }
            camera.lock();
        } else {
            Log.i("Stop Recoding", "isRecording is true");
        }
    }

    public void playVideo() {
        videoView.setVisibility(View.VISIBLE);
        editMedia.setVisibility(View.VISIBLE);
        captureMedia.setVisibility(View.GONE);

        Uri video = Uri.parse(defaultVideo);
        videoView.setVideoURI(video);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();
        preview.setVisibility(View.INVISIBLE);
        //setStickerView(1);
    }

    public void saveMedia(View v) throws IOException {
        if (!videoView.isShown()) {
            Toast.makeText(this, "Saving...",Toast.LENGTH_SHORT).show();
            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard.getAbsolutePath() + "/Opendp");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("ddMMyyHHmm").format(new Date());
            String ImageFile = "opendp-" + timeStamp + ".jpg"; //".png";
            File file = new File(dir, ImageFile);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                //stickerView.createBitmap().compress(Bitmap.CompressFormat.PNG, 90, fos);
                refreshGallery(file);
                Toast.makeText(this, "Saved!",Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Error saving!",Toast.LENGTH_LONG).show();
                Log.d("", "File not found: " + e.getMessage());
            }
        } else {
            if (defaultVideo != null) {
                String timeStamp = new SimpleDateFormat("ddMMyyHHmm").format(new Date());
                String VideoFile = "opendp-" + timeStamp + ".mp4";

                File from = new File(defaultVideo);
                File to = new File(dir,VideoFile);

                InputStream in = new FileInputStream(from);
                OutputStream out = new FileOutputStream(to);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                refreshGallery(to);
                Toast.makeText(this, "Saved!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error saving!",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    //闪光灯控制
    public void FlashControl(View v) {
        Log.i("Flash", "Flash button clicked!");
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            AlertDialog alert = new AlertDialog.Builder(CameraPreview.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
//            alert.setButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            });
            alert.show();
            return;
        } else {

            if (!isFlashOn) {
                isFlashOn = true;
                flashButton.setImageResource(R.drawable.ic_flash_on);
                Log.i("Flash", "Flash On");

            } else {
                isFlashOn = false;
                flashButton.setImageResource(R.drawable.ic_flash_off);
                Log.i("Flash", "Flash Off");
            }
        }
    }

    //图片放大缩小控制
    public void pictureControl(View v) {
        Log.i("picture", "picture button clicked!");
        if(!isPictureEnlarge) {
            isPictureEnlarge = true;
            pictureButton.setImageResource(R.drawable.ic_shrink_picture);


            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth,scaleHeight);

            Bitmap newBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
            imageView.setImageBitmap(newBitmap);
            num = false;


            Log.i("picture","picture enlarged");
        } else {
            isPictureEnlarge = false;
            pictureButton.setImageResource(R.drawable.ic_enlarge_picture);

            Matrix matrix = new Matrix();
            matrix.postScale(1.0f,1.0f);
            Bitmap newBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
            imageView.setImageBitmap(newBitmap);
            num = true;


            Log.i("picture","picture shrinked");
        }
    }

    public void switchCamera() {
        if (!isRecording) {
            if (camera.getNumberOfCameras() != 1) {
                camera.release();
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);
                try {
                    camera.setPreviewDisplay(previewHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startPreview();
            }
        } else {
            Log.i("Switch Camera","isRecording true");
        }
    }


    //弹出Timer对话框
    public void showTimer() {
        View view = LayoutInflater.from(this).inflate(R.layout.timer_dialog,null,false);

        AlertDialog.Builder guideDialog = new AlertDialog.Builder(CameraPreview.this,R.style.MyDialog);
        guideDialog.setView(view);//加载进去
        final AlertDialog dialog = guideDialog.create();


        Button closeTimer = (Button) view.findViewById(R.id.close_timer);
        Button confirmTimer = (Button) view.findViewById(R.id.confirm_timer);


        //显示
       dialog.show();
        //自定义的东西

        Window dialogWindow = dialog.getWindow();
// 把 DecorView 的默认 padding 取消，同时 DecorView 的默认大小也会取消
//        dialogwindow.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
// 设置宽度
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(layoutParams);
// 给 DecorView 设置背景颜色，很重要，不然导致 Dialog 内容显示不全，有一部分内容会充当 padding，上面例子有举出
//        dialogwindow.getDecorView().setBackgroundColor(0xcc232339);
        dialogWindow.setWindowAnimations(R.style.dialog_animation);

//        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


 //       Window dialogWindow = dialog.getWindow();
 //       WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);
//
//        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
//
//        dialogWindow.setWindowAnimations(R.style.dialog_animation);

//        WindowManager m = getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        //WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        //lp.gravity = Gravity.BOTTOM;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度设置为屏幕的0.6
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度设置为屏幕的0.95
        //dialogWindow.setAttributes(p);

        //getWindow().getDecorView().setPadding(0, 0, 0, 0);

//        lp.x = 100; // 新位置X坐标
//        lp.y = 100; // 新位置Y坐标
        //lp.width = ScreenUtils.getScreenWidth(CameraPreview.this); // 宽度
        //lp.height = 180; // 高度
//        lp.alpha = 0.7f; // 透明度

//        dialogWindow.setAttributes(lp);

        closeTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });







        //dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/32*9),LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    //弹出Guide对话框
    public void showGuide() {
        View view = LayoutInflater.from(this).inflate(R.layout.guide_dialog,null,false);

        AlertDialog.Builder guideDialog = new AlertDialog.Builder(CameraPreview.this,R.style.MyDialog1);
        guideDialog.setView(view);//加载进去
        final AlertDialog dialog = guideDialog.create();

        VideoView video_view = (VideoView) view.findViewById(R.id.video_guide);
        ImageView cancle_video = (ImageView) view.findViewById(R.id.cancel_video);

        video_view.setMediaController(mediaController);
        //下面android.resource://是固定的，com.example.work是包名，R.raw.sw是你raw文件夹下的视频文件
        video_view.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.deng_ziqi));
        dialog.show();
        video_view.start();
        //自定义的东西
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);

        cancle_video.setVisibility(View.VISIBLE);
        cancle_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void EditCaptureSwitch() {
        preview.setVisibility(View.VISIBLE);
        captureMedia.setVisibility(View.VISIBLE);
        //capturedImage.setImageResource(android.R.color.transparent);
        startPreview(); //onResume();
        capturedImage.setVisibility(View.GONE);
        editMedia.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
    }
    /*
    @Override
    public void onBackPressed() {
        if (selectSticker.getVisibility() == View.VISIBLE) {
            stickerOptions();
        } else if(editTextBody.getVisibility() == View.VISIBLE) {
            showHideEditText();
        } else if (editMedia.getVisibility() == View.VISIBLE) {
            EditCaptureSwitch();
            removeAllStickers();
        } else {
            finish();
        }
    }
    */
    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        camera=Camera.open(currentCameraId);
        try {
            camera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startPreview();
        //FocusCamera();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera=null;
        inPreview=false;

        super.onPause();
    }

    //设置Camera.size用到
    public int closest(int of, List<Integer> in) {
        int min = Integer.MAX_VALUE;
        int closest = of;
        int position=0;
        int i = 0;

        for (int v: in) {
            final int diff = Math.abs(v - of);
            i++;

            if(diff < min) {
                min = diff;
                closest = v;
                position = i;
            }
        }
        int rePos = position - 1;
        Log.i("Value",closest+"-"+rePos);
        return rePos;
    }

    private void initPreview() {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.stopPreview();
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("Preview:surfaceCallback", "Exception in setPreviewDisplay()", t);
                Toast.makeText(CameraPreview.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (!cameraConfigured) {

                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

                List<Integer> list = new ArrayList<Integer>();
                for (int i=0; i < sizes.size(); i++) {
                    Log.i("ASDF", "Supported Preview: " + sizes.get(i).width + "x" + sizes.get(i).height);
                    list.add(sizes.get(i).width);
                }
                Camera.Size cs = sizes.get(closest(1920, list));

                Log.i("Width x Height", cs.width+"x"+cs.height);

                parameters.setPreviewSize(cs.width, cs.height);
                camera.setParameters(parameters);
                cameraConfigured=true;
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
            camera.setDisplayOrientation(setCameraDisplayOrientation(this, currentCameraId, camera));
            camera.startPreview();
            inPreview=true;
        }
    }

    //设置自动旋转
    private int setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview();
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

}

