/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */

package com.maxst.ar.sample.instantTracker;

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
import com.maxst.ar.sample.arobject.TexturedCube;
import com.maxst.ar.sample.arobject.VideoQuad;
import com.maxst.videoplayer.VideoPlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class InstantTrackerRenderer implements Renderer {

	public static final String TAG = InstantTrackerRenderer.class.getSimpleName();

	private int surfaceWidth;
	private int surfaceHeight;

	private float posX;
	private float posY;
	private Activity activity;
	private VideoQuad videoQuad;

	private BackgroundCameraQuad backgroundCameraQuad;
	private static final float SCALE_FACTOR = 1.5f;

	InstantTrackerRenderer(Activity activity) {
		this.activity = activity;
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

		if (trackingResult.getCount() == 0) {
			if (videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_PLAYING) {
				videoQuad.getVideoPlayer().pause();
			}
			return;
		}

		float [] projectionMatrix = CameraDevice.getInstance().getProjectionMatrix();

		Trackable trackable = trackingResult.getTrackable(0);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		if (videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_READY ||
				videoQuad.getVideoPlayer().getState() == VideoPlayer.STATE_PAUSE) {
			videoQuad.getVideoPlayer().start();
		}
		videoQuad.setProjectionMatrix(projectionMatrix);
		videoQuad.setTransform(trackable.getPoseMatrix());
		videoQuad.setTranslate(posX, posY, -0.14f * SCALE_FACTOR);
		videoQuad.setRotation(90, 1, 0, 0);
		videoQuad.setScale(0.5f * SCALE_FACTOR, -0.28f * SCALE_FACTOR, 1.0f);
		videoQuad.draw();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {

		surfaceWidth = width;
		surfaceHeight = height;

		MaxstAR.onSurfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		backgroundCameraQuad = new BackgroundCameraQuad();

		videoQuad = new VideoQuad();
		VideoPlayer player = new VideoPlayer(activity);
		videoQuad.setVideoPlayer(player);
		player.openVideo("leean_dance.mp4");
	}

	void setTranslate(float x, float y) {
		posX += x;
		posY += y;
	}

	void resetPosition() {
		posX = 0;
		posY = 0;
	}

	void destroyVideoPlayer() {
		videoQuad.getVideoPlayer().destroy();
	}
}
