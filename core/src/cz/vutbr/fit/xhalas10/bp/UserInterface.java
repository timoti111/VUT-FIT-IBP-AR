package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import cz.vutbr.fit.xhalas10.bp.utils.FontGenerator;
import cz.vutbr.fit.xhalas10.bp.utils.GeoidCalculator;

import java.util.ArrayList;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class UserInterface {
    final private float animationSpeed = 0.3f;
    private Stage stage;
    private ArrayList<Actor> actors = new ArrayList<Actor>();
    private Group mainMenu;
    private ImageButton settingsButton;
    private MyGdxGame myGdxGame;
    private SensorManager sensorManager;
    private TextButton calibrationDoneButton;

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

        final CheckBox useCompass = new CheckBox("Use Compass (not good indoors)", MySkin.getInstance());
        useCompass.getImageCell().size(100, 100);
        useCompass.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                sensorManager.useCompass(useCompass.isChecked());
                myGdxGame.setAngle(0);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        useCompass.setChecked(true);
        sensorManager.useCompass(true);

        TextButton calibrateCamera = createTextButton("Calibrate camera");
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

        TextButton downloadSurroundingData = createTextButton("Download Surrounding Data");
        downloadSurroundingData.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (hideActor(mainMenu)) {
                    myGdxGame.downloadSurroundingData();
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });


        Table table = new Table();
        table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        table.add(useCompass).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 15.0f);
        table.add(calibrateCamera).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 15.0f);
        table.add(downloadSurroundingData).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        mainMenu.addActor(table);

        return mainMenu;
    }

    private TextButton createTextButton(String text) {
        TextButton button = new TextButton(text, MySkin.getInstance());
        button.setSize(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 3.0f);
        return button;
    }

    public void setToStage() {
        for (Actor actor : actors) {
            stage.addActor(actor);
        }
    }

    public Texture generateColorTexture(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, color.a);
        pix.fill();
        return new Texture(pix);
    }
}


class MySkin extends Skin {
    private final int fontSize = 50;
    private Color fontColor = Color.WHITE;
    private Color up = Color.LIGHT_GRAY;
    private Color down = Color.DARK_GRAY;
    private Color disabled = Color.GRAY;

    private static final MySkin ourInstance = new MySkin();
    public static MySkin getInstance() {
        return ourInstance;
    }
    private MySkin() {
        super();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.add("white", new Texture(pixmap));
        this.add("default", FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", this.fontSize));
        this.add("checkedCheckbox", new Texture(Gdx.files.internal("check-box.png")));
        this.add("uncheckedCheckbox", new Texture(Gdx.files.internal("blank-check-box.png")));

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = this.newDrawable("white", this.up);
        textButtonStyle.down = this.newDrawable("white", this.down);
        textButtonStyle.font = this.getFont("default");
        this.add("default", textButtonStyle);

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOff = this.newDrawable("uncheckedCheckbox");
        setDrawableSize(checkBoxStyle.checkboxOff);
        checkBoxStyle.checkboxOn = this.newDrawable("checkedCheckbox");
        setDrawableSize(checkBoxStyle.checkboxOn);
        checkBoxStyle.checkboxOffDisabled = this.newDrawable("uncheckedCheckbox", this.disabled);
        setDrawableSize(checkBoxStyle.checkboxOffDisabled);
        checkBoxStyle.checkboxOnDisabled = this.newDrawable("checkedCheckbox", this.disabled);
        setDrawableSize(checkBoxStyle.checkboxOnDisabled);
        checkBoxStyle.fontColor = this.fontColor;
        checkBoxStyle.disabledFontColor = this.fontColor.mul(this.disabled);
        checkBoxStyle.font = this.getFont("default");
        this.add("default", checkBoxStyle);
    }

    private void setDrawableSize(Drawable drawable) {
        drawable.setBottomHeight(fontSize);
        drawable.setLeftWidth(fontSize);
        drawable.setMinHeight(fontSize);
        drawable.setMinWidth(fontSize);
        drawable.setRightWidth(fontSize);
        drawable.setTopHeight(fontSize);
    }
}
