import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    private ComboBox<String> entreeCombo;
    private ComboBox<String> sortieCombo;
    private Label poidsLabel;
    private GraphView graphView;
    private List<ProduitButton> produits;

    @Override
    public void start(Stage stage) {
        InputStream geoStream = getClass().getResourceAsStream("/graph.json");
        String json;
        try {
            assert geoStream != null;
            json = new String(geoStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement JSON", e);
        }

        graphView = new GraphView(json);
        graphView.setPrefSize(700, 500);

        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(150);
        rightPanel.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #e1f5fe);" +
                        "-fx-border-color: #b2ebf2;" +
                        "-fx-border-width: 0 0 0 2;" +
                        "-fx-border-style: solid;"
        );

        // Images pour les produits
        Image fruitImg = new Image(getClass().getResourceAsStream("/images/fruits.png"));
        Image legumeImg = new Image(getClass().getResourceAsStream("/images/legumes.png"));
        Image painImg = new Image(getClass().getResourceAsStream("/images/pain.png"));
        Image petitdejImg = new Image(getClass().getResourceAsStream("/images/petitdej.png"));
        Image poissonImg = new Image(getClass().getResourceAsStream("/images/poisson.png"));
        Image viandeImg = new Image(getClass().getResourceAsStream("/images/viande.png"));

        produits = List.of(
                new ProduitButton("Fruits", fruitImg),
                new ProduitButton("Légumes", legumeImg),
                new ProduitButton("Pain", painImg),
                new ProduitButton("PetitDéj", petitdejImg),
                new ProduitButton("Poisson", poissonImg),
                new ProduitButton("Viande", viandeImg)
        );

        VBox produitBox = new VBox(10);
        produitBox.getChildren().addAll(produits);
        rightPanel.getChildren().addAll(new Label("Produits :"), produitBox);

        entreeCombo = new ComboBox<>();
        entreeCombo.getItems().addAll("Entrée A", "Entrée B");

        sortieCombo = new ComboBox<>();
        sortieCombo.getItems().addAll("Caisse A", "Caisse B", "Caisse C");

        poidsLabel = new Label("Poids : -");

        Button goButton = new Button("▶");
        goButton.setOnAction(e -> lancerDijkstra());

        Button resetButton = new Button("🔄");
        resetButton.setOnAction(e -> reset());

        HBox bottom = new HBox(10,
                new Label("Entrée:"), entreeCombo,
                new Label("Sortie:"), sortieCombo,
                poidsLabel,
                goButton, resetButton
        );
        bottom.setStyle(
                "-fx-padding: 10;" +
                        "-fx-alignment: center;" +
                        "-fx-background-color: linear-gradient(to right, #ffffff, #fff3e0);" +
                        "-fx-border-color: #ffe0b2;" +
                        "-fx-border-width: 2 0 0 0;"
        );

        goButton.setStyle("-fx-background-color: #4dd0e1; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 20;");
        resetButton.setStyle("-fx-background-color: #ffab91; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 20;");

        BorderPane root = new BorderPane();
        root.setCenter(graphView);
        root.setRight(rightPanel);
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        stage.setTitle("Parcours Supermarché - Dijkstra");
        stage.setScene(scene);
        stage.show();
    }

    private void lancerDijkstra() {
        String entree = entreeCombo.getValue();
        String sortie = sortieCombo.getValue();
        if (entree == null || sortie == null) return;

        List<String> produitsChoisis = produits.stream()
                .filter(ProduitButton::isSelected)
                .map(ProduitButton::getProduitName)
                .collect(Collectors.toList());

        int poids = graphView.runDijkstra(entree, sortie, produitsChoisis);
        poidsLabel.setText("Poids : " + poids);
    }

    private void reset() {
        entreeCombo.getSelectionModel().clearSelection();
        sortieCombo.getSelectionModel().clearSelection();
        if (produits != null) {
            produits.forEach(ProduitButton::deselect);
        }
        graphView.reset();
        poidsLabel.setText("Poids : -");
    }

    public static void main(String[] args) {
        launch();
    }
}
