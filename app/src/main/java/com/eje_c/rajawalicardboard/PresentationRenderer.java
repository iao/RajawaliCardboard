package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PresentationRenderer extends RajawaliRenderer {

    //private ATexture texture;
    //private Object3D obj;

    private MediaPlayer mMediaPlayer;
    private StreamingTexture mVideoTexture;
    private Set<MainActivity.Hotspot> hotspots = Collections.synchronizedSet(new HashSet<MainActivity.Hotspot>());
    private Cube descriptionCube;
    private MyRenderer parentRenderer;
    private RenderTarget target;

    public PresentationRenderer (Context context, MyRenderer parentRenderer) {
        super(context);
        this.parentRenderer = parentRenderer;
    }

    @Override
    protected void initScene() {
        Log.d("PresentationRender", "initScene");

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(75);

        /*sq = new ScreenQuad();
        Material m = new Material();
        sq.setTransparent(true);
        sq.setMaterial(m);
        getCurrentScene().addChild(sq);*/
        //loadCubeMapPanorama(R.drawable.east_nx, R.drawable.east_ny, R.drawable.east_nz, R.drawable.east_px, R.drawable.east_py, R.drawable.east_pz);

        /*Texture texture;
        texture = new Texture("photo1", R.drawable.next);
        texture.shouldRecycle(true);
        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(10, 20, 20);
        sphere.setMaterial(material);
        Vector3 position = new Vector3(0, 0, -10);
        sphere.setPosition(position);

        getCurrentScene().addChild(sphere);*/


    }

    public void delayedInit() {
        getCurrentScene().resetGLState();
        //loadPhotoSpherePanorama(R.drawable.panorama);

    }

    protected void onRender(long ellapsedRealtime, double deltaTime) {
        getCurrentCamera().setCameraOrientation(parentRenderer.getCurrentCamera().getOrientation());
        super.onRender(ellapsedRealtime, deltaTime);
        if(mVideoTexture != null)
            mVideoTexture.update();

        if(descriptionCube != null)
            descriptionCube.setRotation(getCurrentCamera().getOrientation());
        //Log.d("PresentationRender", ""+ getCurrentScene().getNumChildren());
    }


    private static Sphere createPhotoSphereWithTexture(ATexture texture) {
        texture.shouldRecycle(true);
        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }

    private static Sphere createPhotoSphereWithTexture(StreamingTexture texture) {
        //texture.shouldRecycle(true);
        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }

    private static Sphere createSphereWithTexture(ATexture texture) {
        texture.shouldRecycle(true);
        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(10, 64, 32);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }

    private static Cube createCubeBoxWithTextures(String texture0, String texture1, String texture2, String texture3, String texture4, String texture5) {

        Material material = new Material();
        material.setColor(0);

        CubeMapTexture texture = new CubeMapTexture("tex0", new Bitmap[]{BitmapFactory.decodeFile(texture0), BitmapFactory.decodeFile(texture1), BitmapFactory.decodeFile(texture2), BitmapFactory.decodeFile(texture3), BitmapFactory.decodeFile(texture4), BitmapFactory.decodeFile(texture5)});
        texture.shouldRecycle(true);
        texture.isSkyTexture(true);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Cube cube = new Cube(100, true, true);
        cube.setMaterial(material);

        return cube;
    }

    private static Cube createCubeBoxWithTextures(Bitmap[] bitmaps) {

        Material material = new Material();
        material.setColor(0);

        CubeMapTexture texture = new CubeMapTexture("description0", bitmaps);
        texture.shouldRecycle(true);
        texture.isSkyTexture(true);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Cube cube = new Cube(100, true, true);
        cube.setMaterial(material);

        return cube;
    }

    private static Cube createCubeBoxWithTextures(int texture0, int texture1, int texture2, int texture3, int texture4, int texture5) {

        Material material = new Material();
        material.setColor(0);

        CubeMapTexture texture = new CubeMapTexture("tex0", new int[]{texture0,texture1, texture2, texture3, texture4, texture5});
        texture.shouldRecycle(true);
        texture.isSkyTexture(true);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Cube cube = new Cube(100, true, true);
        cube.setMaterial(material);

        return cube;
    }

    private void clear() {
        descriptionCube = null;
        getCurrentScene().clearChildren();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            TextureManager.getInstance().removeTexture(mVideoTexture);
            mVideoTexture = null;
        }
        hotspots.clear();
    }

    public void loadCubeMapPanorama(int texture0, int texture1, int texture2, int texture3, int texture4, int texture5) {
        clear();
        Cube cube = createCubeBoxWithTextures(texture0, texture1, texture2, texture3, texture4, texture5);
        getCurrentScene().addChild(cube);
    }

    public void loadCubeMapPanorama(String[] texture) {
        clear();
        Cube cube = createCubeBoxWithTextures(texture[0], texture[1], texture[2], texture[3], texture[4], texture[5]);
        getCurrentScene().addChild(cube);
    }

    public void loadPhotoSpherePanorama(int texture) {
        //obj.destroy();
        clear();
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo1", texture));
        getCurrentScene().addChild(sphere);
    }

    public void loadPhotoSpherePanorama(String texture) {
        //obj.destroy();
        clear();
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo1",  BitmapFactory.decodeFile(texture)));

        getCurrentScene().addChild(sphere);
    }

    public void loadDescription(String description) {
        Bitmap mHelloTexture = this.textAsBitmap(description);

        descriptionCube = createCubeBoxWithTextures(new Bitmap[] {mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture});
        descriptionCube.setTransparent(true);
        descriptionCube.setRotation(getCurrentCamera().getOrientation());
        getCurrentScene().addChild(descriptionCube);
    }

    public void loadVideoSpherePanorama(String texture) {
        //obj.destroy();
        clear();
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(texture);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d("PresentationRender", e.toString());
        }
        mMediaPlayer.setLooping(true);

        mVideoTexture = new StreamingTexture("video", mMediaPlayer);
        Sphere sphere = createPhotoSphereWithTexture(mVideoTexture);

        getCurrentScene().addChild(sphere);
        mMediaPlayer.start();

    }

    public void CreateHotspot(MainActivity.Hotspot hotspot) {
        ATexture texture;
        if (hotspot.thumbnail != null) {
            texture = new Texture("thumbnail",  BitmapFactory.decodeFile(hotspot.thumbnail));
        } else {
            switch (hotspot.type) {
                case toPano:
                    texture = new Texture("photo1", R.drawable.next);
                    break;
                case link:
                    texture = new Texture("hotspot1", R.drawable.link_icon);
                    break;
                case video:
                    texture = new Texture("hotspot1", R.drawable.video_icon);
                    break;
                default:
                    texture = new Texture("photo1", R.drawable.next);
                    break;
            }
        }
        texture.shouldRecycle(true);
        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(10, 20, 20);
        sphere.setMaterial(material);
        Vector3 position = new Vector3(hotspot.x / 10, hotspot.y / 10, hotspot.z / 10);
        sphere.setPosition(position);
        sphere.setRotation(hotspot.rx, hotspot.ry, hotspot.rz);

        getCurrentScene().addChild(sphere);
        hotspot.angle = Vector3.NEG_Z.getRotationTo(position);
        hotspots.add(hotspot);
    }

    public Bitmap textAsBitmap(String text) {// For later usage
        Paint paint = new Paint();
        paint.setTextSize(20);
        paint.setColor(Color.WHITE);
        Bitmap image = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 256, 256, paint);
        return image;
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
