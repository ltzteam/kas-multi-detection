package com.megvii.kas.multi.camera;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megvii.bean.FaceInfo;
import com.megvii.bean.MGYUVImage;
import com.megvii.bean.Track;
import com.megvii.demo.R;
import com.megvii.facequality.DetectionManager;
import com.megvii.facequality.FaceQualityConfig;
import com.megvii.kas.multi.camera.hardware.CameraModule;
import com.megvii.kas.multi.util.FaceModel;
import com.megvii.kas.multi.util.Performance;
import com.megvii.kas.multi.view.MultiDetectView;

import java.util.List;

public class CameraFragment extends Fragment implements Camera.PreviewCallback, DetectionManager.DetectionListener {
    CameraModule camera;
    TextureView cameraArea;
    TextView tips, calc;
    MultiDetectView multiDetect;

    DetectionManager dm;
    private int nLoopDetect = 0;
    private int nLoopPreview = 0;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }
    public CameraFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        Log.w("FIL_MESSAGE", "CameraFragment onResume().");
        if(null == camera) {
            camera = new CameraModule();
            camera.open(getActivity());
            camera.setPreviewCallback(this);
        }

        if (null == dm) {
            FaceQualityConfig config = new FaceQualityConfig.Builder().setMinFaceSize(100).build();
            dm = new DetectionManager(config, 0);
            dm.init(getActivity(), FaceModel.read(getActivity()));
            dm.setDetectionListener(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.w("FIL_MESSAGE", "CameraFragment onPause().");
        if (null != dm) {
            dm.release();
//            dm.setDetectionListener(null);
            dm = null;
        }

        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.closeCamera();
            camera = null;
        }
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.megvii_kas_fragment_camera, container, false);
        cameraArea = (TextureView) view.findViewById(R.id.megvii_kas_camera);
        cameraArea.setSurfaceTextureListener(surfaceTextureListener);

        tips = (TextView) view.findViewById(R.id.megvii_kas_tips);
        calc = (TextView) view.findViewById(R.id.megvii_kas_calc);
        multiDetect = (MultiDetectView) view.findViewById(R.id.megvii_kas_multi_detect);

        return view;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        nLoopPreview ++;
        Performance.instance().update("preview", System.currentTimeMillis());
//        if( nLoopPreview ++ % 2 == 0) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        MGYUVImage image = new MGYUVImage(bytes,
                size.width,
                size.height,
                CameraModule.FACING.CAMERA_FRONT == CameraModule.facing ? 270 : 90);
        dm.postDetection(image);
//        }
    }

    @Override
    public void detected(List<FaceInfo> faces, List<Track> disappearTracks, List<Track> newTracks, List<Track> stillTrackingTracks) {
        nLoopDetect ++;
        tips.setText(String.format("[A:%d D:%d N:%d C:%d] r:%2d Detect: %5d Frame: %5d",
                        faces.size(),
                        disappearTracks.size(),
                        newTracks.size(),
                        stillTrackingTracks.size(),
                        nLoopPreview / nLoopDetect,
                        nLoopDetect,
                        nLoopPreview
                )
        );
        Performance.instance().update("algorithm", System.currentTimeMillis());

        multiDetect.updateFaceRect(faces);

        StringBuilder sb = new StringBuilder();
        for (FaceInfo info : faces) {
            sb.append(String.format("id: %d\n", info.faceID));
        }

        sb.append(String.format("preview: %9d ms\nalgorithm: %5d ms",
                Performance.instance().interval("preview"),
                Performance.instance().interval("algorithm")));

        calc.setText(sb.toString());
    }

    final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (null != camera) {
                camera.startPreview(surface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
}
