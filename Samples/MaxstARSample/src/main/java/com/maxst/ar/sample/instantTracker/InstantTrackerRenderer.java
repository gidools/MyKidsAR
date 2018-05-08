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

	private TexturedCube texturedCube;
	private float posX;
	private float posY;
	private Activity activity;
	private VideoQuad videoQuad2;

	private BackgroundCameraQuad backgroundCameraQuad;

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
			if (videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_PLAYING) {
				videoQuad2.getVideoPlayer().pause();
			}
			return;
		}

		float [] projectionMatrix = CameraDevice.getInstance().getProjectionMatrix();

		Trackable trackable = trackingResult.getTrackable(0);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		if (videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_READY ||
				videoQuad2.getVideoPlayer().getState() == VideoPlayer.STATE_PAUSE) {
			videoQuad2.getVideoPlayer().start();
		}
		videoQuad2.setProjectionMatrix(projectionMatrix);
		videoQuad2.setTransform(trackable.getPoseMatrix());
		videoQuad2.setTranslate(posX, posY, 0.0f);
		videoQuad2.setScale(0.5f, -0.5f, 1.0f);
		videoQuad2.draw();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {

		surfaceWidth = width;
		surfaceHeight = height;

		texturedCube.setScale(0.3f, 0.3f, 0.1f);

		MaxstAR.onSurfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		backgroundCameraQuad = new BackgroundCameraQuad();

		texturedCube = new TexturedCube();
		Bitmap bitmap = MaxstARUtil.getBitmapFromAsset("MaxstAR_Cube.png", activity.getAssets());
		texturedCube.setTextureBitmap(bitmap);

		videoQuad2 = new VideoQuad();
		VideoPlayer player = new VideoPlayer(activity);
		videoQuad2.setVideoPlayer(player);
		player.openVideo(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/kakaotalk_1525765685379.mp4");
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
		videoQuad2.getVideoPlayer().destroy();
	}
}
