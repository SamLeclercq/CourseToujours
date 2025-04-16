import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProduitButton extends StackPane {
    private final String produitName;
    private boolean selected = false;
    private final Rectangle border;

    public ProduitButton(String produitName, Image image) {
        this.produitName = produitName;

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        border = new Rectangle(60, 60);
        border.setArcWidth(20);
        border.setArcHeight(20);
        border.setFill(Color.TRANSPARENT);
        border.setStrokeWidth(3);
        border.setStroke(Color.TRANSPARENT);

        setOnMouseClicked(e -> toggle());

        getChildren().addAll(border, imageView);
    }

    private void toggle() {
        selected = !selected;
        border.setStroke(selected ? Color.web("#4caf50") : Color.TRANSPARENT);
    }

    public boolean isSelected() {
        return selected;
    }

    public void deselect() {
        selected = false;
        border.setStroke(Color.TRANSPARENT);
    }

    public String getProduitName() {
        return produitName;
    }
}
