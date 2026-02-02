module com.group.game.dashdash {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires java.desktop;


    opens com.group.game.dashdash to javafx.fxml, com.almasb.fxgl.core;

    opens assets.music to com.almasb.fxgl.all, com.almasb.fxgl.core;

    exports com.group.game.dashdash;
}