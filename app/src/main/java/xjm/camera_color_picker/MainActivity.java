package xjm.camera_color_picker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements CameraColorPickerPreview.OnColorSelectedListener {

    private Camera mCamera;
    private CameraAsyncTask mCameraAsyncTask;
    private int mSelectedColor;
    private CameraColorPickerPreview mCameraPreview;
    private FrameLayout mPreviewContainer;
    private View mPointerRing;
    private static final int REQUEST_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreviewContainer = findViewById(R.id.camera_container);
        mPointerRing = findViewById(R.id.pointer_ring);
    }

    @Override
    public void onColorSelected(int color) {
        mSelectedColor = color;
        mPointerRing.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            } else {
                mCameraAsyncTask = new CameraAsyncTask();
                mCameraAsyncTask.execute();
            }
        } else {
            mCameraAsyncTask = new CameraAsyncTask();
            mCameraAsyncTask.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//同意权限
//                mCameraAsyncTask = new CameraAsyncTask();
//                mCameraAsyncTask.execute();   //会和onResume重复调用
            } else {
                //不同意权限那就去死吧
                MainActivity.this.finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraAsyncTask != null) {
            mCameraAsyncTask.cancel(true);
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if (mCameraPreview != null) {
            mPreviewContainer.removeView(mCameraPreview);
        }
    }

    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        protected FrameLayout.LayoutParams mPreviewParams;

        @Override
        protected Camera doInBackground(Void... params) {
            Camera camera = getCameraInstance();
            if (camera != null) {
                Camera.Parameters cameraParameters = camera.getParameters();
                Camera.Size bestSize = Cameras.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes()
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , true);
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);
                Cameras.setCameraDisplayOrientation(MainActivity.this, camera);
                int[] adaptedDimension = Cameras.getProportionalDimension(
                        bestSize
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , true);
                mPreviewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                mPreviewParams.gravity = Gravity.CENTER;
            }
            return camera;
        }

        @Override
        protected void onPostExecute(Camera camera) {
            super.onPostExecute(camera);
            if (!isCancelled()) {
                mCamera = camera;
                if (mCamera != null) {
                    mCameraPreview = new CameraColorPickerPreview(MainActivity.this, mCamera);
                    mCameraPreview.setOnColorSelectedListener(MainActivity.this);
                    mPreviewContainer.addView(mCameraPreview, 0, mPreviewParams);
                }
            }
        }

        @Override
        protected void onCancelled(Camera camera) {
            super.onCancelled(camera);
            if (camera != null) {
                camera.release();
            }
        }
    }
}
