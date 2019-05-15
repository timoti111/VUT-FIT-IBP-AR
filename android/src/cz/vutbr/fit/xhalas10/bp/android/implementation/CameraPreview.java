/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.android.implementation;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.ICameraPreview;

/**
 * This class serves for setting camera preview and than it can render it.
 * It also implements ICameraPreview interface from module core and package
 * cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces
 */
public class CameraPreview implements SurfaceTexture.OnFrameAvailableListener, ICameraPreview {
    static final int FPS = 60; // If you can't see preview try to compile with 30 FPS.

    private SurfaceTexture surfaceTexture;
    private boolean updateSurfaceTexture = false;

    private String cameraID;
    private float[] focalLengths;
    private float[] sensorSize;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size previewSize = new Size(1920, 1080);
    private Range<Integer> fpsRange = Range.create(FPS, FPS);
    private int[] texture = new int[1];

    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private CameraManager cameraManager;

    private HandlerThread handlerThread;
    private Handler handler;
    private final CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            CameraPreview.this.cameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            CameraPreview.this.cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            CameraPreview.this.cameraDevice = null;
        }
    };
    private ShaderProgram shader;
    private Mesh mesh;

    public CameraPreview(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public float[] getCameraSensorSize() {
        return sensorSize;
    }

    public float getCameraFocalLength() {
        return focalLengths[0];
    }

    private void initTexture() {
        texture[0] = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateSurfaceTexture = true;
        Gdx.graphics.requestRendering();
    }

    private void getCameraInfo(final int width, final int height) {
        try {
            assert cameraManager != null;
            for (String cameraID : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                //noinspection ConstantConditions
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    this.cameraID = cameraID;
                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF sensorSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    this.sensorSize = new float[2];
                    this.sensorSize[0] = Objects.requireNonNull(sensorSize).getWidth();
                    this.sensorSize[1] = sensorSize.getHeight();

                    assert map != null;
                    for (Size psize : map.getOutputSizes(SurfaceTexture.class)) {
                        if (width == psize.getWidth() && height == psize.getHeight()) {
                            previewSize = psize;
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Log.e("mr", "getCameraInfo - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Log.e("mr", "getCameraInfo - Illegal Argument Exception");
        } catch (SecurityException e) {
            Log.e("mr", "getCameraInfo - Security Exception");
        }
    }

    private void openCamera() {
        try {
            assert cameraManager != null;

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraManager.openCamera(cameraID, cameraDeviceStateCallback, handler);
        } catch (CameraAccessException e) {
            Log.e("mr", "OpenCamera - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Log.e("mr", "OpenCamera - Illegal Argument Exception");
        } catch (SecurityException e) {
            Log.e("mr", "OpenCamera - Security Exception");
        } catch (InterruptedException e) {
            Log.e("mr", "OpenCamera - Interrupted Exception");
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != cameraCaptureSession) {
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void createCameraPreviewSession() {
        try {
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface surface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == cameraDevice)
                                return;

                            CameraPreview.this.cameraCaptureSession = cameraCaptureSession;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                                captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);

                                CameraPreview.this.cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                            } catch (CameraAccessException e) {
                                Log.e("mr", "createCaptureSession");
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            Log.e("mr", "createCameraPreviewSession");
        }
    }

    public void onResume() {
        startBackgroundThread();
        if (captureRequestBuilder != null)
            openCamera();
    }

    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("CameraBackground");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            Log.e("mr", "stopBackgroundThread");
        }
    }

    @Override
    public void init() {
        shader = new ShaderProgram(Gdx.files.internal("cameraVertex.glsl").readString(), Gdx.files.internal("cameraFragment.glsl").readString());
        mesh = new Mesh(true, 4, 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));
        float[] vertices = {
                -1.0f, 1.0f,   // Position 0
                0.0f, 0.0f,   // TexCoord 0
                -1.0f, -1.0f,  // Position 1
                0.0f, 1.0f,   // TexCoord 1
                1.0f, -1.0f,  // Position 2
                1.0f, 1.0f,   // TexCoord 2
                1.0f, 1.0f,   // Position 3
                1.0f, 0.0f    // TexCoord 3
        };

        short[] indices = {0, 1, 2, 0, 2, 3};
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        mesh.transform(new Matrix4().idt().scale(1.0f, Gdx.graphics.getWidth() / 1920.0f, 1.0f));
        initTexture();
        surfaceTexture = new SurfaceTexture(texture[0]);
        surfaceTexture.setOnFrameAvailableListener(this);

        getCameraInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        openCamera();
    }

    @Override
    public void renderBackground() {
        synchronized (this) {
            if (updateSurfaceTexture) {
                surfaceTexture.updateTexImage();
                updateSurfaceTexture = false;
            }
        }
        Gdx.gl.glActiveTexture(Gdx.gl.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        shader.begin();
        shader.setUniformi("diffuseTex", 0);
        mesh.render(shader, Gdx.gl.GL_TRIANGLES);
        shader.end();
    }

    @Override
    public void dispose() {
        closeCamera();
        shader.dispose();
        mesh.dispose();
        Gdx.gl.glDeleteTexture(texture[0]);
    }
}