package com.group.game.dashdash;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MyMainMenu extends FXGLMenu {

    public MyMainMenu() {
        super(MenuType.MAIN_MENU);

        // 1. Add a background color or image
        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        getContentRoot().getChildren().add(bg);

        // 2. Add a Title
        Text title = FXGL.getUIFactoryService().newText("DASH DASH", Color.WHITE, 60);
        title.setTranslateX(FXGL.getAppWidth() / 2.0 - 150);
        title.setTranslateY(200);

        getContentRoot().getChildren().add(title);

        // 3. Add a "Start Game" button
        var btn = FXGL.getUIFactoryService().newButton("PLAY");
        btn.setTranslateX(FXGL.getAppWidth() / 2.0 - 50);
        btn.setTranslateY(400);

        // This is the magic line that starts the game
        btn.setOnAction(e -> fireNewGame());

        getContentRoot().getChildren().add(btn);
    }
}