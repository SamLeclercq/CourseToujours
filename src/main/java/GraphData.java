import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GraphData {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();
    public double minLon, maxLon, minLat, maxLat;

    public GraphData(String json) {
        parseGeoJSON(json);
    }

    private void parseGeoJSON(String content) {
        JSONObject geojson = new JSONObject(content);
        JSONArray features = geojson.getJSONArray("features");

        List<double[]> allCoords = new ArrayList<>();

        for (int i = 0; i < features.length(); i++) {
            JSONObject geom = features.getJSONObject(i).getJSONObject("geometry");
            if (geom.getString("type").equals("Point")) {
                JSONArray coords = geom.getJSONArray("coordinates");
                allCoords.add(new double[]{coords.getDouble(0), coords.getDouble(1)});
            } else if (geom.getString("type").equals("LineString")) {
                JSONArray coords = geom.getJSONArray("coordinates");
                for (int j = 0; j < coords.length(); j++) {
                    JSONArray point = coords.getJSONArray(j);
                    allCoords.add(new double[]{point.getDouble(0), point.getDouble(1)});
                }
            }
        }

        minLon = allCoords.stream().mapToDouble(c -> c[0]).min().orElse(0);
        maxLon = allCoords.stream().mapToDouble(c -> c[0]).max().orElse(1);
        minLat = allCoords.stream().mapToDouble(c -> c[1]).min().orElse(0);
        maxLat = allCoords.stream().mapToDouble(c -> c[1]).max().orElse(1);

        for (int i = 0; i < features.length(); i++) {
            JSONObject f = features.getJSONObject(i);
            JSONObject geom = f.getJSONObject("geometry");
            if (geom.getString("type").equals("Point")) {
                JSONArray coords = geom.getJSONArray("coordinates");
                String name = f.getJSONObject("properties").optString("name", "Node" + nodes.size());
                nodes.add(new Node(name, coords.getDouble(0), coords.getDouble(1)));
            }
        }

        for (int i = 0; i < features.length(); i++) {
            JSONObject f = features.getJSONObject(i);
            JSONObject geom = f.getJSONObject("geometry");
            if (geom.getString("type").equals("LineString")) {
                JSONArray coords = geom.getJSONArray("coordinates");
                int weight = f.getJSONObject("properties").optInt("weight", 1);
                for (int j = 0; j < coords.length() - 1; j++) {
                    Node from = findClosest(coords.getJSONArray(j));
                    Node to = findClosest(coords.getJSONArray(j + 1));
                    if (from != null && to != null) {
                        edges.add(new Edge(from, to, weight));
                    }
                }
            }
        }
    }

    private Node findClosest(JSONArray coord) {
        double lon = coord.getDouble(0);
        double lat = coord.getDouble(1);
        return nodes.stream()
                .min(Comparator.comparingDouble(n -> Math.hypot(n.lon - lon, n.lat - lat)))
                .orElse(null);
    }

    public void clearHighlights() {
        for (Edge e : edges) e.highlighted = false;
    }

    public void highlightPath(List<Node> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            for (Edge edge : edges) {
                if (edge.from == from && edge.to == to) {
                    edge.highlighted = true;
                    break;
                }
            }
        }
    }

    public int calculateWeight(List<Node> path) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            for (Edge edge : edges) {
                if (edge.from == from && edge.to == to) {
                    total += edge.weight;
                    break;
                }
            }
        }
        return total;
    }

    public void normalize(double width, double height) {
        double padding = 50;
        for (Node node : nodes) {
            double normX = (node.lon - minLon) / (maxLon - minLon);
            double normY = (node.lat - minLat) / (maxLat - minLat);
            node.x = padding + normX * (width - 2 * padding);
            node.y = padding + (1 - normY) * (height - 2 * padding);
        }
    }

    public List<Node> getPath(Node start, Node end, List<Node> mustVisit) {
        if (mustVisit.isEmpty()) {
            return dijkstra(start, end);
        }

        List<Node> bestPath = null;
        int bestCost = Integer.MAX_VALUE;

        // toutes les permutations de mustVisit
        List<List<Node>> permutations = new ArrayList<>();
        permute(new ArrayList<>(mustVisit), 0, permutations);

        for (List<Node> perm : permutations) {
            List<Node> currentPath = new ArrayList<>();
            int currentCost = 0;
            Node current = start;
            boolean failed = false;

            for (Node next : perm) {
                List<Node> segment = dijkstra(current, next);
                if (segment == null || segment.size() < 2) {
                    failed = true;
                    break;
                }
                if (!currentPath.isEmpty()) {
                    segment.remove(0); // éviter doublon
                }
                currentPath.addAll(segment);
                currentCost += calculateWeight(segment);
                current = next;
            }

            if (failed) continue;

            // dernier segment vers la sortie
            List<Node> segmentToEnd = dijkstra(current, end);
            if (segmentToEnd == null || segmentToEnd.size() < 2) {
                continue;
            }
            segmentToEnd.remove(0);
            currentPath.addAll(segmentToEnd);
            currentCost += calculateWeight(segmentToEnd);

            if (currentCost < bestCost) {
                bestCost = currentCost;
                bestPath = currentPath;
            }
        }

        return bestPath;
    }





    private void permute(List<Node> nodes, int i, List<List<Node>> result) {
        if (i == nodes.size()) {
            result.add(new ArrayList<>(nodes));
            return;
        }
        for (int j = i; j < nodes.size(); j++) {
            Collections.swap(nodes, i, j);
            permute(nodes, i + 1, result);
            Collections.swap(nodes, i, j);
        }
    }



    private List<Node> dijkstra(Node start, Node end) {
        Map<Node, Integer> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();
        Set<Node> unvisited = new HashSet<>(nodes);

        for (Node node : nodes) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);

        while (!unvisited.isEmpty()) {
            Node current = unvisited.stream().min(Comparator.comparingInt(dist::get)).orElse(null);
            if (current == null || current.equals(end)) break;

            unvisited.remove(current);

            for (Edge edge : edges) {
                // Sens normal
                if (edge.from == current && unvisited.contains(edge.to)) {
                    int alt = dist.get(current) + edge.weight;
                    if (alt < dist.get(edge.to)) {
                        dist.put(edge.to, alt);
                        prev.put(edge.to, current);
                    }
                }

                // Sens inverse
                if (edge.to == current && unvisited.contains(edge.from)) {
                    int alt = dist.get(current) + edge.weight;
                    if (alt < dist.get(edge.from)) {
                        dist.put(edge.from, alt);
                        prev.put(edge.from, current);
                    }
                }
            }
        }

        if (!prev.containsKey(end) && !start.equals(end)) return null;

        List<Node> path = new ArrayList<>();
        for (Node at = end; at != null; at = prev.get(at)) {
            path.add(0, at);
        }

        return path;
    }

}
