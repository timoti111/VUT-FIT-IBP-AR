/* Copyright (C) 2019 Timotej Halas (xhalas10).
 * This file is part of bachelor thesis.
 * Licensed under MIT.
 */

package cz.vutbr.fit.xhalas10.bp.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import cz.vutbr.fit.xhalas10.bp.utils.FontGenerator;

public class MySkin extends Skin {
    private static final int FONT_SIZE = 40;
    private static final MySkin ourInstance = new MySkin();
    private Color fontColorWhite = Color.WHITE;
    private Color fontColorDark = new Color(0.1411764705882353f, 0.1411764705882353f, 0.1411764705882353f, 1.0f);
    private Color up = Color.LIGHT_GRAY;
    private Color down = Color.DARK_GRAY;
    private Color disabled = Color.GRAY;

    private MySkin() {
        super();

        this.add("basicPoi", new Texture(Gdx.files.internal("pois/poi.png")));
        this.add("viewPoint", new Texture(Gdx.files.internal("pois/binoculars.png")));
        this.add("cave", new Texture(Gdx.files.internal("pois/cave.png")));
        this.add("cottage", new Texture(Gdx.files.internal("pois/cottage.png")));
        this.add("spring", new Texture(Gdx.files.internal("pois/drop.png")));
        this.add("information", new Texture(Gdx.files.internal("pois/information_board.png")));
        this.add("peak", new Texture(Gdx.files.internal("pois/peak.png")));
        this.add("signPost", new Texture(Gdx.files.internal("pois/sign_post.png")));
        this.add("waterfalls", new Texture(Gdx.files.internal("pois/waterfalls.png")));
        this.add("volcano", new Texture(Gdx.files.internal("pois/volcano.png")));
        this.add("rock", new Texture(Gdx.files.internal("pois/rock.png")));

        this.add("settings", new Texture(Gdx.files.internal("settings.png")));
        this.add("compass", new Texture(Gdx.files.internal("compass.png")));

        this.add("button", new NinePatch(new Texture(Gdx.files.internal("button.png")), 100, 100, 100, 100));
        this.add("line", new NinePatch(new Texture(Gdx.files.internal("line.png")), 0, 0, 15, 15));
        this.add("knob", new Texture(Gdx.files.internal("knob.png")));


        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.add("white", new Texture(pixmap));
        this.add("fontSize", FONT_SIZE);
        this.add("default", FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", FONT_SIZE, false));
        this.add("default-bold", FontGenerator.generateFont("fonts/OpenSans-Bold.ttf", FONT_SIZE, true));
        this.add("checkedCheckbox", new Texture(Gdx.files.internal("check-box.png")));
        this.add("uncheckedCheckbox", new Texture(Gdx.files.internal("blank-check-box.png")));

        ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        imageTextButtonStyle.up = this.newDrawable("button", this.up);
        imageTextButtonStyle.down = this.newDrawable("button", this.down);
        imageTextButtonStyle.font = this.getFont("default");
        imageTextButtonStyle.fontColor = fontColorDark;
        this.add("default", imageTextButtonStyle);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = this.newDrawable("white", this.up);
        textButtonStyle.down = this.newDrawable("white", this.down);
        textButtonStyle.font = this.getFont("default");
        this.add("default", textButtonStyle);

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOff = this.newDrawable("uncheckedCheckbox", fontColorDark);
        setDrawableSize(checkBoxStyle.checkboxOff);
        checkBoxStyle.checkboxOn = this.newDrawable("checkedCheckbox", fontColorDark);
        setDrawableSize(checkBoxStyle.checkboxOn);
        checkBoxStyle.checkboxOffDisabled = this.newDrawable("uncheckedCheckbox", this.disabled);
        setDrawableSize(checkBoxStyle.checkboxOffDisabled);
        checkBoxStyle.checkboxOnDisabled = this.newDrawable("checkedCheckbox", this.disabled);
        setDrawableSize(checkBoxStyle.checkboxOnDisabled);
        checkBoxStyle.fontColor = this.fontColorDark;
        checkBoxStyle.disabledFontColor = this.fontColorDark.cpy().mul(this.disabled);
        checkBoxStyle.font = this.getFont("default");
        this.add("default", checkBoxStyle);

        ImageButton.ImageButtonStyle settingsButton = new ImageButton.ImageButtonStyle();
        settingsButton.up = this.newDrawable("settings");
        settingsButton.down = this.newDrawable("settings", this.down);
        this.add("settingsButtonStyle", settingsButton);

        Label.LabelStyle labelStyle = new Label.LabelStyle(this.getFont("default"), fontColorDark);
        this.add("default", labelStyle);

        labelStyle = new Label.LabelStyle(this.getFont("default"), fontColorWhite);
        this.add("labelWhite", labelStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = this.newDrawable("line");
        sliderStyle.knob = this.newDrawable("knob", up);
        sliderStyle.knobDown = this.newDrawable("knob", down);
        this.add("default-vertical", sliderStyle);
    }

    public static MySkin getInstance() {
        return ourInstance;
    }

    private void setDrawableSize(Drawable drawable) {
        drawable.setBottomHeight(FONT_SIZE);
        drawable.setLeftWidth(FONT_SIZE);
        drawable.setMinHeight(FONT_SIZE);
        drawable.setMinWidth(FONT_SIZE);
        drawable.setRightWidth(FONT_SIZE);
        drawable.setTopHeight(FONT_SIZE);
    }
}
