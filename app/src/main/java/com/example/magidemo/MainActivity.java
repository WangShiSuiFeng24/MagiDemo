package com.example.magidemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;


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


public class MainActivity extends AppCompatActivity {
//public class MainActivity extends Activity {

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

    private RelativeLayout captureMedia;
    private FrameLayout editMedia;

    private ImageView recordVideo;
    private ImageView stopRecord;

    private ImageView switchCameraBtn;
    private ImageView flashButton;

    private Button pictureButton;

    private ImageView EditCaptureSwitchBtn;

    private VideoView videoView;
    int VideoSeconds = 1;

    private ImageView guide;
    private ImageView timer;

    public File dir;
    public String defaultVideo;

    private MediaController mediaController;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator currentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int shortAnimationDuration;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;



    private static final int EXPAND_IMAGE = 1;
    private static final int SHRINK_IMAGE = 2;

    private static final int SHOW_GUIDE = 3;

    private Handler handler = new Handler() {
        public  void  handleMessage(Message msg) {
            switch (msg.what) {
                case EXPAND_IMAGE:
                    expandImageView();
                    break;
                case SHRINK_IMAGE:
                    shrinkImageView();
                    break;
                case SHOW_GUIDE:
//                    showGuide();
                    showGuide();
                    break;
                default :
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        GetPermission();

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        captureMedia = (RelativeLayout) findViewById(R.id.camera_view);
        editMedia = (FrameLayout) findViewById(R.id.edit_media);

        recordVideo = (ImageView) findViewById(R.id.record_video);
        stopRecord = (ImageView) findViewById(R.id.stop_record);

        switchCameraBtn = (ImageView) findViewById(R.id.img_switch_camera);
        flashButton = (ImageView) findViewById(R.id.img_flash_control);

        pictureButton = (Button) findViewById(R.id.picture_button);


        isRecording = false;
        isFlashOn = false;

        isPictureEnlarge = false;

        EditCaptureSwitchBtn = (ImageView) findViewById(R.id.cancel_capture);

        videoView = (VideoView) findViewById(R.id.captured_video);

        preview  = (SurfaceView)findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        guide = (ImageView)findViewById(R.id.img_guide);
        timer = (ImageView)findViewById(R.id.img_timer);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Do the file write

            //setting dir and VideoFile value
            File sdCard = Environment.getExternalStorageDirectory();
//        dir = new File(sdCard.getAbsolutePath() + "/Opendp");
            dir = new File(sdCard.getAbsolutePath() + "/MagiDemo2");

            if (!dir.exists()) {
                dir.mkdirs();
            }
            defaultVideo =  dir + "/defaultVideo.mp4.ming";
            File createDefault = new File(defaultVideo);
            Log.i("defaultVideo path:",defaultVideo);

            if (!createDefault.isFile()) {
                try {
                    FileWriter writeDefault = new FileWriter(createDefault);
                    writeDefault.append("yy");
                    writeDefault.close();
                    writeDefault.flush();
                } catch (Exception ex) {
                }
            }

        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }



//        //setting dir and VideoFile value
//        File sdCard = Environment.getExternalStorageDirectory();
////        dir = new File(sdCard.getAbsolutePath() + "/Opendp");
//        dir = new File(sdCard.getAbsolutePath() + "/MagiDemo2");
//
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        defaultVideo =  dir + "/defaultVideo.mp4.ming";
//        File createDefault = new File(defaultVideo);
//        Log.i("defaultVideo path:",defaultVideo);
//
//        if (!createDefault.isFile()) {
//            try {
//                FileWriter writeDefault = new FileWriter(createDefault);
//                writeDefault.append("yy");
//                writeDefault.close();
//                writeDefault.flush();
//            } catch (Exception ex) {
//            }
//        }

        //开始录像
        recordVideo.setOnClickListener(new View.OnClickListener() {

            private Timer timer = new Timer();
            private long LONG_PRESS_TIMEOUT = 1000;

            @Override
            public void onClick(View v) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
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
                recordVideo.setVisibility(View.GONE);
                stopRecord.setVisibility(View.VISIBLE);

            }
        });

        //停止录像
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                VideoCountDown.cancel();
                VideoSeconds = 1;

                stopRecord.setVisibility(View.GONE);
                recordVideo.setVisibility(View.VISIBLE);
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
                new Thread (new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = SHOW_GUIDE;
                        handler.sendMessage(message);
                    }
                }).start();
//                showGuide();
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
    }//onCreate()

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    //获取权限
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

    //是否拥有有权限
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
//            int VideoSecondsPercentage = VideoSeconds * 10;
//            customButton.setProgressWithAnimation(VideoSecondsPercentage);
        }

        @Override
        public void onFinish() {
            stopRecording();
//            customButton.setProgress(0);
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
//                takePicture();
            }
            camera.lock();
        } else {
            Log.i("Stop Recoding", "isRecording is true");
        }
    }

    //循环播放拍摄视频
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
    }

    //保存视频
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
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");

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

    //图片动画放大缩小控制
    public void pictureControl(View v) {

        Log.i("picture", "picture button clicked!");
        if(!isPictureEnlarge) {
            isPictureEnlarge = true;


            new Thread (new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = EXPAND_IMAGE;
                    handler.sendMessage(message);
                }
            }).start();

            pictureButton.setBackgroundResource(R.drawable.ic_shrink_picture);
            pictureButton.bringToFront();

            Log.i("picture","picture enlarged");
        } else {
            isPictureEnlarge = false;

            new Thread (new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = SHRINK_IMAGE;
                    handler.sendMessage(message);
                }
            }).start();

            pictureButton.setBackgroundResource(R.drawable.ic_enlarge_picture);
            pictureButton.bringToFront();

            Log.i("picture","picture shrinked");
        }
    }

    //动画放大
    public void expandImageView() {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        final View thumb1View = findViewById(R.id.thumb_button_1);

        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);


        // Load the high-resolution "zoomed-in" image.
//            final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        expandedImageView.setImageResource(R.drawable.picture_demo);
        expandedImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        findViewById(R.id.thumb_button_1).getGlobalVisibleRect(startBounds);
        Log.i("thumb startBounds",startBounds.toString());


        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        Log.i("container finalBounds",finalBounds.toString());
        Log.i("container globalOffset",globalOffset.toString());
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        Log.i("offset startBounds",startBounds.toString());
        Log.i("offset finalbounds",finalBounds.toString());

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            Log.i("startScale","Extend start bounds horizontally");

            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            Log.i("startScale","Extend start bounds vertically");
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumb1View.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, "alpha", 1f, 0.3f));
        set.setDuration(shortAnimationDuration);
//        set.setDuration(3000);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
    }

    //动画缩小
    public void shrinkImageView() {
        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
//        final float startScaleFinal = startScale;

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        final View thumb1View = findViewById(R.id.thumb_button_1);

        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);

        expandedImageView.setImageResource(R.drawable.picture_demo);
        expandedImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        findViewById(R.id.thumb_button_1).getGlobalVisibleRect(startBounds);

        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
//        Log.i("container finalBounds",finalBounds.toString());
//        Log.i("container globalOffset",globalOffset.toString());
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
//        Log.i("offset startBounds",startBounds.toString());
//        Log.i("offset finalbounds",finalBounds.toString());

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
            Log.i("horizontal startBounds",startBounds.toString());

        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;

            Log.i("vertical startBounds",startBounds.toString());
        }


        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumb1View.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);


        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, startScale))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, startScale))
                .with(ObjectAnimator.ofFloat(expandedImageView, "alpha", 0.3f, 1f));;
        set.setDuration(shortAnimationDuration);
//        set.setDuration(10000);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumb1View.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumb1View.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
    }

    //转换摄像头
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

        AlertDialog.Builder guideDialog = new AlertDialog.Builder(MainActivity.this,R.style.MyDialog);
        guideDialog.setView(view);//加载进去
        final AlertDialog dialog = guideDialog.create();


        Button closeTimer = (Button) view.findViewById(R.id.close_timer);
        Button confirmTimer = (Button) view.findViewById(R.id.confirm_timer);

        final Button button1 = (Button) view.findViewById(R.id.Button1);
        Button button2 = (Button) view.findViewById(R.id.Button2);
        Button button3 = (Button) view.findViewById(R.id.Button3);
        Button button4 = (Button) view.findViewById(R.id.Button4);
        Button button5 = (Button) view.findViewById(R.id.Button5);


        //显示
        dialog.show();
        //自定义的东

        Window dialogWindow = dialog.getWindow();
// 把 DecorView 的默认 padding 取消，同时 DecorView 的默认大小也会取消
//        dialogwindow.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
// 设置宽度
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(layoutParams);

        dialogWindow.setWindowAnimations(R.style.dialog_animation);

        dialogWindow.setGravity(Gravity.BOTTOM);

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

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.requestFocus();
                v.requestFocusFromTouch();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.requestFocus();
                v.requestFocusFromTouch();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.requestFocus();
                v.requestFocusFromTouch();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.requestFocus();
                v.requestFocusFromTouch();
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.requestFocus();
                v.requestFocusFromTouch();
            }
        });
    }

    //弹出Guide对话框
    public void showGuide() {
        View view = LayoutInflater.from(this).inflate(R.layout.guide_dialog,null,false);

        //找到对应控件实例
        VideoView video_view = (VideoView) view.findViewById(R.id.video_guide);
        ImageView cancle_video = (ImageView) view.findViewById(R.id.cancel_video);

        video_view.setMediaController(mediaController);
        //下面android.resource://是固定的，com.example.work是包名，R.raw.sw是你raw文件夹下的视频文件
        video_view.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.deng_ziqi));

        AlertDialog.Builder guideDialog = new AlertDialog.Builder(MainActivity.this);
        guideDialog.setView(view);//加载进去
        final AlertDialog dialog = guideDialog.create();
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_corners);
        dialog.show();

        video_view.start();
//        video_view.setBackgroundResource(R.drawable.bg_corners);


        //设置后dialog不再跳动
        Window window = dialog.getWindow() ;
//        window.setBackgroundDrawableResource(R.drawable.bg_corners);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.5); // 改变的是dialog框在屏幕中的位置而不是大小
        p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
        window.setAttributes(p);



        cancle_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //编辑 捕捉 开关
    public void EditCaptureSwitch() {
        preview.setVisibility(View.VISIBLE);
        captureMedia.setVisibility(View.VISIBLE);
        startPreview(); //onResume();
        editMedia.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
    }

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
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
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

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
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
