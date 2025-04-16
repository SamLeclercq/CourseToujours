import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

public class GraphView extends Pane {
    private final Canvas canvas;
    private final GraphicsContext gc;
    public final GraphData data;

    private final Image entreeImage = new Image(getClass().getResourceAsStream("/images/entree.png"));
    private final Image caisseImage = new Image(getClass().getResourceAsStream("/images/caisse.png"));
    private final Image fruitImage = new Image(getClass().getResourceAsStream("/images/fruits.png"));
    private final Image legumeImage = new Image(getClass().getResourceAsStream("/images/legumes.png"));
    private final Image painImage = new Image(getClass().getResourceAsStream("/images/pain.png"));
    private final Image petitdejImage = new Image(getClass().getResourceAsStream("/images/petitdej.png"));
    private final Image poissonImage = new Image(getClass().getResourceAsStream("/images/poisson.png"));
    private final Image viandeImage = new Image(getClass().getResourceAsStream("/images/viande.png"));

    public GraphView(String jsonContent) {
        this.canvas = new Canvas(1000, 700);
        this.gc = canvas.getGraphicsContext2D();
        this.getChildren().add(canvas);
        this.data = new GraphData(jsonContent);

// GraphView background
        setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f5f5, #dcedc8);");


        widthProperty().addListener((obs, o, n) -> draw());
        heightProperty().addListener((obs, o, n) -> draw());

        draw();
    }

    public int runDijkstra(String startName, String endName, List<String> required) {
        data.clearHighlights();

        Node start = data.nodes.stream().filter(n -> n.name.equalsIgnoreCase(startName)).findFirst().orElse(null);
        Node end = data.nodes.stream().filter(n -> n.name.equalsIgnoreCase(endName)).findFirst().orElse(null);

        if (start == null || end == null) return -1;

        // Trouver les nœuds correspondants aux produits (par mot-clé partiel)
        List<Node> mustVisit = new ArrayList<>(data.nodes.stream()
                .filter(n -> required.stream().anyMatch(req -> n.name.toLowerCase().contains(req.toLowerCase())))
                .toList());


        List<Node> path = data.getPath(start, end, mustVisit);
        if (path == null || path.isEmpty()) return -1;

        data.highlightPath(path);
        int poids = data.calculateWeight(path);
        draw();
        return poids;
    }


    public void reset() {
        data.clearHighlights();
        draw();
    }

    private void draw() {
        double width = getWidth() > 0 ? getWidth() : 1000;
        double height = getHeight() > 0 ? getHeight() : 700;

        canvas.setWidth(width);
        canvas.setHeight(height);

        data.normalize(width, height);
        gc.clearRect(0, 0, width, height);
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));

        for (Edge edge : data.edges) {
            gc.setStroke(edge.highlighted ? Color.web("#FF7043") : Color.web("#4e5c68"));
            gc.setLineWidth(edge.highlighted ? 4 : 2);
            gc.strokeLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

            double midX = (edge.from.x + edge.to.x) / 2;
            double midY = (edge.from.y + edge.to.y) / 2 - 6;

            gc.setFill(Color.web("#2e2e2e"));  // Ã©tiquette sombre
            gc.fillRoundRect(midX - 12, midY - 10, 24, 16, 6, 6);
            gc.setFill(Color.web("#e0e0e0"));  // texte clair
            gc.fillText(String.valueOf(edge.weight), midX - 4, midY + 2);
        }

        for (Node node : data.nodes) {
            Image img;

            String name = node.name.toLowerCase();

            if (name.startsWith("entrée")) img = entreeImage;
            else if (name.startsWith("caisse")) img = caisseImage;
            else if (name.contains("fruit")) img = fruitImage;
            else if (name.contains("légume") || name.contains("legume")) img = legumeImage;
            else if (name.contains("pain")) img = painImage;
            else if (name.contains("petitdéj") || name.contains("petitdej")) img = petitdejImage;
            else if (name.contains("poisson")) img = poissonImage;
            else if (name.contains("viande")) img = viandeImage;
            else continue;

            double r = 30; // rayon du cercle (image centrée dans un 60x60)
            double cx = node.x;
            double cy = node.y;

            // cercle blanc derrière
            gc.setFill(Color.WHITE);
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);

            // bord
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

            // dessiner l'image au centre
            gc.drawImage(img, cx - 25, cy - 25, 50, 50); // image 50x50 au centre

            // texte seulement pour entrées/sorties
            if (name.startsWith("entrée") || name.startsWith("caisse")) {
                String label = node.name.replace("Entrée ", "A").replace("Caisse ", "C");
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                gc.fillText(label, cx - 6, cy + 5);
            }
        }


    }
}
