package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import cz.vutbr.fit.xhalas10.bp.utils.FontGenerator;

import java.util.ArrayList;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class UserInterface {
    final private float animationSpeed = 0.3f;
    final private int fontSize = 50;
    private Stage stage;
    private ArrayList<Actor> actors = new ArrayList<Actor>();
    private Group mainMenu;
    private ImageButton settingsButton;
    private MyGdxGame myGdxGame;
    private SensorManager sensorManager;
    private ImageTextButton calibrationDoneButton;

    public UserInterface(final MyGdxGame myGdxGame) {
        this.stage = myGdxGame.stage;
        this.sensorManager = myGdxGame.sensorManager;
        this.myGdxGame = myGdxGame;


        calibrationDoneButton = createTextButton("Done");
        calibrationDoneButton.setSize(Gdx.graphics.getWidth() / 6.0f, Gdx.graphics.getHeight() / 6.0f);
        calibrationDoneButton.setPosition(Gdx.graphics.getWidth() - calibrationDoneButton.getWidth() * 1.2f, calibrationDoneButton.getHeight() * 0.2f);
        calibrationDoneButton.setVisible(false);
        calibrationDoneButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (hideActor(calibrationDoneButton)) {
                    myGdxGame.canRotateCamera(false);
                    showActor(settingsButton);
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return calibrationDoneButton.isVisible();
            }
        });

        actors.add(createSettingsButton());
        actors.add(calibrationDoneButton);
        actors.add(createMainMenu());

    }

    private boolean showActor(Actor actor) {
        if (!actor.isVisible()) {
            actor.getColor().a = 0;
            actor.setVisible(true);
            actor.addAction(fadeIn(animationSpeed));
            return true;
        }
        return false;
    }

    private boolean hideActor(final Actor actor) {
        if (actor.isVisible()) {
            actor.addAction(sequence(fadeOut(animationSpeed), run(new Runnable() {
                @Override
                public void run() {
                    actor.setVisible(false);
                }
            })));
            return true;
        }
        return false;
    }

    private Actor createSettingsButton() {
        settingsButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("settings-normal.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("settings-pressed.png")))));
        settingsButton.setSize(Gdx.graphics.getWidth() / 12.0f, Gdx.graphics.getWidth() / 12.0f);
        settingsButton.setPosition(Gdx.graphics.getWidth() - settingsButton.getWidth() * 1.2f, Gdx.graphics.getHeight() - settingsButton.getHeight() * 1.2f);
        settingsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (showActor(mainMenu))
                    hideActor(settingsButton);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return settingsButton.isVisible();
            }
        });
        return settingsButton;
    }

    private Actor createMainMenu() {
        mainMenu = new Group();
        mainMenu.setVisible(false);

        ImageButton testButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("settings-normal.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("settings-pressed.png")))));
        testButton.setPosition(Gdx.graphics.getWidth() / 2.0f - testButton.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f - testButton.getHeight() / 2.0f);

        Texture texture = generateColorTexture(new Color(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.8f));
        texture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion textureRegion = new TextureRegion(texture);

        Image background = new Image(textureRegion);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        background.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (hideActor(mainMenu))
                    showActor(settingsButton);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return mainMenu.isVisible();
            }
        });

        mainMenu.addActor(background);

        final ImageTextButton calibrateFromCompass = createTextButton("Calibrate from compass");
        calibrateFromCompass.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                myGdxGame.setAngle(-(-sensorManager.getAzimuth() + 2.0f * 90.0f) - 90.0f);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        final CheckBox useCompass = createCheckBox("Use Compass (not good indoors)");
        useCompass.getImageCell().size(100, 100);
        useCompass.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                sensorManager.useCompass(useCompass.isChecked());
                myGdxGame.setAngle(0);
                calibrateFromCompass.setVisible(!useCompass.isChecked());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        ImageTextButton calibrateCamera = createTextButton("Calibrate camera");
        calibrateCamera.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (hideActor(mainMenu)) {
                    myGdxGame.canRotateCamera(true);
                    showActor(calibrationDoneButton);
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });


        Table table = new Table();
        table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        table.add(useCompass).size(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 15.0f);
//        table.add(calibrateFromCompass).size(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 6.0f);
//        table.row();
        table.add(calibrateCamera).size(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 6.0f);
        mainMenu.addActor(table);

        return mainMenu;
    }

    private ImageTextButton createTextButton(String text) {
        Texture upTexture = generateColorTexture(Color.LIGHT_GRAY);
        upTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        Texture downTexture = generateColorTexture(Color.DARK_GRAY);
        downTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle(new TextureRegionDrawable(new TextureRegion(upTexture)), new TextureRegionDrawable(new TextureRegion(downTexture)), null, FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", fontSize));
        ImageTextButton button = new ImageTextButton(text, style);

        button.setSize(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 3.0f);
        return button;
    }

    private CheckBox createCheckBox(String text) {
        Texture upTexture = new Texture(Gdx.files.internal("blank-check-box.png"));
//        Texture upTexture = generateColorTexture(Color.LIGHT_GRAY);
//        upTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(new TextureRegion(upTexture));
        upDrawable.setBottomHeight(50);
        upDrawable.setLeftWidth(50);
        upDrawable.setMinHeight(50);
        upDrawable.setMinWidth(50);
        upDrawable.setRightWidth(50);
        upDrawable.setTopHeight(50);


        Texture downTexture = new Texture(Gdx.files.internal("check-box.png"));
//        Texture downTexture = generateColorTexture(Color.BLUE);
//        downTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(new TextureRegion(downTexture));
        downDrawable.setBottomHeight(50);
        downDrawable.setLeftWidth(50);
        downDrawable.setMinHeight(50);
        downDrawable.setMinWidth(50);
        downDrawable.setRightWidth(50);
        downDrawable.setTopHeight(50);

        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle(upDrawable, downDrawable, FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", fontSize), Color.LIGHT_GRAY);
        return new CheckBox(text, style);
    }

    public void setToStage() {
        for (Actor actor : actors) {
            stage.addActor(actor);
        }
    }

    public Texture generateColorTexture(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, color.a); // DE is red, AD is green and BE is blue.
        pix.fill();
        return new Texture(pix);
    }
}
