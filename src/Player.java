import java.util.*;
import java.util.stream.Collectors;

/* Da implementare nei prossimi giorni
 * Metodo che calcoli value di Pellet presunto su A-B pathValue / pathValuePondered
 * Algoritmo di valutazione priorita, quante caselle indagare, come ponderare distanza e value e speed findBestRoute
 * 
 * Prima quelli vicini
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
//	            Couple c = new Couple(target, cur);
//	            List<Coordinate> partial = dp.get(c);
//	            if (partial != null) {
//	            	partial.addAll(backtrackPath(cur));
//	            	dp.put(new Couple(source, target), partial);
//	                return dp.get(c);
//	            }

	            if (!graph.isValidLocation(cur.getX(), cur.getY()) || graph.isExplored(cur.getX(), cur.getY())) {
	                continue;
	            }

	            if (graph.isWall(cur.getX(), cur.getY())) {
	            	graph.setVisited(cur.getX(), cur.getY(), true);
	                continue;
	            }
	            
//	            dp.put(new Couple(cur, source), backtrackPath(cur));

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
	private static final int safeDistance = 1;
	private static final int minSpread = 3;
	private static final double minStart = 0.5;
    private static final int MAX_COUNT = 10;
	static Graph graph;
	static Map<Integer, Pac> myPacMap;
	static Map<Integer, Pac> enemyPacMap;
	static Map<Coordinate, Integer> pelletList;
	static Set<Coordinate> myDestinations;
//	static Map<Couple, List<Coordinate>> dp = 
//		    new HashMap<Couple, List<Coordinate>>();
	
	//Classe coordinate, utilizzata da tutte le altre classi, la distanza tra due coordinate viene calcolata sul grafo (TODO)
	static class Coordinate {
		private int x;
		private int y;
		@Override
		public String toString() {
			return "Coordinate [x=" + x + ", y=" + y + ", parent=" + parent + "]";
		}

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
			Integer deltaX = Math.min( Math.abs(pos.getX() - this.getX()), 
										Math.min(pos.getX(), this.getX()) + graph.getHeight() - Math.max(pos.getX(), this.getX()));
			Integer deltaY = Math.min( Math.abs(pos.getY() - this.getY()), 
					Math.min(pos.getX(), this.getY()) + graph.getWidth() - Math.max(pos.getY(), this.getY()));

			Double distance = Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));
			return distance;
		}
	}
	//Classe per i Pac
	static class Pac {
		@Override
		public String toString() {
			return "Pac [pacId=" + pacId + ", mine=" + mine + ", position=" + position + ", typeId=" + typeId
					+ ", speedTurnsLeft=" + speedTurnsLeft + ", abilityCooldown=" + abilityCooldown + ", destination="
					+ destination + ", lastPosition=" + lastPosition + ", busy=" + busy + "]";
		}

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
	            this.typeId = PacType.ROCK;
	            break;
	        case PAPER:
	            this.typeId = PacType.PAPER;
	            break;
	        case SCISSORS:
	            this.typeId = PacType.SCISSORS;
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
					pelletList.put(new Coordinate(col, nRow), 1);
				}
				col++;
			}
		}
		//Return a Set of coordinates seen from source
		public Set<Coordinate> getAdiacent(Coordinate source) {
			Set<Coordinate> pathSet = new HashSet<>();
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
			return pathSet;
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
        }

        // game loop
        while (true) {
            int myScore = in.nextInt();
            int opponentScore = in.nextInt();
            int visiblePacCount = in.nextInt(); // all your pacs and enemy pacs in sight
            Set<Integer> tempPacSet = new HashSet<>();
            Set<Integer> tempEnemyPacSet = new HashSet<>();
            for (int i = 0; i < visiblePacCount; i++) {
                int pacId = in.nextInt(); // pac number (unique within a team)
                boolean mine = in.nextInt() != 0; // true if this pac is yours
                int x = in.nextInt(); // position in the grid
                int y = in.nextInt(); // position in the grid
                PacType typeId = PacType.valueOf(in.next()); // unused in wood leagues
                int speedTurnsLeft = in.nextInt(); // unused in wood leagues
                int abilityCooldown = in.nextInt(); // unused in wood leagues
                if (mine) tempPacSet.add(pacId); else tempEnemyPacSet.add(pacId);
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
                pelletList.put(p.getPosition(), 0);
            }
                        
            int visiblePelletCount = in.nextInt(); // all pellets in sight
        	Set<Coordinate> tempPellet = new HashSet<>();
            for (int i = 0; i < visiblePelletCount; i++) {
            	//List of pellet seen in this round
                int x = in.nextInt();
                int y = in.nextInt();
                int value = in.nextInt(); // amount of points this pellet is worth
                if (value == 10 && !myDestinations.contains(new Coordinate(x,y))) {
                	pelletList.put(new Coordinate(x, y), value);
                }
                tempPellet.add(new Coordinate(x, y));
            }
            updatePellet(tempPellet);
            StringBuffer sb = new StringBuffer();
            
            for (int i = 0; i<5; i++) {
	            if (!tempPacSet.contains(i)) {
	            	if (myPacMap.get(i) != null) 
	            		myPacMap.get(i).targetReached();
	            	
	        		myPacMap.remove(i);
	        	}
            }
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
            	if (p.isBusy() && !p.isColliding() && safe) {
            		p.setLastPosition(p.getPosition());
            		p.sendTo(p.getDestination(), sb);
            		continue;
            	}      
            	
                p.targetReached();
   

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
            		Coordinate destination = null;
            		if (safe) {
            			destination = findBestRoute(p.getPosition(), p.getSpeedTurnsLeft());
            		} else {
            			// Mi allontano random dal nemico di almeno 2 volte la distanza da lui (soglia del safe)
            			Random rand = new Random();
            			do {
	            			int safeX = (p.getPosition().getX() - enemy.getPosition().getX()*rand.nextInt(2) + (rand.nextInt(5)-2));
	            			int safeY = (2*p.getPosition().getY() - enemy.getPosition().getY()*rand.nextInt(2) + (rand.nextInt(5)-2));
	            			destination = new Coordinate(safeX, safeY);
            			} while (!graph.isValidPath(destination));
            		}
            		if (!Objects.isNull(destination))
            			p.sendTo(destination, sb);
            		else
            			p.sendTo(p.getPosition(), sb);
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
		Comparator<Coordinate> sortingByDistance = new Comparator<Coordinate>() {
			@Override
			public int compare(Coordinate c1, Coordinate c2) {
				return c1.GetDistanceFrom(position).compareTo(c2.GetDistanceFrom(position));
			}
		};

		List<Coordinate> feasible = pelletList.entrySet().stream()
				.filter(x -> x.getValue() == 10)
				.map(x -> x.getKey())
				.collect(Collectors.toList());
		
		//.sorted(Map.Entry.<Coordinate, Integer>comparingByKey(byDistance(position)).reversed())
		
		if (feasible.isEmpty()) {
			feasible = pelletList.entrySet().stream()
					.filter(x -> x.getValue() > 0)
					.map(x -> x.getKey())
					.collect(Collectors.toList());
		}
		feasible.sort(sortingByDistance.reversed());
		int count = 0;
		for (Coordinate c:feasible) {
			if (!graph.isValidLocation(c.getX(), c.getY())) continue;
			List<Coordinate> path = bfs.solve(graph, position, c);
            graph.reset();
			count++;
			if (path.isEmpty()) continue;
            
			//Eventuale max Length
			double value = pathValue(path, speedTurnsLeft);
			
			
			//Eventuale soglia
			
			//Eventuale controllo su zone
			boolean farFromOthers = true;
			for (Coordinate cDest:myDestinations) {
				if (bfs.solve(graph, position, cDest).size() <= minSpread)
					farFromOthers = false;
			
				graph.reset();
			}
			
			if (value > bestValue && farFromOthers) {
                best = c;
                bestValue = value;
                if (value >= minStart || count >= MAX_COUNT)
                	break;
            }
				
			
		}
		if (!Objects.isNull(best)) {
			if (pelletList.get(best) == 10)
	    		pelletList.put(best, 0);
		}

		return best;
	}
	
	private static double pathValue(List<Coordinate> path, int speedTurnsLeft) {
		if (path.size() == 0) {
			System.err.println("Path vuota, dp non funziona?");
			return 0.0;
		}
		double value = 0.0;
		for (Coordinate cpath:path)
			value+= pelletList.get(cpath);
		
		value = value==0? 0: value / Math.max(1, path.size() - speedTurnsLeft);
		return value;
	}
	private static Pac findClosestEnemy(Pac p) {
		Coordinate c = p.getPosition();
		Pac enemy = null;
		for (Pac e:enemyPacMap.values()) {
			BFSSolver bfs = new BFSSolver();
			if (bfs.solve(graph, c, e.getPosition()).size() <= safeDistance) {
				graph.reset();
				
                if (e.getTypeId().beats(p.getTypeId()) || e.getAbilityCooldown() == 0) {
                    enemy = e;
                    break;
                }
			}
			graph.reset();
		}
		return enemy;
	}
	//Aggiorno la mappa dei Pellet togliendo quelli che non vedo più
	private static void updatePellet(Set<Coordinate> tempPellet) {
		for (Pac p: myPacMap.values()) {
			Set<Coordinate> coordPacSee = graph.getAdiacent(p.getPosition());
			for (Coordinate c:coordPacSee) {
				if (!tempPellet.contains(c)) {
					pelletList.put(c, 0);
				}
			}
		}
	}	
}