package com.eje_c.rajawalicardboard;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import min3d.Shared;
import min3d.Utils;
import min3d.core.Renderer;
import min3d.core.Scene;
import min3d.core.TextureManager;
import min3d.interfaces.ISceneController;
import min3d.objectPrimitives.SkyBox;
import min3d.vos.Light;

/**
 * Created by iao on 20/10/15.
 */
public class DemoPresentation extends Presentation implements ISceneController {
    protected GLSurfaceView _glSurfaceView;
    private MyRenderer renderer;
    private MainActivity mainActivity;
    public Scene scene;
    private int idCount = 0;

    protected Handler _initSceneHander;
    protected Handler _updateSceneHander;
    private int[] cubeNums;
    private int panoNum;
    private String[] cubeStr;
    private String panoStr;
    private ToLoad toLoad = ToLoad.none;

    final Runnable _initSceneRunnable = new Runnable()
    {
        public void run() {
            onInitScene();
        }
    };

    final Runnable _updateSceneRunnable = new Runnable()
    {
        public void run() {
            onUpdateScene();
        }
    };

    public DemoPresentation(Context context, Display display, MainActivity mainActivity, MyRenderer renderer) {
        super(context, display);
        this.mainActivity = mainActivity;
        this.renderer = renderer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        _initSceneHander = new Handler();
        _updateSceneHander = new Handler();


        // Inflate the layout.
        //setContentView(R.layout.presentation_with_media_router_content);


        //
        // These 4 lines are important.
        //
        Shared.context(getContext());
        scene = new Scene(this);
        Renderer r = new Renderer(scene);
        Shared.renderer(r);

        _glSurfaceView = new GLSurfaceView(getContext());
        glSurfaceViewConfig();
        _glSurfaceView.setRenderer(r);
        _glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        onCreateSetContentView();
    }


    /**
     * Separated out for easier overriding...
     */
    protected void onCreateSetContentView()
    {
        setContentView(_glSurfaceView);
    }

    /**
     * Any GlSurfaceView settings that needs to be executed before
     * GLSurfaceView.setRenderer() can be done by overriding this method.
     * A couple examples are included in comments below.
     */
    protected void glSurfaceViewConfig()
    {
        // Example which makes glSurfaceView transparent (along with setting scene.backgroundColor to 0x0)
        // _glSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
        // _glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Example of enabling logging of GL operations
        // _glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
    }

    public GLSurfaceView getSurfaceView() {
        return _glSurfaceView;
    }

    @Override
    public void initScene() {
        Log.d("PresentationRender", "initScene");

        scene.camera().position.setAll(0, 0, 0);
        //getCurrentCamera().setPosition(Vector3.ZERO);
        scene.lights().add(new Light());


        /*sq = new ScreenQuad();
        Material m = new Material();
        sq.setTransparent(true);
        sq.setMaterial(m);
        getCurrentScene().addChild(sq);*/
        //loadCubeMapPanorama(R.drawable.east_nx, R.drawable.east_ny, R.drawable.east_nz, R.drawable.east_px, R.drawable.east_py, R.drawable.east_pz);

        //loadPhotoSpherePanorama(R.drawable.panorama);

    }

    public void setLoadPhotoSpherePanorama(int panorama) {
        toLoad = ToLoad.equirectangularpanoNum;
        panoNum = panorama;
    }

    private void loadPhotoSpherePanorama(int panorama) {
        scene.reset();
        scene.camera().position.setAll(0, 0, 0);
        scene.lights().add(new Light());

        min3d.objectPrimitives.Sphere sphere = new min3d.objectPrimitives.Sphere(50, 64, 32);
        sphere.scale().x = -1;
        Bitmap bitmap = Utils.makeBitmapFromResourceId(panorama);
        TextureManager texMan = Shared.textureManager();
        if(texMan.contains("sphereTexture"+idCount))
            texMan.deleteTexture("sphereTexture"+idCount);
        idCount++;
        texMan.addTextureId(bitmap, "sphereTexture" + idCount, true);
        bitmap.recycle();
        sphere.textures().addById("sphereTexture"+idCount);
        scene.addChild(sphere);
    }

    public void setLoadPhotoSpherePanorama(String panorama) {
        toLoad = ToLoad.equirectangularpano;
        panoStr = panorama;
    }

    private void loadPhotoSpherePanorama(String texture) {
        scene.reset();
        scene.camera().position.setAll(0, 0, 0);
        scene.lights().add(new Light());

        min3d.objectPrimitives.Sphere sphere = new min3d.objectPrimitives.Sphere(50, 64, 32);
        sphere.scale().x = -1;
        Bitmap bitmap = BitmapFactory.decodeFile(texture);
        TextureManager texMan = Shared.textureManager();
        if(texMan.contains("sphereTexture"+idCount))
            texMan.deleteTexture("sphereTexture"+idCount);
        idCount++;
        Shared.textureManager().addTextureId(bitmap, "sphereTexture" + idCount, true);
        bitmap.recycle();
        sphere.textures().addById("sphereTexture" + idCount);
        scene.addChild(sphere);
    }

    public void setLoadCubeMapPanorama(int nx, int ny, int nz, int px, int py, int pz) {
        toLoad = ToLoad.imagepanoNum;
        cubeNums = new int[] {nx, ny, nz, px, py, pz};
    }

    private void loadCubeMapPanorama(int nx, int ny, int nz, int px, int py, int pz) {
        scene.reset();
        scene.camera().position.setAll(0, 0, 0);
        scene.lights().add(new Light());

        SkyBox skyBox = new SkyBox(100, 2);
        TextureManager texMan = Shared.textureManager();
        if(texMan.contains("nx"+idCount))
            texMan.deleteTexture("nx"+idCount);
        if(texMan.contains("ny"+idCount))
            texMan.deleteTexture("ny"+idCount);
        if(texMan.contains("nz"+idCount))
            texMan.deleteTexture("nz"+idCount);
        if(texMan.contains("px"+idCount))
            texMan.deleteTexture("px"+idCount);
        if(texMan.contains("py"+idCount))
            texMan.deleteTexture("py"+idCount);
        if(texMan.contains("pz"+idCount))
            texMan.deleteTexture("pz"+idCount);
        idCount++;
        skyBox.addTexture(SkyBox.Face.West, nx, "nx"+idCount);
        skyBox.addTexture(SkyBox.Face.East, pz, "px"+idCount);

        skyBox.addTexture(SkyBox.Face.North, nz, "nz"+idCount);
        skyBox.addTexture(SkyBox.Face.South, px, "pz"+idCount);

        skyBox.addTexture(SkyBox.Face.Down, ny, "ny" + idCount);
        skyBox.addTexture(SkyBox.Face.Up, py, "py" + idCount);

        scene.addChild(skyBox);
    }

    public void setLoadCubeMapPanorama(String[] texture) {
        toLoad = ToLoad.imagepano;
        cubeStr = texture;
    }

    private void loadCubeMapPanorama(String[] texture) {
        scene.reset();
        scene.camera().position.setAll(0, 0, 0);
        scene.lights().add(new Light());

        SkyBox skyBox = new SkyBox(100, 2);
        TextureManager texMan = Shared.textureManager();
        if(texMan.contains("nx"+idCount))
            texMan.deleteTexture("nx"+idCount);
        if(texMan.contains("ny"+idCount))
            texMan.deleteTexture("ny"+idCount);
        if(texMan.contains("nz"+idCount))
            texMan.deleteTexture("nz"+idCount);
        if(texMan.contains("px"+idCount))
            texMan.deleteTexture("px"+idCount);
        if(texMan.contains("py"+idCount))
            texMan.deleteTexture("py"+idCount);
        if(texMan.contains("pz"+idCount))
            texMan.deleteTexture("pz"+idCount);
        idCount++;

        skyBox.addTexture(SkyBox.Face.West, BitmapFactory.decodeFile(texture[0]), "nx" + idCount);
        skyBox.addTexture(SkyBox.Face.East, BitmapFactory.decodeFile(texture[5]), "px"+idCount);

        skyBox.addTexture(SkyBox.Face.North, BitmapFactory.decodeFile(texture[2]), "nz"+idCount);
        skyBox.addTexture(SkyBox.Face.South, BitmapFactory.decodeFile(texture[3]), "pz"+idCount);

        skyBox.addTexture(SkyBox.Face.Down, BitmapFactory.decodeFile(texture[1]), "ny" + idCount);
        skyBox.addTexture(SkyBox.Face.Up, BitmapFactory.decodeFile(texture[4]), "py" + idCount);

        scene.addChild(skyBox);
    }

    @Override
    public void updateScene() {
        switch (toLoad) {
            case imagepano:
                loadCubeMapPanorama(cubeStr);
                panoStr = null;
                break;
            case imagepanoNum:
                loadCubeMapPanorama(cubeNums[0], cubeNums[1], cubeNums[2], cubeNums[3], cubeNums[4], cubeNums[5]);
                cubeNums = null;
                break;
            case equirectangularpano:
                loadPhotoSpherePanorama(panoStr);
                break;
            case equirectangularpanoNum:
                loadPhotoSpherePanorama(panoNum);
                break;
        }
        toLoad = ToLoad.none;

        Quaternion q = renderer.getCurrentCamera().getOrientation();
        //Log.d("PresentationRender", "updateScene " + q.getYaw() + " " + q.getRoll() + "  " + q.getPitch());

        scene.camera().upAxis.setAll(0, 1, 0);
        //scene.camera().upAxis.rotateX((float) q.getYaw());
        //scene.camera().upAxis.rotateY((float) q.getRoll());
        //scene.camera().upAxis.rotateZ((float) q.getRoll());
        Vector3 target = new Vector3(0, 0, 5);
        target.rotateBy(q);
        scene.camera().target.setAll((float)target.x, (float)target.y * -1f, (float)target.z);

        /*scene.camera().target.setAll(0, 0, 5);
        scene.camera().target.rotateX((float) q.getPitch());
        scene.camera().target.rotateY((float) q.getYaw() * -1f);*/
        /*renderer.getCurrentCamera().getViewMatrix();*/
    }

    /**
     * Called _after_ scene init (ie, after initScene).
     * Unlike initScene(), gets called from the UI thread.
     */
    public void onInitScene()
    {
    }

    /**
     * Called _after_ updateScene()
     * Unlike initScene(), gets called from the UI thread.
     */
    public void onUpdateScene()
    {
    }

    public Handler getInitSceneHandler()
    {
        return _initSceneHander;
    }

    public Handler getUpdateSceneHandler()
    {
        return _updateSceneHander;
    }

    public Runnable getInitSceneRunnable()
    {
        return _initSceneRunnable;
    }

    public Runnable getUpdateSceneRunnable()
    {
        return _updateSceneRunnable;
    }

    public enum ToLoad {
        imagepano, equirectangularpano, videopano, imagepanoNum, equirectangularpanoNum, videopanoNum, none
    }
}
