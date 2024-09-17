package net.foxavis.kingdoms;

import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.*;

public class DynmapManager extends DynmapCommonAPIListener {

	public static final String DYNMAP_SET_ID = "foxiavs:kingdoms";
	public static final String TESTING_MARKER = "testingMarker";

	private static DynmapManager instance = null;
	public static DynmapManager getInstance() { return instance; }

	private DynmapCommonAPI dynmapAPI = null;

	private MarkerSet markerSet;
	private AreaMarker testingMarker;

	public DynmapManager() {
		instance = this;
	}

	@Override public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
		FoxavisKingdoms.getLoggerInstance().info("Dynmap instance enabled!");

		dynmapAPI = dynmapCommonAPI;

		markerSet = dynmapAPI.getMarkerAPI().getMarkerSet(DYNMAP_SET_ID);
		if(markerSet == null)
			markerSet = dynmapAPI.getMarkerAPI().createMarkerSet(DYNMAP_SET_ID, "Kingdoms", null, true);

		testingMarker = markerSet.findAreaMarker(TESTING_MARKER);
		if(testingMarker == null)
			testingMarker = markerSet.createAreaMarker(TESTING_MARKER, "Testing", false, "world", new double[] { 0, -5 }, new double[] { 0, -5 }, false);

		FoxavisKingdoms.getLoggerInstance().info("testing marker: " + testingMarker.toString());
	}

	public DynmapCommonAPI getDynmapAPI() { return dynmapAPI; }
	public MarkerSet getKingdomsMarkerSet() { return markerSet; }
	public AreaMarker getTestingMarker() { return testingMarker; }

	public static List<double[]> getPerimeterPoints(List<KingdomChunk> kingdomChunks) {
		Set<List<Integer>> edges = new LinkedHashSet<>();
		Set<KingdomChunk> chunkSet = new HashSet<>(kingdomChunks);

		for (KingdomChunk chunk : kingdomChunks) {
			int x = chunk.getX(), z = chunk.getZ();

			boolean hasNorth = chunkSet.contains(new KingdomChunk(chunk.getWorld(), x, z - 1));
			boolean hasSouth = chunkSet.contains(new KingdomChunk(chunk.getWorld(), x, z + 1));
			boolean hasWest  = chunkSet.contains(new KingdomChunk(chunk.getWorld(), x - 1, z));
			boolean hasEast  = chunkSet.contains(new KingdomChunk(chunk.getWorld(), x + 1, z));

			// Each edge is represented by the grid points it connects
			if (!hasNorth) {
				edges.add(Arrays.asList(x, z, x + 1, z));  // Top edge
			}
			if (!hasSouth) {
				edges.add(Arrays.asList(x, z + 1, x + 1, z + 1));  // Bottom edge
			}
			if (!hasWest) {
				edges.add(Arrays.asList(x, z, x, z + 1));  // Left edge
			}
			if (!hasEast) {
				edges.add(Arrays.asList(x + 1, z, x + 1, z + 1));  // Right edge
			}
		}

		// Convert edges to corner points and order them
		return tracePerimeter(edges);
	}

	private static List<double[]> tracePerimeter(Set<List<Integer>> edges) {
		List<double[]> perimeter = new ArrayList<>();
		Map<String, double[]> pointsMap = new HashMap<>();
		Set<List<Integer>> visitedEdges = new HashSet<>(); // Track visited edges to avoid loops

		for (List<Integer> edge : edges) {
			if (edge.size() != 4) continue; // Skip invalid edges

			int x1 = edge.get(0), z1 = edge.get(1), x2 = edge.get(2), z2 = edge.get(3);
			double[] startPoint = {x1 * 16.0, z1 * 16.0}, endPoint = {x2 * 16.0, z2 * 16.0};

			pointsMap.put(x1 + "," + z1, startPoint);
			pointsMap.put(x2 + "," + z2, endPoint);
		}

		if (pointsMap.isEmpty()) return perimeter; // Return empty if no valid points

		String currentPointKey = pointsMap.keySet().iterator().next();
		while (!pointsMap.isEmpty()) {
			double[] currentPoint = pointsMap.get(currentPointKey);
			perimeter.add(currentPoint);
			pointsMap.remove(currentPointKey);

			String nextPointKey = findNextPoint(currentPointKey, edges, visitedEdges);
			if (nextPointKey == null) break; // Exit loop if no next point is found
			currentPointKey = nextPointKey;
		}

		return perimeter;
	}

	private static String findNextPoint(String currentPoint, Set<List<Integer>> edges, Set<List<Integer>> visitedEdges) {
		String[] currentCoords = currentPoint.split(",");
		int x = Integer.parseInt(currentCoords[0]), z = Integer.parseInt(currentCoords[1]);

		for (List<Integer> edge : edges) {
			if (visitedEdges.contains(edge)) continue; // Skip already visited edges

			int x1 = edge.get(0), z1 = edge.get(1), x2 = edge.get(2), z2 = edge.get(3);

			if ((x1 == x && z1 == z) || (x2 == x && z2 == z)) {
				visitedEdges.add(edge); // Mark edge as visited

				if (x1 == x && z1 == z) return x2 + "," + z2;
				else return x1 + "," + z1;
			}
		}
		return null; // Return null if no valid next point is found
	}
}