package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import org.rajawali3d.Object3D;
import org.rajawali3d.cardboard.RajawaliCardboardView;
import org.rajawali3d.math.Quaternion;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends CardboardActivity {

    private int panoIndex = 0;
    public MyRenderer renderer;
    private PanoLocation[] panoLocations;
    private MediaPlayer mMediaPlayer;
    private String dir;
    private OrientationEventListener orientationEventListener;
    private int THRESHOLD = 5;

    private MediaRouter mMediaRouter;
    private DemoPresentation mPresentation;
    private boolean mPaused;

    private CardboardOverlayView overlayView;
    private boolean cardboard;

    private final int mMediaTypes = MediaRouter.ROUTE_TYPE_LIVE_AUDIO | MediaRouter.ROUTE_TYPE_LIVE_VIDEO | (1 << 2) | MediaRouter.ROUTE_TYPE_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //RajLog.setDebugEnabled(true);

        Intent intent = getIntent();
        dir = intent.getStringExtra("dir") + "/";
        cardboard = intent.getBooleanExtra("cardboard", true);

        Log.d(this.getLocalClassName(), dir);

        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream in_s = new FileInputStream(new File(dir + "test.xml"));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            parseXML(parser);

        } catch (XmlPullParserException e) {

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*panoLocations = new PanoLocation[]{new PanoLocation(new String[]{"/sdcard/PhotoTour/panorama.jpg"},
                PanoType.imagepano), new PanoLocation(new String[]{"/sdcard/PhotoTour/east_px.jpg", "/sdcard/PhotoTour/east_nx.jpg",
                "/sdcard/PhotoTour/east_py.jpg", "/sdcard/PhotoTour/east_ny.jpg", "/sdcard/PhotoTour/east_pz.jpg", "/sdcard/PhotoTour/east_nz.jpg"}, PanoType.equirectangularpano)};*/

        //RajawaliCardboardView view = new RajawaliCardboardView(this);
        //setContentView(view);

        setContentView(R.layout.main_layout);
        RajawaliCardboardView view = (RajawaliCardboardView)findViewById(R.id.cardboard_view);

        setCardboardView(view);

        if(!cardboard) {
            view.setVRModeEnabled(false);
        }
        //Log.d(this.getLocalClassName(), view.getScreenParams().toString());

        renderer = new MyRenderer(this, cardboard);

        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);

        //view.getCurrentEyeParams();

        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                if((orientation >= (360 - THRESHOLD) && orientation <= 360) || (orientation >= 0 && orientation <= THRESHOLD)) {
                    //if(renderer != null)
                    renderer.startExit();
                    //MainActivity.this.finish();
                } else {
                    renderer.cancelExit();
                }
            }
        };

        if (orientationEventListener.canDetectOrientation() == true) {
            Log.v(getLocalClassName(), "Can detect orientation");
            orientationEventListener.enable();
        } else {
            Log.v(getLocalClassName(), "Cannot detect orientation");
            orientationEventListener.disable();
        }

        // Get the media router service.
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);

        HeadTransform head = new HeadTransform();
        Eye left = new Eye(1);
        Eye right = new Eye(2);
        Eye one = new Eye(0);
        Eye leftFull = new Eye(1);
        Eye rightFull = new Eye(2);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        view.getCurrentEyeParams(head, left, right, one, leftFull, rightFull);
        Log.v(getLocalClassName(), leftFull.getViewport().toString() + " " + rightFull.getViewport().toString() + " " + one.getViewport().toString() + " "+height + " " + width);


        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        if(cardboard) {
            overlayView.reLayout(leftFull.getViewport(), rightFull.getViewport(), one.getViewport(), height);
        } else {
            Viewport viewport = new Viewport();
            viewport.x = 0;
            viewport.y = 0;
            viewport.height = height;
            viewport.width = width;
            overlayView.reLayout(viewport, rightFull.getViewport(), one.getViewport(), height);
        }

        //overlayView.show3DToast("Pull the magnet when you find an object.");

    }

    protected void onPause() {
        super.onPause();
        if(mMediaPlayer != null)
            mMediaPlayer.pause();


        // Stop listening for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);

        // Pause rendering.
        mPaused = true;
        updateContents();
    }

    protected void onResume() {
        super.onResume();
        if(mMediaPlayer != null)
            mMediaPlayer.start();


        // Listen for changes to media routes.
        mMediaRouter.addCallback(mMediaTypes, mMediaRouterCallback);
        //mMediaRouter.addCallback(mMediaTypes, mMediaRouterCallback);

        // Update the presentation based on the currently selected route.
        mPaused = false;
        Log.v(getLocalClassName(), "onResume");
        updatePresentation();
    }

    private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    {
        ArrayList<PanoLocation> locations = null;
        HashMap<String, Integer>panoIDs = new HashMap<String, Integer>();
        int eventType = parser.getEventType();
        PanoLocation currentPano = null;
        Hotspot currentHotspot = null;
        boolean panos = false;
        boolean hotspots = false;
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;

            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    locations = new ArrayList<PanoLocation>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Log.d(getLocalClassName(), name);
                    if (name.equalsIgnoreCase("panos")){
                        panos = true;
                    } else if(panos) {
                        if (name.equalsIgnoreCase("pano")) {
                            currentPano = new PanoLocation();
                        } else if (currentPano != null) {
                            if (name.equalsIgnoreCase("right")) {
                                currentPano.images[1] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("left")) {
                                currentPano.images[0] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("up")) {
                                currentPano.images[2] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("down")) {
                                currentPano.images[3] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("front")) {
                                currentPano.images[4] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("back")) {
                                currentPano.images[5] = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("type")) {
                                currentPano.type = PanoType.valueOf(parser.nextText());
                            } else if (name.equalsIgnoreCase("id")) {
                                panoIDs.put(parser.nextText(), locations.size());
                            } else if (name.equalsIgnoreCase("description")) {
                                currentPano.description = parser.nextText();
                                Log.d(getLocalClassName(), "| "+currentPano.description);
                            } else if (name.equalsIgnoreCase("name")) {
                                currentPano.name = parser.nextText();
                                //Log.d(getLocalClassName(), currentPano.name);
                            } else if (name.equalsIgnoreCase("latitude")) {
                                try {
                                    currentPano.latatude = Double.parseDouble(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("longitude")) {
                                try {
                                    currentPano.longitude = Double.parseDouble(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("audioFile")) {
                                String file = parser.nextText();
                                if(!file.equalsIgnoreCase("null"))
                                currentPano.audioFile = dir + file;
                            }
                        }
                    } else if (name.equalsIgnoreCase("hotspots")) {
                        hotspots = true;
                    } else if(hotspots) {
                        if (name.equalsIgnoreCase("hotspot")) {
                            currentHotspot = new Hotspot();
                        } else if (currentHotspot != null) {
                            if (name.equalsIgnoreCase("type")) {
                                currentHotspot.type = HotspotType.valueOf(parser.nextText());
                            } else if (name.equalsIgnoreCase("parentPano")) {
                                String parentPano = parser.nextText();
                                currentHotspot.parentPano = locations.get(panoIDs.get(parentPano));
                            } else if (name.equalsIgnoreCase("value")) {
                                currentHotspot.value = parser.nextText();
                            } else if (name.equalsIgnoreCase("transition")) {
                                currentHotspot.transition = dir + parser.nextText();
                            } else if (name.equalsIgnoreCase("x")) {
                                try {
                                    currentHotspot.x = Integer.parseInt(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("y")) {
                                try {
                                    currentHotspot.y = Integer.parseInt(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("z")) {
                                try {
                                    currentHotspot.z = Integer.parseInt(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("rx")) {
                                try {
                                    currentHotspot.rx = Double.parseDouble(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("ry")) {
                                try {
                                    currentHotspot.ry = Double.parseDouble(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("rz")) {
                                try {
                                    currentHotspot.rz = Double.parseDouble(parser.nextText());
                                } catch (Exception e) {}
                            } else if (name.equalsIgnoreCase("thumbnail")) {
                                currentHotspot.thumbnail = dir + parser.nextText();
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("panos")){
                        panos = false;
                    }
                    if (panos && name.equalsIgnoreCase("pano") && currentPano != null){
                        locations.add(currentPano);
                    }
                    if (name.equalsIgnoreCase("hotspots")){
                        hotspots = false;
                    }
                    if (hotspots && name.equalsIgnoreCase("hotspot") && currentHotspot != null && currentHotspot.parentPano != null){
                        currentHotspot.parentPano.hostspots.add(currentHotspot);
                        if(currentHotspot.type == HotspotType.toPano && currentHotspot.value != null) {
                            currentHotspot.target = locations.get(panoIDs.get(currentHotspot.value));
                            currentHotspot.targetIndex = panoIDs.get(currentHotspot.value);
                        }
                    }
            }
            eventType = parser.next();
        }
        panoLocations = locations.toArray(new PanoLocation[locations.size()]);
    }


    public void onCardboardTrigger() {
        panoIndex++;
        panoIndex %= panoLocations.length;
        loadPano();
    }

    public void loadPano(int index) {
        this.panoIndex = index;
        loadPano();
    }

    MediaPlayer.OnPreparedListener prepListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
        }
    };

    public void loadPano() {

        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if(!cardboard) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    overlayView.clearImage();
                }
            });

        }

        final PanoLocation pano = panoLocations[panoIndex];
        switch (pano.type) {
            case equirectangularpano:
                renderer.loadPhotoSpherePanorama(panoLocations[panoIndex].images[1]);
                if(mPresentation != null)
                    mPresentation.setLoadPhotoSpherePanorama(panoLocations[panoIndex].images[1]);
                break;
            case imagepano:
                renderer.loadCubeMapPanorama(panoLocations[panoIndex].images);
                //renderer.loadPhotoSperePanorama(R.drawable.pano_1_1, R.drawable.pano_1_2);
                if(mPresentation != null)
                    mPresentation.setLoadCubeMapPanorama(panoLocations[panoIndex].images);
                break;
            case videopano:
                renderer.loadVideoSpherePanorama(panoLocations[panoIndex].images[1]);
                break;
            case image:
                if(cardboard) {
                    renderer.loadImage(panoLocations[panoIndex].images[1]);
                } else {
                    renderer.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            overlayView.setImage(panoLocations[panoIndex].images[1]);
                        }
                    });

                }
                break;
            case video:
                renderer.loadVideo(panoLocations[panoIndex].images[1]);
                break;
        }
        if(pano.description != null && !pano.description.equals("")) {
            //renderer.loadDescription(pano.description);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    overlayView.show3DToast(pano.description);
                }
            });

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    overlayView.clear3dToast();
                }
            });
        }


        if (pano.audioFile != null) {
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(pano.audioFile);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                Log.d("MyRender", e.toString());
            }
            mMediaPlayer.start();
        }
        renderHotspots(pano);



    }

    private void renderHotspots(PanoLocation pano) {
        for (Hotspot hotspot:pano.hostspots) {
            renderer.CreateHotspot(hotspot);
        }
    }


    private void updatePresentation() {
        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(mMediaTypes);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(getLocalClassName(), "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(getLocalClassName(), "Showing presentation on display: " + presentationDisplay);
            mPresentation = new DemoPresentation(this, presentationDisplay, this, renderer);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(getLocalClassName(), "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }
        updateContents();
    }

    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        if (mPresentation != null) {
            if (mPaused) {
                mPresentation.getSurfaceView().onPause();
            } else {
                mPresentation.getSurfaceView().onResume();
            }
        }
    }


    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(getLocalClassName(), "onRouteSelected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(getLocalClassName(), "onRouteUnselected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(getLocalClassName(), "onRoutePresentationDisplayChanged: info=" + info);
                    updatePresentation();
                }
            };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        Log.i(getLocalClassName(), "Presentation was dismissed.");
                        mPresentation = null;
                    }
                }
            };

    public enum PanoType {
        imagepano, equirectangularpano, videopano, image, video
    }

    public class PanoLocation {
        String[] images;
        boolean isActive = false;
        PanoType type;
        String description = null;
        String audioFile;
        double latatude;
        double longitude;
        String name;
        ArrayList<Hotspot> hostspots;

        public PanoLocation() {
            this.images = new String[6];
            this.hostspots = new ArrayList<Hotspot>();
        }

        public PanoLocation(String[] images, PanoType type) {
            this.images = images;
            this.type = type;
            /*this.name = name;
            this.description = description;*/
        }
    }

    public enum HotspotType {
        toPano, link, video
    }

    public class Hotspot {
        Object3D hostspot;
        HotspotType type;
        PanoLocation parentPano;
        int x;
        int y;
        int z;
        double rx;
        double ry;
        double rz;
        PanoLocation target;
        int targetIndex;
        String value;
        String transition;
        String thumbnail;
        Quaternion angle;

        public void trigger() {
            MainActivity.this.loadPano(targetIndex);
        }
    }

    /**
            * The presentation to show on the secondary display.
            * <p>
    * Note that this display may have different metrics from the display on which
    * the main activity is showing so we must be careful to use the presentation's
            * own {@link Context} whenever we load resources.
            * </p>
            */

    /*private final static class DemoPresentation extends Presentation {
        private GLSurfaceView mSurfaceView;

        public DemoPresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);

            // Get the resources for the context of the presentation.
            // Notice that we are getting the resources from the context of the presentation.
            Resources r = getContext().getResources();

            // Inflate the layout.
            setContentView(R.layout.presentation_with_media_router_content);

            // Set up the surface view for visual interest.
            mSurfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
            mSurfaceView.setRenderer(new CubeRenderer(false));
        }

        public GLSurfaceView getSurfaceView() {
            return mSurfaceView;
        }
    }*/
}
