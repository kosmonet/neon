package neon.editor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;
import neon.system.files.NeonFileSystem;
import neon.system.logging.NeonLogFormatter;
import neon.system.resources.CreatureLoader;
import neon.system.resources.ItemLoader;
import neon.system.resources.RCreature;
import neon.system.resources.RItem;
import neon.system.resources.ResourceException;
import neon.system.resources.ResourceManager;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Editor extends Application {
	private static final Logger logger = Logger.getGlobal();
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("editor.fxml"));
			Scene scene = new Scene(root, 400, 400);
			scene.getStylesheets().add(getClass().getResource("editor.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			NeonFileSystem files = new NeonFileSystem("main", "mod1", "mod2", "mod3");
			files.setTemporaryFolder(Paths.get("temp"));
			files.setSaveFolder(Paths.get("saves", "save1"));
			
			ResourceManager manager = new ResourceManager(files);
			manager.addLoader("creature", new CreatureLoader());
			manager.addLoader("item", new ItemLoader());
			RCreature r1 = (RCreature)manager.getResource("creatures", "creature1");
			System.out.println(r1.getID());
			System.out.println(r1.getName());
			RItem r2 = new RItem("kwurp", "item");
			r2.setName("kwurpel");
			manager.addResource("items", r2);
		} catch (IOException | ResourceException e) {
			System.out.println("error: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		handler.setFormatter(new NeonLogFormatter());
		logger.addHandler(handler);
		launch(args);
	}
}
