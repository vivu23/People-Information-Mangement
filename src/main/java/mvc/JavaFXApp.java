package mvc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvc.screens.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApp extends Application {

    private static final Logger LOGGER = LogManager.getLogger();

    private ConfigurableApplicationContext applicationContext;
    @Override
    public void init() throws Exception{
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                    .sources(AppMain.class)
                    .run(args);
        super.init();
        LOGGER.info("in init");
    }

    @Override
    public void stop() throws Exception {
        this.applicationContext.close();
        Platform.exit();
        super.stop();
        LOGGER.info("in stop");
    }

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("before start");

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/main_view.fxml"));
        loader.setController(MainController.getInstance());
        Parent rootNode = loader.load();
        stage.setScene(new Scene(rootNode));

        stage.setTitle("CS 4743 Fall 2021");
        stage.show();
        LOGGER.info("after start");
    }
}
