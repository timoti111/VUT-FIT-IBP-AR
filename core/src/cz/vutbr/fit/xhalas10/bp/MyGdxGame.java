package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;

import cz.vutbr.fit.xhalas10.bp.earth.Compass;
import cz.vutbr.fit.xhalas10.bp.earth.EarthCamera;
import cz.vutbr.fit.xhalas10.bp.earth.Poi;
import cz.vutbr.fit.xhalas10.bp.scene.WorldManager;
import cz.vutbr.fit.xhalas10.bp.utils.Location;

import java.util.Locale;

public class MyGdxGame extends ApplicationAdapter {
	private PerspectiveCamera cam;
	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;
	public CameraInputController camController;
	private HardwareCamera hardwareCamera;
    private Matrix4 cameraRotationMatrix;
    private Decal decal;
    private DecalBatch decalBatch;
    public SensorManager sensorManager;
    private float angle = 0;
    private final double TOUCH_SCALE_FACTOR = 63.0 / 1920.0;
    public Stage stage;
    private Label positionLabel;
    private Label accuracyLabel;
    private boolean canRotateCamera = false;
    private SpriteBatch spriteBatch;
    private Poi poiTest;
    private Sprite testSprite;

    private UserInterface userInterface;
    private PersonLocation personLocation;
    private GeoApiContext context;
    private WorldManager worldManager;
    EarthCamera earthCamera;

	public MyGdxGame(SensorManager sensorManager, HardwareCamera hardwareCamera, PersonLocation personLocation) {
		this.hardwareCamera = hardwareCamera;
		this.sensorManager = sensorManager;
		this.personLocation = personLocation;
        cameraRotationMatrix = new Matrix4();
	}

	public void canRotateCamera(boolean value) {
	    canRotateCamera = value;
    }

    public void setAngle(float angle) {
	    this.angle = angle;
    }

	@Override
	public void create () {
        context = new GeoApiContext.Builder()
            .apiKey("AIzaSyBQaK3OYcPfdtMaVZUbzjVLfegmOOc7K-E")
            .build();

	    OSMData.getInstance().setGeoApiContext(context);
        //OSMData.getInstance().checkNodeElevations();

        stage = new Stage(new ScreenViewport());
        userInterface = new UserInterface(this);
        userInterface.setToStage();
        spriteBatch = new SpriteBatch();

        BitmapFont font = MySkin.getInstance().getFont("default");
        Label.LabelStyle style = new Label.LabelStyle(font, Color.GREEN);
        positionLabel = new Label("Press a Button", style);
        positionLabel.setSize(Gdx.graphics.getWidth(), 60);
        positionLabel.setPosition(0, 0);
        positionLabel.setAlignment(Align.center);
        positionLabel.toBack();
        stage.addActor(positionLabel);

        accuracyLabel = new Label("Press a Button", style);
        accuracyLabel.setSize(Gdx.graphics.getWidth(), 60);
        accuracyLabel.setPosition(0, 60);
        accuracyLabel.setAlignment(Align.center);
        accuracyLabel.toBack();
        stage.addActor(accuracyLabel);

        Gdx.graphics.setContinuousRendering(false);

        hardwareCamera.init();
		modelBatch = new ModelBatch();

		double ratio = (double)Gdx.graphics.getWidth() / (double)Gdx.graphics.getHeight();
		double fovy = Math.toDegrees(2.0 * Math.atan((hardwareCamera.getCameraSensorSize()[0] * (1.0 / ratio)) / (2.0 * (double)hardwareCamera.getCameraFocalLength())));
		cam = new PerspectiveCamera((float)fovy, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.01f;
		cam.far = 500f;
		cam.update();
        worldManager = WorldManager.getInstance();
        earthCamera = new EarthCamera(cam);
        worldManager.setWorldCamera(earthCamera);

        Texture texture = new Texture(Gdx.files.internal("compass_grey2icent.png"));
        TextureRegion textureRegion = new TextureRegion(texture);
        decal = Decal.newDecal(textureRegion, true);
        decal.setWidth(1.0f);
        decal.setHeight(1.0f);
        decal.setPosition(0.0f, -1.5f, 0.0f);
        decal.lookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f));
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));

        Location location = new Location(49.230795, 16.568154, 311.0);
        Vector3 up = location.getUpPointingVector();
        Vector3 north = location.getNorthPointingVector().scl(-1);
        Vector3 east = location.getEastPointingVector();
        Matrix4 mat = new Matrix4();
        mat.set(east.cpy(), up, north.cpy(), Vector3.Zero);
        Vector3 upTra = new Vector3(Vector3.Y);
        Vector3 northTra = new Vector3(Vector3.Z);
        Vector3 eastTra = new Vector3(Vector3.X);
        up.mul(mat);
        north.mul(mat);
        east.mul(mat);
        //
//        instance = new ModelInstance(model);
//        Poi poi1 = new Poi("Chatka 1", cam);
//        Poi poi2 = new Poi("Chatka 2", cam);
//        Poi poi3 = new Poi("Chatka 3", cam);
//        Poi poi4 = new Poi("Chatka 4", cam);//
//        earth.add(instance, 49.230795, 16.568154, 311.0);
//        earth.add(poi1, 49.231114, 16.568264, 311.0);
//        earth.add(poi2, 49.231254, 16.568456, 307.0);
//        earth.add(poi3, 49.230565, 16.568028, 312.0);
//        earth.add(poi4, 49.230735, 16.568519, 303.0);
//        Poi kopecBazenik = new Poi("Velký kopec", cam);
//        Poi budkaZaA02 = new Poi("Búdka", cam);
//        Poi krizovatkaNaKopci = new Poi("Križovatka", cam);
//        Poi budkaPodKopcom = new Poi("Rozvodňa", cam);
//        Poi vchodA05 = new Poi("Vchod A05", cam);
//        Poi vchodA02 = new Poi("Vchod A02", cam);
//        earth.add(kopecBazenik, 49.234399, 16.567903, 336.0);
//        earth.add(budkaZaA02, 49.232329, 16.569171, 286.0);
//        earth.add(krizovatkaNaKopci, 49.233315, 16.567080, 315.0);
//        earth.add(budkaPodKopcom, 49.235859, 16.568215, 310.0);
//        earth.add(vchodA05, 49.231937, 16.570472, 280.0);
//        earth.add(vchodA02, 49.231666, 16.570102, 284.0);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            private double mPreviousX = 0;
            private double mPreviousY = 0;

            @Override
            public boolean touchDown (int x, int y, int pointer, int button) {
                mPreviousX = x;
                mPreviousY = y;
                return true; // return true to indicate the event was handled
            }

            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
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


        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        Compass compass = new Compass(cam);
        worldManager.addWorldObject(compass);

        for (OSMNode node : OSMData.getInstance().getOSMNodes()) {
            if (node.hasElevation())
                worldManager.addWorldObject(new Poi(node.getName(), node.getLocation().lat, node.getLocation().lng, node.getElevation()));
        }
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cam.direction.set(0.0f, 0.0f, -1.0f);
        cam.up.set(0.0f, 1.0f, 0.0f);
        cam.rotate(sensorManager.getQuaternion());
        cam.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
        cam.rotate(angle - 90.0f, 0.0f, 1.0f, 0.0f);
        cam.update();

		hardwareCamera.renderBackground();

        updateLocation();
        worldManager.renderWorld();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
	}
	
	@Override
	public void dispose () {
		model.dispose();
		hardwareCamera.destroy();
	}

	public void downloadSurroundingData() {
        OSMData.getInstance().getSurroundingData(new LatLng(personLocation.getLatitude(), personLocation.getLongitude()), 0.005);
        //earth.set(OSMData.getInstance().getOSMNodes());
    }

    public void updateLocation() {
	    if (!earthCamera.isSameLocation(personLocation.getLatitude(), personLocation.getLongitude(), personLocation.getAltitude())) {
            earthCamera.setPosition(personLocation.getLatitude(), personLocation.getLongitude(), personLocation.getAltitude());
            earthCamera.update();
            worldManager.updateObjectsAndCache();
            positionLabel.setText(String.format(Locale.getDefault(), "Lat: %.6f   Lon: %.6f   Alt: %.1f",
                    personLocation.getLatitude(),
                    personLocation.getLongitude(),
                    personLocation.getAltitude()));

            accuracyLabel.setText(String.format(Locale.getDefault(), "Nodes: %d   hAc: %.1f   vAc: %.1f",
                    OSMData.getInstance().getOSMNodes().size(),
                    personLocation.getVerticalAccuracy(),
                    personLocation.getHorizontalAccuracy()));
        }
    }
}
