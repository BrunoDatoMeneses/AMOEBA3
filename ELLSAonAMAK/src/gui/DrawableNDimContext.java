package gui;

import agents.context.Context;
import multiagent.framework.ui.drawables.Drawable;
import multiagent.framework.ui.drawables.DrawableDefaultMini;
import multiagent.framework.ui.drawables.DrawableNDimRectangle;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * Drawable for contexts agents, include its mini drawable.
 * @author Hugo
 *
 */
public class DrawableNDimContext extends DrawableNDimRectangle {

	/**
	 * Mini drawable for context. Add a "destroy" button.
	 * @author Hugo
	 *
	 */
	public class DrawableContextMini extends DrawableDefaultMini {

		private HBox top;

		public DrawableContextMini(Drawable original, Context context) {
			super(original);

			top = new HBox();
			top.setAlignment(Pos.BASELINE_RIGHT);
			Button destroyButton = new Button("Destroy");
			destroyButton.setOnAction(e -> {
				context.destroy();
			});
			top.getChildren().add(destroyButton);
			top.setVisible(false);
			top.setManaged(false);
			label.setCenterShape(true);
			stack.getChildren().add(top);
		}

		@Override
		public void expand() {
			top.setVisible(true);
			top.setManaged(true);
			super.expand();
		}

		@Override
		public void collapse() {
			top.setVisible(false);
			top.setManaged(false);
			super.collapse();
		}

	}

	public DrawableNDimContext(int dimensions, Context context) {
		super(dimensions);
		new DrawableContextMini(this, context);
	}

}
