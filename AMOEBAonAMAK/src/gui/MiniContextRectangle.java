package gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * A smaller version of a ContextRectangle, used for graphical list.
 * Used in {@link ContextExplorer}.
 * @author Hugo
 *
 */
public class MiniContextRectangle extends ContextRectangle {
	private StackPane stack;
	private Label label;
	private boolean activated;
	
	public MiniContextRectangle(ContextRectangle original) {
		super(0, 0, 60, 30, original.context);
		rectangle.setFill(original.rectangle.getFill());
		rectangle.setStyle(original.rectangle.getStyle());
		stack = new StackPane();
		label = new Label(context.getName());
		activated = false;
		rectangle.widthProperty().bind(label.widthProperty());
		rectangle.heightProperty().bind(label.heightProperty());
		stack.getChildren().addAll(label, rectangle);
	}
	
	@Override
	public Node getNode() {
		return stack;
	}
	
	@Override
	protected void onMouseClick(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			if(activated) {
				label.setText(context.getName());
				activated = false;
			} else {
				label.setText(context.toStringFull());
				activated = true;
			}
		}
	}
}
