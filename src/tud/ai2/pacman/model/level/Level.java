package tud.ai2.pacman.model.level;

import tud.ai2.pacman.exceptions.NoDotsException;
import tud.ai2.pacman.exceptions.NoGhostSpawnPointException;
import tud.ai2.pacman.exceptions.NoPacmanSpawnPointException;
import tud.ai2.pacman.exceptions.ReachabilityException;
import tud.ai2.pacman.util.Consts;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;


/**
 * Modelliert einen spielbaren Level.
 *
 * @author Simon Breitfelder
 * @author Dominik Puellen
 * @author Robert Cieslinski
 * @author Kurt Cieslinski
 */
public class Level {
    /** aktuell angesprochener Geisterspawner */
    private int currentGhostSpawnCounter = 0;

    /** Ein Random-Generator */
    private final Random rnd;
    /** Name des Levels */
    private String name;
    /** Level-Layout */
    private final MapModule[][] map;
    /** alle Positionen der Pacman-Spawner */
    private final Point[] pacmanSpawns;
    /** alle Positionen der Geister-Spawner */
    private final Point[] ghostSpawns;

    /**
     * Konstruktor.
     *
     * @param map Level-Layout
     * @param pacmanSpawns Positionen der Pacman-Spawner
     * @param ghostSpawns Positionen der Geister-Spawner
     */
    public Level(MapModule[][] map, Point[] pacmanSpawns, Point[] ghostSpawns) {
        this("Unbenannter Level", map, pacmanSpawns, ghostSpawns);
    }

    /**
     * Konstruktor.
     *
     * @param name Name des Levels
     * @param map Level-Layout
     * @param pacmanSpawns Positionen der Pacman-Spawner
     * @param ghostSpawns Positionen der Geister-Spawner
     */
    public Level(String name, MapModule[][] map, Point[] pacmanSpawns, Point[] ghostSpawns) {
        this.name = name;
        this.map = map;
        this.pacmanSpawns = pacmanSpawns;
        this.ghostSpawns = ghostSpawns;
        rnd = new Random();
    }

    /**
     * @return Level-Name
     */
    public String getName() {
        return name;
    }

    /**
     * Aendert den Level-Namen
     * @param value neuer Namen
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * @return Breite des Levels
     */
    public int getWidth() {
        return map[0].length;
    }

    /**
     * @return Hoehe des Levels
     */
    public int getHeight() {
        return map.length;
    }

    /**
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @return Modul an der uebergebenen Position
     */
    public MapModule getField(int x, int y) {
        return map[y][x];
    }

    @Deprecated
    public void setField(int x, int y, char c) {
        map[y][x] = MapModule.findByValue(c);
    }

    /**
     * Punkte ausserhalb des Spielfeldbereichs sind solid.
     *
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @return true <-> Modul ist Wand oder Hintergrund
     */
    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return true;
        return map[y][x] == MapModule.WALL || map[y][x] == MapModule.BACKGROUND;
    }

    /**
     * Punkte ausserhalb des Spielfeldbereichs sind keine Wand.
     *
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @return true <-> Modul ist Wand
     */
    public boolean isWall(int x, int y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return false;
        return map[y][x] == MapModule.WALL;
    }

    //TODO task 2a
    /**
	 * Gibt an ob ein direkter Weg von einem Punkt zum anderen besteht
	 * 
	 * @param p1 Punkt 1
	 * @param p2 Punkt 2
	 * @return true <-> der direkte Weg existiert
	 */
	public boolean existsStraightPath(Point p1, Point p2) {

		if (p1 == null || p2 == null)
			throw new IllegalArgumentException();
		
		// Punkte auf "solid" überprüfen
		if (isSolid(p2.x, p2.y) || isSolid(p1.x, p1.y))
			return false;

		/*
		 * Wenn ein direkter Weg besteht, dann sind entweder x-Koordinaten oder
		 * y-Koordinaten gleich
		 */
		if (p1.y == p2.y) {

			if (p1.x == p2.x)
				return true;

			// Beide Fälle für x-Größen werden überprüft.
			// Wenn zwischen den Punkten ein Wand existiert, dann false,, sonst true
			if (p1.x > p2.x) {
				for (int i = p2.x; i <= p1.x; i++) {
					if (isWall(i, p1.y)) {
						return false;
					}
				}
				return true;
			} else {
				for (int i = p1.x; i <= p2.x; i++) {
					if (isWall(i, p1.y)) {
						return false;
					}
				}
				return true;
			}
		}

		if (p1.x == p2.x) {
			// Beide Fälle für x-Größen werden überprüft.
			// Wenn zwischen den Punkten ein Wand existiert, dann false,, sonst true
			if (p1.y > p2.y) {
				for (int j = p2.y; j <= p1.y; j++) {
					if (isWall(p1.x, j)) {
						return false;
					}
				}
				return true;
			} else {
				for (int j = p1.y; j <= p2.y; j++) {
					if (isWall(p1.x, j)) {
						return false;
					}
				}
				return true;
			}
		}
		return false; // wenn keiner der Koordinaten gleich sind, dann false
	}


    /**
     * @return zufaelliger PacmanSpawner-Punkt
     */
    public Point getRandomPacmanSpawn() {
        return pacmanSpawns[rnd.nextInt(pacmanSpawns.length)];
    }

    /**
     * @return zufaelliger GeisterSpawner-Punkt
     */
    public Point getRandomGhostSpawn() {
        return ghostSpawns[rnd.nextInt(ghostSpawns.length)];
    }

    /**
     * @return naechster GeisterSpawner-Punkt
     */
    public Point getNextGhostSpawn() {
        return ghostSpawns[currentGhostSpawnCounter++ % ghostSpawns.length];
    }

    /**
     * @return eine zufaellige Position, die ein Dot oder Freiraum ist
     */
    public Point getRandomSpaceField() {
        // alle begehbaren felder auflisten
        ArrayList<Point> fields = new ArrayList<>();

        for (int y = 0; y < getHeight(); y++)
            for (int x = 0; x < getWidth(); x++)
                if (map[y][x] == MapModule.DOT || map[y][x] == MapModule.SPACE)
                    fields.add(new Point(x, y));

        // zufaelliges element der liste ausgeben
        return fields.get(rnd.nextInt(fields.size()));
    }

    /**
     * @return true <-> mindestens ein Dot wurde platziert
     */
    private boolean hasDot() {
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
                if (map[y][x] == MapModule.DOT)
                    return true;
        return false;
    }

    /**
     * Ueberprueft, ob der Level-Grid korrekt aufgebaut ist.
     *
     * @throws NoDotsException falls keine Dots platziert wurden
     * @throws ReachabilityException falls nicht erreichbare Dots existieren
     * @throws NoPacmanSpawnPointException falls keine PacmanSpawner existieren
     * @throws NoGhostSpawnPointException falls keine GeisterSpawner existieren
     */
    public void validate() throws ReachabilityException, NoPacmanSpawnPointException, NoGhostSpawnPointException, NoDotsException {
        // anzahl spawner pruefen
        if (pacmanSpawns.length == 0) throw new NoPacmanSpawnPointException();
        if (ghostSpawns.length == 0)  throw new NoGhostSpawnPointException();

        // punkte pruefen
        if (!hasDot()) throw new NoDotsException();

        reachability();
    }

    //TODO task 2b
    /**
	 * Überprüft ob alle Punkte erreichbar sind
	 * @throws ReachabilityException Wenn auch ein Punkt nicht erreichbar
	 */
	public void reachability() throws ReachabilityException {

		boolean[][] valid = new boolean[getHeight()][getWidth()];

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				valid[y][x] = isSolid(x, y);
			}
		}

		LinkedList<Point> trace = new LinkedList<Point>();

		trace.add(pacmanSpawns[0]);

		while (!trace.isEmpty()) {
			Point p = trace.getLast();
			valid[p.y][p.x] = true;
			trace.remove(p);
			
			//Überprüfung von Nachbaren
			//Wenn der Nachbar nicht solid ist und außerhalb des Rasters ist,
			//heißt, es gibt einen Tunnel dort
			for (Point q : getBranches(p)) {
				if (!isSolid(q.x, q.y)) {
					if (!valid[q.y][q.x]) {
						trace.add(q);
					}
				} else {
					if (q.x == -1) {
						Point l = new Point(getWidth() - 1, q.y);
						if (!valid[l.y][l.x]) {
							trace.add(l);
						}
					} else if (q.x == getWidth()) {
						Point l = new Point(0, q.y);
						if (!valid[l.y][l.x]) {
							trace.add(l);
						}
					} else if (q.y == -1) {
						Point l = new Point(q.x, getHeight() - 1);
						if (!valid[l.y][l.x]) {
							trace.add(l);
						}
					} else if (q.y == getWidth()) {
						Point l = new Point(q.x, 0);
						if (!valid[l.y][l.x]) {
							trace.add(l);
						}
					}
				}
			}
		}

		//Wenn ein Dot nicht valide, wirf Exception
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (getField(x, y) == MapModule.DOT) {
					if (valid[y][x] == false)
						throw new ReachabilityException("Error! Invalid dot.");
				}
			}

		}

	}

    /**
     * {@inheritDoc}
     * Gibt den Level-Grid anhand der Modul-Zeichen als String zurueck.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++)
                sb.append(map[y][x].getValue());
            if (y < (getHeight() - 1))
                sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * @param p zu untersuchende Position
     * @return alle direkten Nachbarn, die keine Wand sind
     */
    public Point[] getBranches(Point p) {
        ArrayList<Point> branches = new ArrayList<>();
        if (p.x >= 0 && !isWall(p.x - 1, p.y))
            branches.add(new Point(p.x - 1, p.y));
        if (p.y >= 0 && !isWall(p.x, p.y - 1))
            branches.add(new Point(p.x, p.y - 1));
        if (p.x <= (getWidth() - 1) && !isWall(p.x + 1, p.y))
            branches.add(new Point(p.x + 1, p.y));
        if (p.y <= (getHeight() - 1) && !isWall(p.x, p.y + 1))
            branches.add(new Point(p.x, p.y + 1));
        return branches.toArray(new Point[0]);
    }

    /**
     * @return alle Levelnamen in dem Levelordner
     */
    public static String[] listLevelFiles() {
        File f = new File(Consts.LEVEL_FOLDER);
        return f.list();
    }
}
