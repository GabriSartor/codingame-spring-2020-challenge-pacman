import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.io.*;
import java.math.*;

/**
 * Grab the pellets as fast as you can!
 **/
class Player {
	
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
	        // note: see alternative below
	        default:
	            throw new IllegalStateException();
	        }
	        // alternatively, just throw here without the default case
	        // throw new IllegalStateException();
	    }
	}

	private static int width;
	private static int height;
	private final static int COOLDOWN = 10;
	
	
	static class Coordinate {
		private int x;
		private int y;
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public Coordinate(int x, int y) {
			super();
			this.x = x;
			this.y = y;
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
										Math.min(pos.getX(), this.getX()) + width - Math.max(pos.getX(), this.getX()));
			Integer deltaY = Math.min( Math.abs(pos.getY() - this.getY()), 
					Math.min(pos.getX(), this.getY()) + width - Math.max(pos.getY(), this.getY()));

			Double distance = Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));
			return distance;
		}
	}
	
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
			sb.append("MOVE "+this.getPacId()+" "+c.getX()+" "+c.getY()+"|");
		}

		public boolean isBusy() {
			return busy;
		}

		public Coordinate getDestination() {
			return this.destination;
		}

		public void targetReached() {
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
		}
	}
	
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

		public Double GetDistanceFrom(Coordinate pos) {
			return this.coordinate.GetDistanceFrom(pos);
		}
	}

    public static void main(String args[]) {
    	Map<Integer, Pac> myPacMap = new HashMap<>();
    	Map<Integer, Pac> enemyPacMap = new HashMap<>();
    	List<Pellet> pelletList = new ArrayList<>();
        Scanner in = new Scanner(System.in);
        width = in.nextInt(); // size of the grid
        height = in.nextInt(); // top left corner is (x=0, y=0)
        if (in.hasNextLine()) {
            in.nextLine();
        }
        for (int i = 0; i < height; i++) {
            String row = in.nextLine(); // one line of the grid: space " " is floor, pound "#" is wall
        }

        // game loop
        while (true) {
            pelletList.clear();
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
                
                if (!mine) continue;
 
                Pac p = myPacMap.get(pacId);
                if (p == null) {
                	p = new Pac(pacId, mine, new Coordinate(x,y), typeId, speedTurnsLeft, abilityCooldown);
                	myPacMap.put(pacId, p);
                }
                p.setPosition(new Coordinate(x, y));
                p.setAbilityCooldown(abilityCooldown);
            }
            int visiblePelletCount = in.nextInt(); // all pellets in sight
            for (int i = 0; i < visiblePelletCount; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int value = in.nextInt(); // amount of points this pellet is worth
                Pellet p = new Pellet(new Coordinate(x, y), value);
                pelletList.add(p);
            }
            StringBuffer sb = new StringBuffer();
            for (Pac p:myPacMap.values()) {
            	boolean cmd = false;
            	if (p.getPosition().equals(p.getDestination()) && p.isBusy()) {
            		p.targetReached();
            	}
            	for (Pac pEnemy:enemyPacMap.values()) {
            		if (p.getPosition().GetDistanceFrom(pEnemy.getPosition()) <= 2) {
            			if (!p.getTypeId().beats(pEnemy.getTypeId()) && p.getAbilityCooldown() == 0) {
            				p.switchTo(pEnemy.getTypeId(), sb);
            				cmd = true;
            				break;
            			}
            		}
            	}
            	if (p.getAbilityCooldown() == 0) {
            		p.increaseSpeed(sb);
            		cmd = true;
            	}
            	if (cmd) continue;
            	// Se non si sta muovendo o ha subito una collisione, trova nuovo percorso
            	if ((!p.isBusy() || p.isColliding())) {
            		List<Pellet> bigPelletList = pelletList
            				  .stream()
            				  .filter(o -> o.GetValue() == 10)
            				  .sorted((o1, o2) -> o1.GetDistanceFrom(p.getPosition()).compareTo(o2.GetDistanceFrom(p.getPosition())))
            				  .collect(Collectors.toList());
            		
            		List<Pellet> smallPelletList = pelletList
          				  .stream()
          				  .filter(o -> o.GetValue() != 10)
          				  .sorted((o1, o2) -> o1.GetDistanceFrom(p.getPosition()).compareTo(o2.GetDistanceFrom(p.getPosition())))
          				  .collect(Collectors.toList());
            		Random rand = new Random();
            		Coordinate randC = new Coordinate( p.getPosition().getX() + rand.nextInt(2)- 1,  p.getPosition().getY() + rand.nextInt(2)- 1);
            		Coordinate c = bigPelletList.isEmpty() ? (smallPelletList.isEmpty() ? randC : smallPelletList.get(0).coordinate) : bigPelletList.get(0).coordinate;
            		p.sendTo(c, sb);
            		pelletList.remove(new Pellet(c, 0));
            	// Altrimenti prosegui
            	} else {
                    p.sendTo(p.getDestination(), sb);
                }
            	//Aggiorna ultima posizione
            	p.setLastPosition(p.getPosition());
            }
            
            
            if (sb.length() != 0)
            	sb.deleteCharAt(sb.length()-1);
            
            System.out.println(sb.toString());
        }
    }

	
}