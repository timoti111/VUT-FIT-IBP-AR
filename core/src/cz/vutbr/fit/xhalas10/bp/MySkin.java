package cz.vutbr.fit.xhalas10.bp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import cz.vutbr.fit.xhalas10.bp.utils.FontGenerator;

public class MySkin extends Skin {
    public static final int FONT_SIZE = 40;
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
        this.add("fontSize", FONT_SIZE);
        this.add("default", FontGenerator.generateFont("fonts/OpenSans-Regular.ttf", FONT_SIZE));
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
        checkBoxStyle.disabledFontColor = this.fontColor.cpy().mul(this.disabled);
        checkBoxStyle.font = this.getFont("default");
        this.add("default", checkBoxStyle);
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
