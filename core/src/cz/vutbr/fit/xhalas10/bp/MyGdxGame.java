package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import cz.vutbr.fit.xhalas10.bp.earth.Compass;
import cz.vutbr.fit.xhalas10.bp.earth.EarthCamera;
import cz.vutbr.fit.xhalas10.bp.earth.Poi;
import cz.vutbr.fit.xhalas10.bp.osm.OSMData;
import cz.vutbr.fit.xhalas10.bp.osm.model.Node;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;

public class MyGdxGame extends ApplicationAdapter {
//    private static final boolean showDebugInfo = true;
    private final double TOUCH_SCALE_FACTOR = 63.0 / 1920.0;
    Stage stage;
    private float angle = 0;
//    private Label positionLabel;
//    private Label accuracyLabel;
    private boolean canRotateCamera = false;
    private WorldManager worldManager;
    private EarthCamera earthCamera;
    private Utils utils;

    MyGdxGame(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);

        stage = new Stage(new ScreenViewport());
        UserInterface userInterface = new UserInterface(this);
        userInterface.setToStage();

//        BitmapFont font = MySkin.getInstance().getFont("default");
//        Label.LabelStyle style = new Label.LabelStyle(font, Color.GREEN);
//        positionLabel = new Label("Press a Button", style);
//        positionLabel.setSize(Gdx.graphics.getWidth(), 60);
//        positionLabel.setPosition(0, 0);
//        positionLabel.setAlignment(Align.center);
//        positionLabel.toBack();
//
//        accuracyLabel = new Label("Press a Button", style);
//        accuracyLabel.setSize(Gdx.graphics.getWidth(), 60);
//        accuracyLabel.setPosition(0, 60);
//        accuracyLabel.setAlignment(Align.center);
//        accuracyLabel.toBack();
//        if (showDebugInfo) {
//            stage.addActor(positionLabel);
//            stage.addActor(accuracyLabel);
//        }

        utils.getHardwareCamera().init();
        earthCamera = new EarthCamera(utils.getHardwareCamera());

        worldManager = WorldManager.getInstance();
        worldManager.setWorldCamera(earthCamera);

        Compass compass = new Compass();
        worldManager.addWorldObject(compass, false);

        for (Node node : OSMData.getInstance().getOSMNodes()) {
            worldManager.addWorldObject(Poi.fromOSMNode(node), true);
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            private double mPreviousX = 0;
            private double mPreviousY = 0;

            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                mPreviousX = x;
                mPreviousY = y;
                return true; // return true to indicate the event was handled
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                mPreviousX = 0;
                mPreviousY = 0;
                return true; // return true to indicate the event was handled
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (canRotateCamera) {
                    double dx = screenX - mPreviousX;
                    double dy = screenY - mPreviousY;
                    dx = dx * 1;
                    dy = dy * 1;

                    angle += (dx + dy) * TOUCH_SCALE_FACTOR;

                    mPreviousX = screenX;
                    mPreviousY = screenY;
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        earthCamera.setCorrectionAngle(angle);
        earthCamera.setSensorQuaternion(utils.getSensorManager().getQuaternion());

        utils.getHardwareCamera().renderBackground();

        updateLocation();
        worldManager.renderWorld();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void updateLocation() {
        if (!earthCamera.getLocation().equals(utils.getPersonLocation().getLocation())) {
            earthCamera.setPosition(utils.getPersonLocation().getLocation());
            earthCamera.calculateOriginRelativePosition();
            worldManager.updateObjectsAndCache();
//            if (showDebugInfo) {
//                positionLabel.setText(String.format(Locale.getDefault(), "Lat: %.6f   Lon: %.6f   Alt: %.1f",
//                        utils.getPersonLocation().getLocation().getLatitude(),
//                        utils.getPersonLocation().getLocation().getLongitude(),
//                        utils.getPersonLocation().getLocation().getAltitude()));
//
//                accuracyLabel.setText(String.format(Locale.getDefault(), "Nodes: %d   hAc: %.1f   vAc: %.1f",
//                        OSMData.getInstance().getOSMNodes().size(),
//                        utils.getPersonLocation().getVerticalAccuracy(),
//                        utils.getPersonLocation().getHorizontalAccuracy()));
//            }
        }
    }

    public void downloadSurroundingData() {
        OSMData.getInstance().getSurroundingData(earthCamera.getLocation(), earthCamera.getCamera().far / 100000.0f);

        worldManager.clearManagedObjects();
        OSMData.getInstance().getOSMNodes().forEach(node -> worldManager.addWorldObject(Poi.fromOSMNode(node), true));
        worldManager.updateAll();

        utils.showToast("Surrounding data downloaded and set");
    }

    @Override
    public void dispose() {
        utils.getHardwareCamera().dispose();
        worldManager.dispose();
        stage.dispose();
    }

    public void useCompass(boolean value) {
        utils.getSensorManager().useCompass(value);
    }

    public void canRotateCamera(boolean value) {
        canRotateCamera = value;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setCameraHeightOffset(float heightOffset) {
        earthCamera.setHeight(heightOffset);
    }
}
