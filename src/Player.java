import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.io.*;
import java.math.*;


/* Da implementare nei prossimi giorni
 * Metodo che calcoli value di Pellet presunto su A-B pathValue / pathValuePondered
 * Algoritmo di valutazione priorita, quante caselle indagare, come ponderare distanza e value e speed findBestRoute
 * 
 * 
 * Ricerca di nemici nelle vicinanze e soglia di pericolosità (si appoggia su metodo del grafo che calcola percorso)
 */

/**
 * Grab the pellets as fast as you can!
 **/
class Player {
	static class BFSSolver {
	    private static final int[][] DIRECTIONS = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	    public List<Coordinate> solve(Graph graph, Coordinate source, Coordinate target) {
	        LinkedList<Coordinate> nextToVisit = new LinkedList<>();
	        nextToVisit.add(source);

	        while (!nextToVisit.isEmpty()) {
	            Coordinate cur = nextToVisit.remove();

	            if (!graph.isValidLocation(cur.getX(), cur.getY()) || graph.isExplored(cur.getX(), cur.getY())) {
	                continue;
	            }

	            if (graph.isWall(cur.getX(), cur.getY())) {
	            	graph.setVisited(cur.getX(), cur.getY(), true);
	                continue;
	            }

	            if (target.equals(new Coordinate(cur.getX(), cur.getY()))) {
	                return backtrackPath(cur);
	            }

	            for (int[] direction : DIRECTIONS) {
	                Coordinate coordinate = new Coordinate(cur.getX() + direction[0], cur.getY() + direction[1], cur);
	                if (!graph.isValidLocation(coordinate.getX(), coordinate.getY())) {
	                	coordinate.setX( coordinate.getX() >= 0? coordinate.getX(): graph.getWidth()-1);
	                	coordinate.setY( coordinate.getY() >= 0? coordinate.getY(): graph.getHeight()-1);
	                }
	                nextToVisit.add(coordinate);
	                graph.setVisited(cur.getX(), cur.getY(), true);
	            }
	        }
	        return Collections.emptyList();
	    }

	    private List<Coordinate> backtrackPath(Coordinate cur) {
	        List<Coordinate> path = new ArrayList<>();
	        Coordinate iter = cur;

	        while (iter != null) {
	            path.add(iter);
	            iter = iter.parent;
	        }

	        return path;
	    }
	}
	
	//Enum per i tipi di pacman con metodo per comparare
	enum PacType {
	    ROCK, PAPER, SCISSORS;

	    boolean beats(PacType another) {
	        switch (this) {
	        case ROCK:
	            return another == SCISSORS;
	        case PAPER:
	            return another == ROCK;
	        case SCISSORS:
	            return another == PAPER;
	        
	        default:
	            throw new IllegalStateException();
	        }
	        
	    }
	}

	//Costanti del gioco e strutture dati accessibili ovunque
	private final static int COOLDOWN = 10;
	private static final int safeDistance = 2;
	private static final int minSpread = 0;
	static Graph graph;
	static Map<Integer, Pac> myPacMap;
	static Map<Integer, Pac> enemyPacMap;
	static Map<Pellet, Integer> pelletList;
	static Set<Coordinate> myDestinations;
	
	//Classe coordinate, utilizzata da tutte le altre classi, la distanza tra due coordinate viene calcolata sul grafo (TODO)
	static class Coordinate {
		private int x;
		private int y;
		Coordinate parent;
		
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public void setX(int x) {
			this.x = x;
		}
		public void setY(int y) {
			this.y = y;
		}
		public Coordinate(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}
		public Coordinate(int x, int y, Coordinate parent) {
			super();
			this.x = x;
			this.y = y;
			this.parent = parent;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coordinate other = (Coordinate) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		
		private Double GetDistanceFrom(Coordinate pos) {
//			Integer deltaX = Math.min( Math.abs(pos.getX() - this.getX()), 
//										Math.min(pos.getX(), this.getX()) + maze.getHeight() - Math.max(pos.getX(), this.getX()));
//			Integer deltaY = Math.min( Math.abs(pos.getY() - this.getY()), 
//					Math.min(pos.getX(), this.getY()) + maze.getWidth() - Math.max(pos.getY(), this.getY()));
//
//			Double distance = Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));
			return 0.0;
		}
	}
	
	//Classe per i Pac
	static class Pac {
		private int pacId;
		private boolean mine;
		private Coordinate position;
		private PacType typeId;
		private int speedTurnsLeft;
		private int abilityCooldown;
		private Coordinate destination = null;
		private Coordinate lastPosition = null;
		private boolean busy = false;
		
		public Pac(int pacId, boolean mine, Coordinate position, PacType typeId, int speedTurnsLeft, int abilityCooldown) {
			super();
			this.pacId = pacId;
			this.mine = mine;
			this.position = position;
			this.typeId = typeId;
			this.speedTurnsLeft = speedTurnsLeft;
			this.abilityCooldown = abilityCooldown;
		}
		
		public int getPacId() {
			return pacId;
		}
		public void setPacId(int pacId) {
			this.pacId = pacId;
		}
		public boolean isMine() {
			return mine;
		}
		public void setMine(boolean mine) {
			this.mine = mine;
		}
		public Coordinate getPosition() {
			return position;
		}
		public void setPosition(Coordinate position) {
			this.position = position;
		}
		public PacType getTypeId() {
			return typeId;
		}
		public void setTypeId(PacType typeId) {
			this.typeId = typeId;
		}
		public int getSpeedTurnsLeft() {
			return speedTurnsLeft;
		}
		public void setSpeedTurnsLeft(int speedTurnsLeft) {
			this.speedTurnsLeft = speedTurnsLeft;
		}
		public int getAbilityCooldown() {
			return abilityCooldown;
		}
		public void setAbilityCooldown(int abilityCooldown) {
			this.abilityCooldown = abilityCooldown;
		}

		public void sendTo(Coordinate c, StringBuffer sb) {
			this.destination = c;
			this.busy = true;
			myDestinations.add(c);
			sb.append("MOVE "+this.getPacId()+" "+c.getX()+" "+c.getY()+"|");
		}

		public boolean isBusy() {
			return busy;
		}

		public Coordinate getDestination() {
			return this.destination;
		}

		public void targetReached() {
			myDestinations.remove(this.destination);
			this.busy = false;
			this.destination = null;
		}

		public boolean isColliding() {
			return this.position.equals(this.lastPosition);
		}

		public void setLastPosition(Coordinate position) {
			this.lastPosition = position;
		}
		
		public void switchTo(PacType newType, StringBuffer sb) {
			switch (newType) {
	        case ROCK:
	            this.typeId = PacType.PAPER;
	            break;
	        case PAPER:
	            this.typeId = PacType.SCISSORS;
	            break;
	        case SCISSORS:
	            this.typeId = PacType.ROCK;
	            break;
	        default:
	            throw new IllegalStateException();
	        }
			sb.append("SWITCH "+this.getPacId()+" "+this.getTypeId()+"|");
			this.abilityCooldown = COOLDOWN;
		}
		
		public void free() {
			this.busy = false;
		}

		public void increaseSpeed(StringBuffer sb) {
			sb.append("SPEED "+this.getPacId()+"|");
			this.abilityCooldown = COOLDOWN;
		}
	}
	
	//Classe per i Pellet, semplicemente Coordinate e value, equals considera solo coordinate, non possono esistere 2 pellet nella stessa cella
	static class Pellet {
		private Coordinate coordinate;
		private Integer value;
		
		public Pellet(Coordinate coordinate, int value) {
			this.coordinate = coordinate;
			this.value = value;
		}
		
		private Integer GetValue() {
			return this.value;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((coordinate == null) ? 0 : coordinate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pellet other = (Pellet) obj;
			if (coordinate == null) {
				if (other.coordinate != null)
					return false;
			} else if (!coordinate.equals(other.coordinate))
				return false;
			return true;
		}

		// Restituisce la distanza calcolata sul grafo, si appoggia al metodo di Coordinate
		public Double GetDistanceFrom(Coordinate pos) {
			return this.coordinate.GetDistanceFrom(pos);
		}
	}

	static class Graph {
	    private static final int ROAD = 0;
	    private static final int WALL = 1;
	    
	    private int width;
	    private int height;

	    private int[][] graph;
	    private boolean[][] visited;

	    public Graph(int width, int height) {
	        this.width = width;
	        this.height = height;
	        graph = new int[height][width];
	        visited = new boolean[height][width];
	    }

	    public int getHeight() {
	        return height;
	    }

	    public int getWidth() {
	        return width;
	    }

	    public boolean isExplored(int col, int row) {
	        return visited[row][col];
	    }

	    public boolean isWall(int col, int row) {
	        return graph[row][col] == WALL;
	    }

	    public void setVisited(int col, int row, boolean value) {
	        visited[row][col] = value;
	    }

	    public boolean isValidLocation(int col, int row) {
	        if (row < 0 || row >= getHeight() || col < 0 || col >= getWidth()) {
	            return false;
	        }
	        return true;
	    }
	    
	    public boolean isValidPath(Coordinate target) {
			return (this.isValidLocation(target.getX(), target.getY()) && !this.isWall(target.getX(), target.getY()));
		}

	    public void reset() {
	        for (int i = 0; i < visited.length; i++)
	            Arrays.fill(visited[i], false);
	    }

		public void addRow(String row, int nRow) {
			int col = 0;
			for (char ch:row.toCharArray()) {
				if (ch == '#')
					graph[nRow][col] = WALL;
				else if (ch == ' ') {
					graph[nRow][col] = ROAD;
					pelletList.put(new Pellet(new Coordinate(col, nRow), 1), 1);
				}
				col++;
			}
		}
		//Return a Set of coordinates seen from source
		public void getAdiacent(Coordinate source, Set<Coordinate> pathSet) {
			int x = source.getX();
			int y = source.getY();
			while (this.isValidLocation(x, y) && !this.isWall(x, y)) {
				pathSet.add(new Coordinate(x,y));
				x++;
			}
			x = source.getX()-1;
			while (this.isValidLocation(x, y) && !this.isWall(x, y)) {
				pathSet.add(new Coordinate(x,y));
				x--;
			}
			x = source.getX();
			y = source.getY()+1;
			while (this.isValidLocation(x, y) && !this.isWall(x, y)) {
				pathSet.add(new Coordinate(x,y));
				y++;
			}
			y = source.getY()-1;
			while (this.isValidLocation(x, y) && !this.isWall(x, y)) {
				pathSet.add(new Coordinate(x,y));
				y--;
			}		
		}
		
		public Set<Coordinate> getValidCoordinates() {
			Set<Coordinate> res = new HashSet<>();
			for (int i = 0; i<width; i++) {
				for(int j = 0; j<height; j++) {
					if (this.isValidLocation(i, j) && !this.isWall(i, j))
						res.add(new Coordinate(i, j));
				}
			}
			return res;
		}
	}
	
    public static void main(String args[]) {
    	myPacMap = new HashMap<>();
    	enemyPacMap = new HashMap<>();
    	pelletList = new HashMap<>();
    	myDestinations = new HashSet<>();
        Scanner in = new Scanner(System.in);
        graph = new Graph(in.nextInt(), in.nextInt());

        if (in.hasNextLine()) {
            in.nextLine();
        }
        
        for (int i = 0; i < graph.getHeight(); i++) {
            String row = in.nextLine(); // one line of the grid: space " " is floor, pound "#" is wall
            graph.addRow(row, i);
            System.err.println("Grafo creato");
            System.err.println(graph.getHeight()+" "+graph.getWidth());
        }

        // game loop
        while (true) {
            int myScore = in.nextInt();
            int opponentScore = in.nextInt();
            int visiblePacCount = in.nextInt(); // all your pacs and enemy pacs in sight
            for (int i = 0; i < visiblePacCount; i++) {
                int pacId = in.nextInt(); // pac number (unique within a team)
                boolean mine = in.nextInt() != 0; // true if this pac is yours
                int x = in.nextInt(); // position in the grid
                int y = in.nextInt(); // position in the grid
                PacType typeId = PacType.valueOf(in.next()); // unused in wood leagues
                int speedTurnsLeft = in.nextInt(); // unused in wood leagues
                int abilityCooldown = in.nextInt(); // unused in wood leagues
                Pac p = mine ? myPacMap.get(pacId) : enemyPacMap.get(pacId);
                if (p == null) {
                	p = new Pac(pacId, mine, new Coordinate(x,y), typeId, speedTurnsLeft, abilityCooldown);
                	if (mine) myPacMap.put(pacId, p); else enemyPacMap.put(pacId, p);
                } else {
                	p.setPosition(new Coordinate(x, y));
            		p.setAbilityCooldown(abilityCooldown);
            		p.setSpeedTurnsLeft(speedTurnsLeft);
            		p.setTypeId(typeId);
                }
                //Se c'è un Pac non c'è un pellet
                pelletList.put(new Pellet(p.getPosition(), 0), 0);
            }
            int visiblePelletCount = in.nextInt(); // all pellets in sight
            for (int i = 0; i < visiblePelletCount; i++) {
            	//List of pellet seen in this round
            	Set<Pellet> tempPellet = new HashSet<>();
                int x = in.nextInt();
                int y = in.nextInt();
                int value = in.nextInt(); // amount of points this pellet is worth
                Pellet p = new Pellet(new Coordinate(x, y), value);
                if (value == 10) {
                	pelletList.put(p, p.GetValue());
                }
                tempPellet.add(p);
                updatePellet(tempPellet);
            }
            StringBuffer sb = new StringBuffer();
            for (Pac p:myPacMap.values()) {
            	//Controllo e aggiorno il mio PAC
            	//Se il Pac era in viaggio ed è arrivato, aggiorno
            	if (p.getPosition().equals(p.getDestination()) && p.isBusy())
            		p.targetReached();
            	
            	//Analisi dei nemici
            	Pac enemy = findClosestEnemy(p);
            	boolean safe = Objects.isNull(enemy);
            	
            	//Se il Pac è in viaggio e la strada vale >= x
	        		//Aggiorno lastPosition
	        		//continue
            	if (p.isBusy() && !p.isColliding() && safe/*&& pathValue(p.getPosition(), p.getDestination()) >= soglia*/) {
            		p.setLastPosition(p.getPosition());
            		p.sendTo(p.getDestination(), sb);
            		continue;
            	}      
            	
            	//Se ho mana
            	if (p.getAbilityCooldown() == 0) {
            		if (safe)
            			p.increaseSpeed(sb);
            		else {
            			// Mi difendo
            			PacType newType = null;
            			for (PacType pt:PacType.values()) {
            				if (pt.beats(enemy.getTypeId()))
            					newType = pt;
            			}
            			p.switchTo(newType, sb);
            		}
            			
            	//Se non ho mana
            	} else { 
            		Coordinate destination;
            		if (safe) {
            			destination = findBestRoute(p.getPosition(), p.getSpeedTurnsLeft());
            		} else {
            			// Mi allontano random dal nemico di almeno 2 volte la distanza da lui (soglia del safe)
            			Random rand = new Random();
            			do {
	            			int safeX = (2*p.getPosition().getX() - enemy.getPosition().getX()*(rand.nextInt(1)+1));
	            			int safeY = (2*p.getPosition().getY() - enemy.getPosition().getY()*(rand.nextInt(1)+1));
	            			destination = new Coordinate(safeX, safeY);
            			} while (!graph.isValidPath(destination));
            		}
            		if (Objects.isNull(destination))
            			p.sendTo(destination, sb);
            	}
            	p.setLastPosition(p.getPosition());
            }           
            
            // Se ci sono comandi, trimma (Se non ci sono perchè? Ti droghi? )
            if (sb.length() != 0)
            	sb.deleteCharAt(sb.length()-1);
            
            System.out.println(sb.toString());
        }
    }
    private static Coordinate findBestRoute(Coordinate position, int speedTurnsLeft) {
		BFSSolver bfs = new BFSSolver();
		Coordinate best = null;
		double bestValue = 0.0;
		for (int i = 0; i < graph.getWidth(); i++) {
			for (int j = 0; j <graph.getHeight(); j++) {
				if ( !graph.isValidLocation(i, j)) continue;
				
				List<Coordinate> path = bfs.solve(graph, position, new Coordinate(i, j));
				graph.reset();
				double value = 0.0;
				
				//Eventuale max Length
				
				for (Coordinate c:path)
					value+= pelletList.get(new Pellet(c, 0));
				
				value = value / Math.min(0, (path.size() - speedTurnsLeft));
				
				//Eventuale soglia
				
				//Eventuale controllo su zone
				boolean farFromOthers = true;
				for (Coordinate c:myDestinations) {
					if (bfs.solve(graph, position, c).size() <= minSpread) {
						farFromOthers = false;
						graph.reset();
						break;
					}
					graph.reset();
				}
				
				if (value > bestValue && farFromOthers)
					best = new Coordinate(i, j);
				
			}
		}
		return best;
	}
	private static Pac findClosestEnemy(Pac p) {
		Coordinate c = p.getPosition();
		Pac enemy = null;
		for (Pac e:enemyPacMap.values()) {
			BFSSolver bfs = new BFSSolver();
			if (bfs.solve(graph, c, e.getPosition()).size() <= safeDistance) {
				graph.reset();
				enemy = e;
				break;
			}
			graph.reset();
		}
		return enemy;
	}
	//Aggiorno la mappa dei Pellet togliendo quelli che non vedo più
	private static void updatePellet(Set<Pellet> tempPellet) {
		for (Pac p: myPacMap.values()) {
			Set<Coordinate> coordPacSee = new HashSet<>();
			graph.getAdiacent(p.getPosition(), coordPacSee);
			for (Coordinate c:coordPacSee) {
				if (!tempPellet.contains(new Pellet(c, 0))) {
					pelletList.put(new Pellet(c, 0), 0);
				}
			}
		}
	}

	
}