package com.group.game.dashdash;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import org.jetbrains.annotations.NotNull;

public class MenuFactory extends SceneFactory {

    @NotNull
    @Override
    public FXGLMenu newMainMenu() {
        // We will create this class in the next step
        return new MyMainMenu();
    }
}