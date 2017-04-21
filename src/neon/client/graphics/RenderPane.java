package neon.client.graphics;

import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import neon.util.quadtree.RegionQuadTree;

/**
 * A {@code StackPane} for rendering the game world in multiple layers.
 * 
 * @author mdriesen
 *
 */
public class RenderPane extends StackPane {
	private final TextureFactory factory = new TextureFactory();
	private final HashMap<Integer, Canvas> layers = new HashMap<>();
	private RegionQuadTree<Color> terrain;
	private RegionQuadTree<Integer> depth;

	public RenderPane() {
		double parallax = 1.02;
		
		for (int i = -5; i < 4; i++) {
			Canvas canvas = new RenderCanvas();
			layers.put(i, canvas);
			getChildren().add(canvas);			
			canvas.setScaleX(Math.pow(parallax, i));
			canvas.setScaleY(Math.pow(parallax, i));
			canvas.widthProperty().bind(widthProperty());
			canvas.heightProperty().bind(heightProperty());
		}
		
		// TODO: brightness en/of saturation filters op elk canvas
	}
	
	public void setMap(RegionQuadTree<Color> terrain, RegionQuadTree<Integer> depth) {
		this.terrain = terrain;
		this.depth = depth;
	}
	
    public void drawShapes(int xpos, int ypos) {
    	for (Canvas canvas : layers.values()) {
    		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	}

    	for (int x = xpos; x < 100; x++) {
    		for (int y = ypos; y < 100; y++) {
    			GraphicsContext gc = layers.get(depth.get(x, y)).getGraphicsContext2D();
    			gc.setFill(Color.GREY);
   				gc.setFill(terrain.get(x, y));
    			gc.drawImage(factory.getImage(30, gc.getFill(), "g"), 30*(x - xpos), 30*(y - ypos), 30, 30);
    		}
    	}
    }
}
