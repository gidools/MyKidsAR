/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */
package com.maxst.ar.sample.imageTracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;

import com.maxst.ar.CameraDevice;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.MaxstARUtil;
import com.maxst.ar.Trackable;
import com.maxst.ar.TrackedImage;
import com.maxst.ar.TrackerManager;
import com.maxst.ar.TrackingResult;
import com.maxst.ar.TrackingState;
import com.maxst.ar.sample.arobject.BackgroundCameraQuad;
import com.maxst.ar.sample.arobject.ChromaKeyVideoQuad;
import com.maxst.ar.sample.arobject.ColoredCube;
import com.maxst.ar.sample.arobject.TexturedCube;
import com.maxst.ar.sample.arobject.VideoQuad;
import com.maxst.videoplayer.VideoPlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class ImageTrackerRenderer implements Renderer {

	public static final String TAG = ImageTrackerRenderer.class.getSimpleName();

	private TexturedCube texturedCube;
	private ColoredCube coloredCube;
	private VideoQuad videoQuad;
	private VideoQuad videoQuad2;

	private int surfaceWidth;
	private int surfaceHeight;
	private BackgroundCameraQuad backgroundCameraQuad;

	private final Activity activity;

	ImageTrackerRenderer(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		backgroundCameraQuad = new BackgroundCameraQuad();

		videoQuad = new VideoQuad();
		VideoPlayer player = new VideoPlayer(activity);
		videoQuad.setVideoPlayer(player);
		player.openVideo(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/20180422_154452.mp4");

		videoQuad2 = new VideoQuad();
		player = new VideoPlayer(activity);
		videoQuad2.setVideoPlayer(player);
		player.openVideo(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/kakaotalk_1525765685379.mp4");
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		surfaceWidth = width;
		surfaceHeight = height;

		MaxstAR.onSurfaceChanged(width, height);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

		TrackingState state = TrackerManager.getInstance().updateTrackingState();
		TrackingResult trackingResult = state.getTrackingResult();

		TrackedImage image = state.getImage();
		float[] cameraProjectionMatrix = CameraDevice.getInstance().getBackgroundPlaneProjectionMatrix();
		backgroundCameraQuad.setProjectionMatrix(cameraProjectionMatrix);
		backgroundCameraQuad.draw(image);

		boolean daheeDetected = false;
		boolean kidsDetected = false;

		float[] projectionMatrix = CameraDevice.getInstance().getProjectionMatrix();

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		for (int i = 0; i < trackingResult.getCount(); i++) {
			Trackable trackable = trackingResult.getTrackable(i);
			if (trackable.getName().equals("dahee")) {
				daheeDetected = true;
				if (videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_READY ||
						videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_PAUSE) {
					videoQuad.getVideoPlayer().start();
				}
				videoQuad.setProjectionMatrix(projectionMatrix);
				videoQuad.setTransform(trackable.getPoseMatrix());
				videoQuad.setTranslate(0.0f, 0.0f, 0.0f);
				videoQuad.setScale(1, -2, 1.0f);
				videoQuad.draw();
			} else if (trackable.getName().equals("kids")) {
				kidsDetected = true;
				if (videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_READY ||
						videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_PAUSE) {
					videoQuad2.getVideoPlayer().start();
				}
				videoQuad2.setProjectionMatrix(projectionMatrix);
				videoQuad2.setTransform(trackable.getPoseMatrix());
				videoQuad2.setTranslate(0.0f, 0.0f, 0.0f);
				videoQuad2.setScale(1, -1, 1.0f);
				videoQuad2.draw();
			}
		}

		if (!daheeDetected) {
			if (videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_PLAYING) {
				videoQuad.getVideoPlayer().pause();
			}
		}

		if (!kidsDetected) {
			if (videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_PLAYING) {
				videoQuad2.getVideoPlayer().pause();
			}
		}
	}

	void destroyVideoPlayer() {
		videoQuad.getVideoPlayer().destroy();
		videoQuad2.getVideoPlayer().destroy();
	}
}
