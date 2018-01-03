package xjm.camera_color_picker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

public final class Cameras {
    private static final double ASPECT_TOLERANCE = 0.15;
    public static Camera.Size getBestPreviewSize(List<Camera.Size> sizes, int layoutWidth, int layoutHeight, boolean isPortrait) {
        if (isPortrait) {
            layoutHeight = layoutHeight + layoutWidth;
            layoutWidth = layoutHeight - layoutWidth;
            layoutHeight = layoutHeight - layoutWidth;
        }
        double targetRatio = (double) layoutWidth / layoutHeight;
        Camera.Size optimalSize = null;
        double optimalArea = 0;
        for (Camera.Size candidateSize : sizes) {
            double candidateRatio = (double) candidateSize.width / candidateSize.height;
            double candidateArea = candidateSize.width * candidateSize.height;
            double ratioDifference = Math.abs(candidateRatio - targetRatio);
            if (ratioDifference < ASPECT_TOLERANCE && candidateArea > optimalArea) {
                optimalSize = candidateSize;
                optimalArea = candidateArea;
            }
        }
        if (optimalSize == null) {
            optimalArea = 0;
            for (Camera.Size candidateSize : sizes) {
                double candidateArea = candidateSize.width * candidateSize.height;
                if (candidateArea > optimalArea) {
                    optimalSize = candidateSize;
                    optimalArea = candidateArea;
                }
            }
        }
        return optimalSize;
    }

    public static int[] getProportionalDimension(Camera.Size size, int targetW, int targetH, boolean isPortrait) {
        int[] adaptedDimension = new int[2];
        double previewRatio;
        if (isPortrait) {
            previewRatio = (double) size.height / size.width;
        } else {
            previewRatio = (double) size.width / size.height;
        }
        if (((double) targetW / targetH) > previewRatio) {
            adaptedDimension[0] = targetW;
            adaptedDimension[1] = (int) (adaptedDimension[0] / previewRatio);
        } else {
            adaptedDimension[1] = targetH;
            adaptedDimension[0] = (int) (adaptedDimension[1] * previewRatio);
        }
        return adaptedDimension;
    }

    public static void setCameraDisplayOrientation(Context context, Camera camera) {
        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int degrees = 0;
        final int currentRotation = ((WindowManager) context.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (currentRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayOrientation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (info.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayOrientation);
    }

    private Cameras() {
    }
}
