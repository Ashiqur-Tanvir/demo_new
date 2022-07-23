package tud.ai2.pacman.model.level;

import tud.ai2.pacman.util.Consts;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Generiert einen zufaelligen Level.
 *
 * @author Simon Breitfelder
 * @author Dominik Puellen
 * @author Robert Cieslinski
 * @author Kurt Cieslinski
 */
public class LevelGenerator {
	/** Ein Random-Generator */
	private final Random rnd;

	/**
	 * WARNUNG Nicht manuell veraendern! Diese beiden Variablen werden bereits
	 * zufaellig initialisiert.
	 *
	 * Zeigen Hoehe und Breite des generierten Levels an
	 */
	private int width, height;
	/** Die aktuelle Anzahl von Dots */
	private int free;
	/** aktueller Level-Grid */
	private MapModule[][] map;
	/** die aktuellen Pacman-Spawner */
	private List<Point> pacmanSpawns;
	/** die aktuellen Geister-Spawner */
	private List<Point> ghostSpawns;

	/**
	 * Konstruktor.
	 */
	public LevelGenerator() {
		rnd = new Random();
	}

	/**
	 * Aktualisiert einen Level-Baustein.
	 *
	 * @param x X-Koordinate
	 * @param y Y-Koordinate
	 * @param m neues Modul
	 */
	private void setMapAt(int x, int y, MapModule m) {
		map[y][x] = m;
	}

	/**
	 * @param x X-Koordinate
	 * @param y Y-Koordinate
	 * @return Level-Modul an der gewaehlten Position
	 */
	private MapModule getMapAt(int x, int y) {
		return map[y][x];
	}

	/**
	 * @param min minimale Zahl
	 * @param max maximale Zahl
	 * @return eine Zufahlszahl zwischen min und max
	 */
	private int rndInt(int min, int max) {
		if (min >= max)
			return min;
		return rnd.nextInt(max - min) + min;
	}

	/**
	 *
	 *
	 * @return einen zufaellig generierten Level
	 */
	public Level generateLevel() {
		// levelabmessungen zufaellig bestimmen (nur ungerade zahlen)
		width = rndInt(Consts.WIDTH_MIN, Consts.WIDTH_MAX) * 2 + 1;
		height = rndInt(Consts.HEIGHT_MIN, Consts.HEIGHT_MAX) * 2 + 1;
		free = 0;

		// level mit waenden initialisieren
		map = new MapModule[height][width];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				setMapAt(x, y, MapModule.WALL);

		pacmanSpawns = new ArrayList<>();
		ghostSpawns = new ArrayList<>();

		// landschaft generieren
		genMaze();
		removeDeadEnds();
		for (int i = 0; i < rndInt(1, free / 15); i++)
			genOpenBorder();

		// spawner platzieren
		for (int i = 0; i < rndInt(1, free / 18); i++)
			genPacmanSpawner();
		for (int i = 0; i < 4; i++)
			genGhostSpawner();

		// items platzieren
		for (int i = 0; i < free / 15; i++)
			genRandomModule(MapModule.POWERUP);
		for (int i = 0; i < rndInt(1, free / 18); i++)
			genRandomModule(MapModule.SPEEDUP);
		if (rnd.nextInt(3) == 0)
			for (int i = 0; i < rndInt(1, free / 20); i++)
				genRandomModule(MapModule.TELEPORT);

		return new Level("Random Level", map, pacmanSpawns.toArray(new Point[0]), ghostSpawns.toArray(new Point[0]));
	}

	/**
	 * Rekursions-Initialisierung. Starte mit einem zufaelligen Punkt.
	 */
	private void genMaze() {
		genMaze(new Point(rnd.nextInt((width - 1) / 2) * 2 + 1, rnd.nextInt((height - 1) / 2) * 2 + 1));
	}

	/**
	 * @param p der Startpunkt der Pfadgeneration
	 */
	// TODO task 3b
	private void genMaze(Point p) {
		if (p == null)
			throw new IllegalArgumentException();

		if (getMapAt(p.x, p.y) != MapModule.DOT) {
			setMapAt(p.x, p.y, MapModule.DOT);
			free++;
		}

		// Erzeuge eine Liste der Nachbaren
		List<Point> neighbours_2 = get2Neighbours(p);

		// Rekursionsanker
		if (neighbours_2.isEmpty()) {
			return;
		}

		// Wähle einen zufälligen Punkt von der Liste der Nachbaren und setzte Dot an
		// der Stelle dazwischen
		Point n = neighbours_2.get(rnd.nextInt(neighbours_2.size()));

		if (n.x == p.x + 2) {
			setMapAt(p.x + 1, p.y, MapModule.DOT);
			free++;
		} else if (n.x == p.x - 2) {
			setMapAt(p.x - 1, p.y, MapModule.DOT);
			free++;
		} else if (n.y == p.y + 2) {
			setMapAt(p.x, p.y + 1, MapModule.DOT);
			free++;
		} else if (n.y == p.y - 2) {
			setMapAt(p.x, p.y - 1, MapModule.DOT);
			free++;
		}

		genMaze(n);
		genMaze(p);
	}

	/**
	 * Gibt alle Punkte mit gerader Entfernung 2 zum Ursprungspunkt zurueck.
	 * Herausgefiltert werden Punkte, die am Rand liegen oder mehr als einen Dot als
	 * direkten Nachbarn besitzen.
	 *
	 * @param p Ursprungspunkt
	 */
	private List<Point> get2Neighbours(Point p) {
		List<Point> neighbours = new LinkedList<>();

		if (getMapAt(p.x - 1, p.y) != MapModule.DOT)
			neighbours.add(new Point(p.x - 2, p.y));
		if (getMapAt(p.x + 1, p.y) != MapModule.DOT)
			neighbours.add(new Point(p.x + 2, p.y));
		if (getMapAt(p.x, p.y - 1) != MapModule.DOT)
			neighbours.add(new Point(p.x, p.y - 2));
		if (getMapAt(p.x, p.y + 1) != MapModule.DOT)
			neighbours.add(new Point(p.x, p.y + 2));

		neighbours.removeIf(point -> !isTarget(point));

		return neighbours;
	}

	/**
	 * @param p ein Punkt
	 * @return true <-> nicht am Rand und weniger als einem Dot als Nachbar
	 */
	private boolean isTarget(Point p) {
		// Punkte direkt am Rand werden hier noch nicht betrachtet
		if (p.x < 1 || p.x >= (width - 1) || p.y < 1 || p.y >= (height - 1))
			return false;

		// Punkte mit mehr als einem Dot als direkte Nachbarn werden nicht betrachtet
		int sum = 0;
		for (int y = (p.y - 1); y <= (p.y + 1); y++)
			for (int x = (p.x - 1); x <= (p.x + 1); x++)
				if (getMapAt(x, y) == MapModule.DOT)
					sum++;
		return sum <= 1;
	}

	// TODO task 3c
	/**
	 * Vermeidet die Sackgassen (außer am Rand)
	 */
	private void removeDeadEnds() {
		// Für jeden Punkt des Rasters: wenn nicht die Wand, finde die Nachbarwände und
		// ersetze eine durch den DOT wenn mehr als 2 Nachbarwände vorhanden
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (getMapAt(x, y) != MapModule.WALL) {
					List<Point> walls = getSurroundingWalls(x, y);
					if (walls.size() > 2) {
						Point change = walls.get(rnd.nextInt(walls.size()));
						setMapAt(change.x, change.y, MapModule.DOT);
						free++;
					}
				}
			}
		}
	}

	/**
	 * Gibt alle Koordinaten der umgebenen Waende zurueck, wenn diese NICHT am Rand
	 * des Levels liegen.
	 *
	 * @param x X-Koordinate
	 * @param y Y-Koordinate
	 * @return alle Koordinaten der umgebenden Waende
	 */
	private List<Point> getSurroundingWalls(int x, int y) {
		List<Point> walls = new ArrayList<>();

		if (x > 1 && getMapAt(x - 1, y) == MapModule.WALL)
			walls.add(new Point(x - 1, y));
		if (x < (width - 2) && getMapAt(x + 1, y) == MapModule.WALL)
			walls.add(new Point(x + 1, y));
		if (y > 1 && getMapAt(x, y - 1) == MapModule.WALL)
			walls.add(new Point(x, y - 1));
		if (y < (height - 2) && getMapAt(x, y + 1) == MapModule.WALL)
			walls.add(new Point(x, y + 1));

		return walls;
	}

	/**
	 * Generiert einen zufaelligen Pacman-Spawner. Dieser muss am weitesten von
	 * anderen Pacman-Spawnern entfernt sein.
	 */
	private void genPacmanSpawner() {
		// platziert einen pacman spawner
		Point p;

		if (pacmanSpawns.size() == 0) {
			// ersten spawner zufaellig platzieren
			do {
				p = new Point(rnd.nextInt(width), rnd.nextInt(height));
			} while (getMapAt(p.x, p.y) != MapModule.DOT);

		} else
			p = furthestFromPacSpawn();

		if (p != null) {
			pacmanSpawns.add(p);
			setMapAt(p.x, p.y, MapModule.PLAYER_SPAWN);
		}
	}

	/**
	 * Generiert einen zufaelligen Geister-Spawner. Dieser muss am weitesten von
	 * Pacman-Spawnern entfernt sein.
	 */
	private void genGhostSpawner() {
		// platziert einen geist spawner
		Point p = furthestFromPacSpawn();

		if (p != null) {
			ghostSpawns.add(p);
			setMapAt(p.x, p.y, MapModule.GHOST_SPAWN);
		}
	}

	/**
	 * Generiert eine zufaellige WrapArround-Verbindung am Rand. Diese ist
	 * horizontal oder vertikal.
	 */
	private void genOpenBorder() {
		if (rnd.nextInt(2) == 0) {
			// vertikale verbindung
			int x = rnd.nextInt((width - 1) / 2) * 2 + 1;
			setMapAt(x, 0, MapModule.DOT);
			setMapAt(x, height - 1, MapModule.DOT);
		} else {
			// horizontale verbindung
			int y = rnd.nextInt((height - 1) / 2) * 2 + 1;
			setMapAt(0, y, MapModule.DOT);
			setMapAt(width - 1, y, MapModule.DOT);
		}
	}

	// TODO task 3a

	/**
	 * Setzt einen zufällig gewählten Dot durch übergene MapModule
	 * 
	 * @param m MapModule
	 */
	private void genRandomModule(MapModule m) {
		// Erzeuge einen Punkt
		Point dotPoint = new Point(rnd.nextInt(width), rnd.nextInt(height));

		// Wenn der Punkt ein Dot ist, ersetze den Punkt, sonst führe die Methode wieder
		// aus
		if (getMapAt(dotPoint.x, dotPoint.y) == MapModule.DOT) {
			setMapAt(dotPoint.x, dotPoint.y, m);
		} else {
			genRandomModule(m);
		}
	}

	// TODO task 3d
	/**
	 * Gibt den Punkt, der Dot ist und größte Distanz zum Pacmanspawnshat
	 * 
	 * @return Der angeforderte Punkt
	 */
	private Point furthestFromPacSpawn() {

		if (pacmanSpawns.isEmpty())
			return null;

		/*
		 * Erzeuge eine Map, wo die Punkte mit der Distanz zu nächstgelegenem
		 * Pacmanspawn gespeichert werden.
		 */
		HashMap<Double, Point> map = new HashMap<>();

		// Gehe jeden Punkt des Rasters durch
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {

				Point p = new Point(i, j);

				if (getMapAt(p.x, p.y) == MapModule.DOT) { // für alle Dots

					// Gehe wieder das Raster durch und wenn der Punkt ein Pacmanspawn ist,
					// speichere in einer Liste die Abstände von unserem Punkt zu jedem Pacmanspawn.
					// Am Ende wähle die kleinste Distanz (nächstgelegener PacmanSpawn) und
					// speichere in der Map
					for (int m = 0; m < width; m++) {
						for (int n = 0; n < height; n++) {

							Point k = new Point(m, n);

							// Liste für die Abstände eines Punktes zu jedem Playerspawn
							ArrayList<Double> lengthToPlayerSpawnsList = new ArrayList<Double>();

							for (Point spawner : pacmanSpawns) {
								if (k.equals(spawner)) {
									lengthToPlayerSpawnsList.add(Math.sqrt((p.x - k.x) ^ 2 + (p.y - k.y) ^ 2));
								}
							}

							// wenn die Liste leer ist, gehe weiter mit nächster Iteration
							if (lengthToPlayerSpawnsList.isEmpty())
								continue;

							// finde nächstgelegenen PlayerSpawn für jeden Punkt und füge der Liste hinzu
							map.put(Collections.min(lengthToPlayerSpawnsList), p);

						}
					}
				}
			}
		}

		if (map.isEmpty())
			return null;
		else {
			// Der Punkt mit größter Distanz zum nächstgelegenen Pacmanspawn wird gewählt
			double biggestDistance = Collections.max(map.keySet());
			Point furthestPoint = map.get(biggestDistance);
			return furthestPoint;
		}
	}

}
