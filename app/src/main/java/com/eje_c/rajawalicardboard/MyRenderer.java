package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import org.rajawali3d.Object3D;
import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.util.Capabilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MyRenderer extends RajawaliCardboardRenderer {

    //private ATexture texture;
    //private Object3D obj;

    private MediaPlayer mMediaPlayer;
    private StreamingTexture mVideoTexture;
    private ScreenQuad sq;
    private Set<MainActivity.Hotspot> hotspots = Collections.synchronizedSet(new HashSet<MainActivity.Hotspot>());
    private int triggerCount = 0;
    private Object3D descriptionCube;
    private RenderTarget target;
    private int descriptionCount;
    private static final double mImageScale = 2;
    private static final int mImageSize = 3072;
    private boolean exit = false;
    private int exitTimer = 0;

    public MyRenderer(Context context, boolean cardboard) {
        super(context);
        /*if(!cardboard)
            mImageScale = 1.1;*/
    }

    @Override
    protected void initScene() {
        ((MainActivity)getContext()).loadPano();

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(75);

        /*sq = new ScreenQuad();
        Material m = new Material();
        sq.setTransparent(true);
        sq.setMaterial(m);
        getCurrentScene().addChild(sq);*/
        target = new RenderTarget("Target", 1000, 1000);
        //setRenderTarget(target);

    }

    public void onPause() {
        super.onPause();
        if(mMediaPlayer != null)
            mMediaPlayer.pause();
    }

    public void onResume() {
        super.onResume();
        if(mMediaPlayer != null)
            mMediaPlayer.start();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        /*setRenderTarget(target);
        render(0, 0);
        setRenderTarget(null);*/
    }

    public void startExit() {
        exit = true;
    }

    public void cancelExit() {
        exit = false;
        exitTimer = 0;
    }

    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        if(mVideoTexture != null) {
            mVideoTexture.update();
            //Log.d("MyRender", "" + mVideoTexture.getWidth() + " " + mVideoTexture.getHeight());
        }
        boolean triggered = false;
        for (MainActivity.Hotspot hotspot: hotspots) {
            double angleBetween = getCurrentCamera().getOrientation().angleBetween(hotspot.angle);
            if(angleBetween < 0.1) {
                if(++triggerCount > 150)
                    hotspot.trigger();
                else
                    triggered = true;
            }
        }
        if(!triggered)
            triggerCount = 0;
        if(descriptionCube != null) {
            descriptionCube.setRotation(getCurrentCamera().getOrientation());
            /*descriptionCount++;
            if(descriptionCount > 1000) {
                getCurrentScene().removeChild(descriptionCube);
                descriptionCube = null;
            }*/

        }
        if(exit) {
            exitTimer++;
            if(exitTimer > 30) {
                clear();
                ((MainActivity)getContext()).finish();
                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(50);
            }
        }
        //Log.d("MyRender", viewport.toString());
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

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

    private static Sphere createPhotoSphereWithTexture(ATexture[] textures) {

        VertexShader vertexShader = new VertexShader();
        FragmentShader fragmentShader = new FragmentShader();

        fragmentShader.initialize();
        fragmentShader.addShaderFragment(new TiledShader(Arrays.asList(textures)));
        fragmentShader.buildShader();
        fragmentShader.setNeedsBuild(false);

        Material material = new Material(vertexShader, fragmentShader);
        material.setColor(0);



        for (ATexture texture: textures) {

            texture.shouldRecycle(true);
            try {
                material.addTexture(texture);
            } catch (ATexture.TextureException e) {
                throw new RuntimeException(e);
            }
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

        Cube cube = new Cube(10, true, true);
        cube.setMaterial(material);

        return cube;
    }

    private static Cube createCubeBoxWithTexture(Bitmap bitmap) {

        Material material = new Material();
        material.setColor(0);

        ATexture texture = new Texture("description0", bitmap);
        texture.shouldRecycle(true);
        //texture.isSkyTexture(true);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Cube cube = new Cube(10, true, false);
        cube.setMaterial(material);

        return cube;
    }

    private static Cube createCubeBoxWithTexture(ATexture texture) {

        Material material = new Material();
        material.setColor(0);

        //ATexture texture = new Texture("description0", bitmap);
        texture.shouldRecycle(true);
        //texture.isSkyTexture(true);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Cube cube = new Cube(-10);
        cube.setDoubleSided(true);
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


    public void clear() {
        descriptionCube = null;
        descriptionCount = 0;
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
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo", texture));
        getCurrentScene().addChild(sphere);
    }

    public void loadPhotoSperePanorama(int... textureIDs) {
        clear();
        ATexture[] textures = new ATexture[textureIDs.length];
        int i = 0;
        for (int texture: textureIDs) {
            textures[i++] = new Texture("tex"+i, texture);
        }
        Sphere sphere = createPhotoSphereWithTexture(textures);
        getCurrentScene().addChild(sphere);
    }

    public void loadPhotoSpherePanorama(String texture) {
        //obj.destroy();
        clear();
        Bitmap bitmap = BitmapFactory.decodeFile(texture);
        int size = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if(Capabilities.getInstance().getMaxTextureSize() >= size) {
            loadPhotoSpherePanorama(bitmap);
        } else {
            int texSize = Capabilities.getInstance().getMaxTextureSize();
            int width = (int) Math.ceil((double)bitmap.getWidth()/texSize);
            int height = (int) Math.ceil((double)bitmap.getHeight()/texSize);
            Log.d("MyRender", width + " " + height);

            Bitmap[] bitmaps = generateTiles(bitmap, width, height);
            bitmap.recycle();
            loadPhotoSperePanorama(bitmaps);

        }

    }

    private void loadPhotoSpherePanorama(Bitmap texture) {
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo", texture));
        getCurrentScene().addChild(sphere);
    }

    public void loadPhotoSperePanorama(Bitmap... bitmaps) {
        clear();
        ATexture[] textures = new ATexture[bitmaps.length];
        int i = 0;
        for (Bitmap bitmap: bitmaps) {
            textures[i++] = new Texture("tex"+i, bitmap);
        }
        Sphere sphere = createPhotoSphereWithTexture(textures);
        getCurrentScene().addChild(sphere);
    }

    private Bitmap[] generateTiles(Bitmap bitmap, int width, int height) {
        Bitmap[] bitmaps = new Bitmap[width*height];
        int tileWidth = bitmap.getWidth() / width;
        int tileHeight = bitmap.getHeight() / height;
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int index  = i + j*width;
                bitmaps[index] = Bitmap.createBitmap(tileWidth, tileHeight, bitmap.getConfig());
                Canvas canvas = new Canvas(bitmaps[index]);
                canvas.drawBitmap(bitmap, new Rect(tileWidth*i, tileHeight*j, tileWidth*(i+1), tileHeight*(j+1)), new Rect(0, 0, tileWidth, tileHeight), null);
            }
        }

        return bitmaps;
    }

    public void loadDescription(String description) {
        Bitmap mHelloTexture = this.textAsBitmap(description);

        descriptionCube = createCubeBoxWithTextures(new Bitmap[] {mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture, mHelloTexture});
        descriptionCube.setTransparent(true);
        descriptionCube.setRotation(getCurrentCamera().getOrientation());
        getCurrentScene().addChild(descriptionCube);
    }

    public void loadImage(int texture) {
        clear();

        Resources res = getContext().getResources();
        Bitmap image = BitmapFactory.decodeResource(res, texture);
        image = prepareImage(image);

        //descriptionCube = createCubeBoxWithTextures(new Bitmap[] {image, image, image, image, image, image});
        ATexture texture1 = new Texture("description0", image);
        descriptionCube = createCubeBoxWithTexture(texture1);
        descriptionCube.setRotation(getCurrentCamera().getOrientation());
        getCurrentScene().addChild(descriptionCube);
    }

    public void loadImage(String texture) {
        clear();

        Resources res = getContext().getResources();
        Bitmap image = BitmapFactory.decodeFile(texture);
        image = prepareImage(image);

        //descriptionCube = createCubeBoxWithTextures(new Bitmap[] {image, image, image, image, image, image});
        ATexture texture1 = new Texture("description0", image);
        descriptionCube = createCubeBoxWithTexture(texture1);
        descriptionCube.setRotation(getCurrentCamera().getOrientation());
        getCurrentScene().addChild(descriptionCube);
    }

    public void loadVideo(String texture) {
        clear();

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(texture);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d("MyRender", e.toString());
        }
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        mVideoTexture = new StreamingTexture("video", mMediaPlayer);
        //mVideoTexture.setWrapType(ATexture.WrapType.REPEAT);
        /*mVideoTexture.setWidth(mMediaPlayer.getVideoWidth());
        mVideoTexture.setHeight(mMediaPlayer.getVideoHeight());*/
        int width = mMediaPlayer.getVideoWidth();
        int height = mMediaPlayer.getVideoHeight();
        double ratio = ((double)width / height);
        float scale = 2f;
        float offset = -0.25f;
        float offsetTop = -0.2f;
        mVideoTexture.enableOffset(true);
        if(width > height) {
            mVideoTexture.setRepeat(scale, (float) (scale * ratio));
            mVideoTexture.setOffset(offset, (float) (offsetTop*ratio));
        } else {
            mVideoTexture.setRepeat((float) (scale / ratio), scale);
            mVideoTexture.setOffset((float) (offset / ratio), offset);
        }



        Log.d("MyRender", ""+mMediaPlayer.getVideoWidth() + " " + mMediaPlayer.getVideoHeight() + " " + ratio);
        descriptionCube = createCubeBoxWithTexture(mVideoTexture);
        descriptionCube.getMaterial().addPlugin(new BorderClampPlugin());
        descriptionCube.setRotation(getCurrentCamera().getOrientation());
        //descriptionCube.setScaleZ(20);
        getCurrentScene().addChild(descriptionCube);

    }

    private Bitmap prepareImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = (int) (Math.max(width, height) * mImageScale);

        int left = (size - width) / 2;
        int top = (size - height ) / 2;

        double scale = (double) mImageSize / size;
        int leftS = (int) (left * scale);
        int topS = (int) (top * scale);
        int rightS = (int) (width * scale) + leftS;
        int bottomS = (int) (height * scale) + topS;

        Log.d("MyRender", ""+left + " " + top + " " + scale);
        Bitmap image = Bitmap.createBitmap(mImageSize, mImageSize, bitmap.getConfig());
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(bitmap, null, new Rect(leftS, topS, rightS, bottomS), null);
        bitmap.recycle();
        return image;
    }

    public void loadVideoSpherePanorama(String texture) {
        //obj.destroy();
        clear();

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(texture);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d("MyRender", e.toString());
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
                    texture = new Texture("photo", R.drawable.next);
                    break;
                case link:
                    texture = new Texture("hotspot", R.drawable.link_icon);
                    break;
                case video:
                    texture = new Texture("hotspot", R.drawable.video_icon);
                    break;
                default:
                    texture = new Texture("photo", R.drawable.next);
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
        paint.setTextSize(10);
        paint.setColor(Color.WHITE);
        Bitmap image = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 100, 256, paint);
        return image;
    }
}
