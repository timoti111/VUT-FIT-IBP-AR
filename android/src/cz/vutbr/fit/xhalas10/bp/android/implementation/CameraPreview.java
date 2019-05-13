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
import android.support.annotation.NonNull;

import cz.vutbr.fit.xhalas10.bp.multiplatform.interfaces.ICameraPreview;

public class CameraPreview implements SurfaceTexture.OnFrameAvailableListener, ICameraPreview {
    static final int FPS = 60;

    private SurfaceTexture mSTexture;
    private boolean mUpdateST = false;

    private String mCameraID;
    private float[] mFocalLengths;
    private float[] sensorSize;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private Size mPreviewSize = new Size(1920, 1080);
    private Range<Integer> fpsRange = Range.create(FPS, FPS);
    private int[] texture = new int[1];

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraManager cameraManager;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
    private ShaderProgram shader; //Our shader
    private Mesh mesh; //Our mesh that we will draw the texture on

    public CameraPreview(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public float[] getCameraSensorSize() {
        return sensorSize;
    }

    public float getCameraFocalLength() {
        return mFocalLengths[0];
    }

    private void initTex() {
        texture[0] = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
        Gdx.gl.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mUpdateST = true;
        Gdx.graphics.requestRendering();
    }

    private void getCameraInfo(final int width, final int height) {
        try {
            assert cameraManager != null;
            for (String cameraID : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                //noinspection ConstantConditions
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraID = cameraID;
                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mFocalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF mSensorSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    sensorSize = new float[2];
                    sensorSize[0] = Objects.requireNonNull(mSensorSize).getWidth();
                    sensorSize[1] = mSensorSize.getHeight();

                    assert map != null;
                    for (Size psize : map.getOutputSizes(SurfaceTexture.class)) {
                        if (width == psize.getWidth() && height == psize.getHeight()) {
                            mPreviewSize = psize;
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

            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraManager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
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
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void createCameraPreviewSession() {
        try {
            mSTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface surface = new Surface(mSTexture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice)
                                return;

                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                                mPreviewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);

                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
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
        if (mPreviewRequestBuilder != null)
            openCamera();
    }

    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
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
        //The indices come in trios of vertex indices that describe the triangles of our mesh
        short[] indices = {0, 1, 2, 0, 2, 3};
        //Set vertices and indices to our mesh
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        mesh.transform(new Matrix4().idt().scale(1.0f, Gdx.graphics.getWidth() / 1920.0f, 1.0f));
        initTex();
        mSTexture = new SurfaceTexture(texture[0]);
        mSTexture.setOnFrameAvailableListener(this);

        getCameraInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        openCamera();
    }

    @Override
    public void renderBackground() {
        synchronized (this) {
            if (mUpdateST) {
                mSTexture.updateTexImage();
                mUpdateST = false;
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