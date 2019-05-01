package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class UserInterface {
    final private float animationSpeed = 0.3f;
    private Stage stage;
    private Array<Actor> actors = new Array<Actor>();
    private Group mainMenu;
    private Group legend;
    private ImageButton settingsButton;
    private ARNature ARNature;
    private ImageTextButton calibrationDoneButton;
    private HorizontalGroup calibrationSlider;

    public UserInterface(final ARNature ARNature) {
        this.stage = ARNature.stage;
        this.ARNature = ARNature;

        actors.add(createCalibrationDoneButton());
        actors.add(createMainMenu());
        actors.add(createLegend());
        actors.add(createHeightCalibrationSlider());
    }

    private void moveActor(Actor actor, float fromX, float fromY, float toX, float toY) {
        actor.setPosition(fromX, fromY);
        actor.addAction(moveTo(toX, toY, animationSpeed));
    }

    private void moveActor(Actor actor, float toX, float toY) {
        actor.addAction(moveTo(toX, toY, animationSpeed));
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

    private ImageTextButton createImageTextButton(String text) {
        ImageTextButton button = new ImageTextButton(text.toUpperCase(), MySkin.getInstance());
        button.setSize(Gdx.graphics.getWidth() / 3.0f, Gdx.graphics.getHeight() / 3.0f);
        return button;
    }

    private static float offsetValue = 0.0f;
    private static float lastValue = 0.0f;

    private HorizontalGroup createHeightCalibrationSlider() {
        calibrationSlider = new HorizontalGroup();
        final Slider slider = new Slider(-15.0f, 15.0f, 0.5f, true, MySkin.getInstance());
        slider.setValue(0.0f);
        Group group = new Group();
        group.addActor(slider);
        group.setHeight(Gdx.graphics.getHeight() / 2.0f);
        group.setWidth(slider.getWidth());

        final Label label = new Label(Float.toString(slider.getValue()) + "m", MySkin.getInstance(), "labelWhite");

        slider.setHeight(Gdx.graphics.getHeight() / 2.0f);
        calibrationSlider.addListener(new InputListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                lastValue = 0.0f;
                slider.setValue(0.0f);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                offsetValue += lastValue - slider.getValue();
                lastValue = slider.getValue();
                label.setText(Float.toString(offsetValue) + "m");
                ARNature.setCameraHeightOffset(offsetValue);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        calibrationSlider.addActor(group);
        calibrationSlider.addActor(label);
        calibrationSlider.expand(true);
        calibrationSlider.rowRight();
        calibrationSlider.pad(15);
        calibrationSlider.space(30);
        calibrationSlider.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        calibrationSlider.setVisible(false);
        return calibrationSlider;
    }

    private ImageTextButton createCalibrationDoneButton() {
        calibrationDoneButton = createImageTextButton("Done");
        calibrationDoneButton.setSize(Gdx.graphics.getWidth() / 6.0f, Gdx.graphics.getHeight() / 6.0f);
        calibrationDoneButton.setPosition(Gdx.graphics.getWidth() - calibrationDoneButton.getWidth() * 1.2f, calibrationDoneButton.getHeight() * 0.2f);
        calibrationDoneButton.setVisible(false);
        calibrationDoneButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                hideActor(calibrationDoneButton);
                hideActor(calibrationSlider);
                ARNature.canRotateCamera(false);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return calibrationDoneButton.isVisible();
            }
        });
        return calibrationDoneButton;
    }


    private ImageButton createSettingsButton() {
        Texture texture = MySkin.getInstance().get("settings", Texture.class);
        float width = Gdx.graphics.getWidth() / 12.0f;
        float height = width * texture.getHeight() / texture.getWidth();
        settingsButton = new ImageButton(MySkin.getInstance(), "settingsButtonStyle");
        settingsButton.setSize(width, height);
        settingsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                moveActor(mainMenu, Gdx.graphics.getWidth(), 0.0f, 0.0f, 0.0f);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        return settingsButton;
    }

    private Actor createMainMenu() {
        mainMenu = new Group();

        Image background = new Image(MySkin.getInstance().newDrawable("white", new Color(1.0f, 1.0f, 1.0f, 0.80480478f)));
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        background.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                moveActor(mainMenu, Gdx.graphics.getWidth(), 0.0f);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        final CheckBox useCompass = new CheckBox("Use Compass (not good indoors)", MySkin.getInstance());
        useCompass.getImageCell().size(100, 100);
        useCompass.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                ARNature.useCompass(useCompass.isChecked());
                ARNature.setAngle(0);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        useCompass.setChecked(true);
        ARNature.useCompass(true);

        ImageTextButton calibrateCamera = createImageTextButton("Calibrate camera");
        calibrateCamera.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                showActor(calibrationDoneButton);
                showActor(calibrationSlider);
                moveActor(mainMenu, Gdx.graphics.getWidth(), 0.0f);
                ARNature.canRotateCamera(true);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        ImageTextButton downloadSurroundingData = createImageTextButton("Download Surrounding Data");
        downloadSurroundingData.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                moveActor(mainMenu, Gdx.graphics.getWidth(), 0.0f);
                ARNature.downloadSurroundingData();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        ImageTextButton showLegend = createImageTextButton("Show Legend");
        showLegend.addListener(new InputListener() {
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (hideActor(mainMenu))
                    showActor(legend);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        Table table = new Table();
        table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        table.add(useCompass).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 30.0f);
        table.add(calibrateCamera).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 30.0f);
        table.add(downloadSurroundingData).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);
        table.row().pad(Gdx.graphics.getHeight() / 30.0f);
        table.add(showLegend).size(Gdx.graphics.getWidth() / 2.7f, Gdx.graphics.getHeight() / 6.0f);

        mainMenu.addActor(background);
        mainMenu.addActor(table);
        mainMenu.addActor(createSettingsButton());
        settingsButton.setPosition(-settingsButton.getWidth(), Gdx.graphics.getHeight() - settingsButton.getHeight() * 1.2f);
        mainMenu.setPosition(Gdx.graphics.getWidth(), 0.0f);
        return mainMenu;
    }

    public void setToStage() {
        for (Actor actor : actors) {
            stage.addActor(actor);
        }
    }

    public Group createLegend() {
        legend = new Group();
        legend.setVisible(false);
        Image background = new Image(MySkin.getInstance().newDrawable("white", new Color(1.0f, 1.0f, 1.0f, 0.80480478f)));
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        legend.addActor(background);

        VerticalGroup verticalGroup = new VerticalGroup();
        verticalGroup.setWidth(Gdx.graphics.getWidth());
        verticalGroup.columnLeft();
        verticalGroup.addActor(createLegendItem("peak", "Peak"));
        verticalGroup.addActor(createLegendItem("volcano", "Volcano"));
        verticalGroup.addActor(createLegendItem("viewPoint", "View Point"));
        verticalGroup.addActor(createLegendItem("information", "Information Board"));
        verticalGroup.addActor(createLegendItem("signPost", "Guide Post"));
        verticalGroup.addActor(createLegendItem("cave", "Cave"));
        verticalGroup.addActor(createLegendItem("cottage", "Cottage"));
        verticalGroup.addActor(createLegendItem("spring", "Spring"));
        verticalGroup.addActor(createLegendItem("rock", "Rock"));
        verticalGroup.addActor(createLegendItem("waterfalls", "Waterfalls"));
        verticalGroup.addActor(createLegendItem("basicPoi", "Other"));

        ScrollPane scrollPane = new ScrollPane(verticalGroup);
        scrollPane.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        legend.addActor(scrollPane);
        legend.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (event.isHandled()) {
                    if (hideActor(legend))
                        showActor(mainMenu);
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        return legend;
    }

    private HorizontalGroup createLegendItem(String texture, String text) {
        Image image = new Image(MySkin.getInstance().get(texture, Texture.class));
        Label label = new Label(text.toUpperCase(), MySkin.getInstance());
        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(image);
        horizontalGroup.addActor(label);
        horizontalGroup.pad(15);
        horizontalGroup.space(30);
        return horizontalGroup;
    }
}


