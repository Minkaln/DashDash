module com.group.game.dashdash {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens com.group.game.dashdash to javafx.fxml;
    exports com.group.game.dashdash;
}