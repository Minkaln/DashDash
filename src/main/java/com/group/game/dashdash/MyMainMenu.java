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

        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        getContentRoot().getChildren().add(bg);

        Text title = FXGL.getUIFactoryService().newText("DASH DASH", Color.WHITE, 60);
        title.setTranslateX(FXGL.getAppWidth() / 2.0 - 150);
        title.setTranslateY(150);
        getContentRoot().getChildren().add(title);

        // --- ENDLESS MODE BUTTON ---
        var btnEndless = FXGL.getUIFactoryService().newButton("ENDLESS MODE");
        btnEndless.setTranslateX(FXGL.getAppWidth() / 2.0 - 100);
        btnEndless.setTranslateY(300);
        btnEndless.setOnAction(e -> {
            FXGL.set("mode", GameMode.Endless); // Set the mocde
            fireNewGame();
        });

        // --- CLASSIC MODE (LEVELS) ---
        Text classicText = FXGL.getUIFactoryService().newText("CLASSIC LEVELS", Color.GRAY, 30);
        classicText.setTranslateX(FXGL.getAppWidth() / 2.0 - 100);
        classicText.setTranslateY(400);

        getContentRoot().getChildren().addAll(btnEndless, classicText);

        // Create 3 Level Buttons
        for (int i = 1; i <= 3; i++) {
            int levelNum = i;
            var btnLevel = FXGL.getUIFactoryService().newButton("Level " + levelNum);
            btnLevel.setTranslateX(FXGL.getAppWidth() / 2.0 - 250 + (i * 120));
            btnLevel.setTranslateY(450);

            btnLevel.setOnAction(e -> {
                FXGL.set("mode", GameMode.Classic); // Set mode to Classic
                FXGL.set("level", levelNum);        // Set the specific level
                fireNewGame();
            });

            getContentRoot().getChildren().add(btnLevel);
        }
    }
}