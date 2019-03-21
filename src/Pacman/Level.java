package Pacman;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Short class description
 * 
 * @author Nicolas FONNIER, Henri GLEVEAU
 *
 */
public class Level {
	
	/**
	 * Number of levels
	 */
	protected static final int MAX_INDEX = 3;
	
	/**
	 * The name of the score file
	 */
	protected static final String SCORE_FILE = "score.dat"; 
	
	/**
	 * Holds the number of the current level
	 */
	protected int index;
	
	/**
	 * Holds all the squares of the board
	 */
	protected ArrayList<ArrayList<Case>> list; 
	
	/**
	 * Holds a reference to the pacman object
	 */
	protected Pac_Man pacman;
	
	/**
	 * Holds a reference to all the ghosts on the board
	 */
	protected ArrayList<Fantome> ghostList;
	
	/**
	 * Holds a reference to all the bonus entities on the board
	 * Allows for fast checking of end game state
	 */
	protected ArrayList<BonusEntity> bonusList;
	
	/**
	 * Level constructor
	 * 
	 * No param needed
	 */
	public Level() {
		this.list = new ArrayList<ArrayList<Case>>();
		this.pacman = null;
		this.ghostList = new ArrayList<Fantome>();
		this.bonusList = new ArrayList<BonusEntity>();
		index = 1;
		
		this.changeList();
	}
	
	/**
	 * Runs through the list and draws each square.
	 * Draws, in order: Squares, bonus, ghost, Pacman
	 */
	public void drawList() {		
		//Drawing all the board squares		
		for(ArrayList<Case> caseList : list) {
			for(Case boardCase : caseList) {
				boardCase.draw();
			}
		}
		//Drawing every bonus entity
		for(BonusEntity bonus : bonusList) {
			bonus.draw();
		}
		
		//Drawing every ghost
		for(Fantome fantome : ghostList) {
			fantome.draw();
		}
		
		//Drawing PacMan
		pacman.draw();
	}
	
	/**
	 * Loads the file that contains the level corresponding to the index
	 * 
	 */
	public void changeList() {
		assert(index > 0) : "Invalid index : Negative.";
		assert(index < MAX_INDEX) : "Invalid index : Greater than MAX_INDEX";
		
		//Loads the file, then puts it in the fileText string
		InputStreamReader fileInput = null;
		try {
			fileInput = new InputStreamReader(new FileInputStream("Level" + index));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the level: " + this.index);
			e.printStackTrace();
		}
		
		String fileText = "";
		char[] buffer = new char[1024];
		
		if(fileInput != null) {
			int len = 0;
			try {
				len = fileInput.read(buffer);
			} catch (IOException e) {
				System.out.println("Error while reading first kB of file");
				e.printStackTrace();
			}
			
			while(len != -1) {
				try {
					len = fileInput.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileText += String.valueOf(buffer);
			}
		}
		
		//File parsing
		String[] lines = fileText.split("\n");
		int i = 0;
		int j = 0;
		int caseNbOnLine = 0;
		for(String line : lines) {
			//Checking if line definition
			if(line.toCharArray()[0] == 'f' || line.toCharArray()[0] == 't') {
				
				ArrayList<Case> caseLine = new ArrayList<Case>();
				//Allows to work on each character separately
				for(char caseDef : line.toCharArray()) {
					
					//Checking in case garbage was slipped inside a line def
					if(caseDef == 'f' || caseDef == 't') {
						
						caseLine.add(new Case(caseDef == 't', null, null, null,  i, j));
						if(caseLine.get(caseNbOnLine).isWalkable()) {
							//If the square is walkable, set a simple bonus on the square
							BonusEntity newBonus = new BonusEntity("s", this.index, i-11, j-9);
							this.bonusList.add(newBonus);
							caseLine.get(caseNbOnLine).setBonus(newBonus);
						}
					}
					caseNbOnLine++;
					i+=10;
				}
				
				caseNbOnLine=0;
				list.add(caseLine);
				System.out.println(caseLine.size());
				i=0;
				j=j+10;
			}
			
			
			//Checking if Pacman location
			if(line.toCharArray()[0] == 'P') {
				//Getting the X and Y location of Pacman
				String[] pacLocLine = line.split(" ");
				int x = Integer.parseInt(pacLocLine[1].trim());
				int y = Integer.parseInt(pacLocLine[2].trim());
				
				assert(x > 0) : "X location of Pacman negative";
				assert(y > 0) : "Y location of Pacman negative";
				assert(x < this.list.size()) : "X location of Pacman greater than board maximum";
				assert(y >  this.list.get(0).size()) : "Y location of Pacman greater than board maximum";
				
				System.out.println("Pacman detected @ " + x + "," + y);
				
				//Setting the Pacman on the right board square
				if(this.list.get(x).get(y).getBonus()!=null) {
					this.list.get(x).get(y).setBonus(null);
				}
				
				//Creating the Pacman we'll keep a reference to for later
				this.pacman = new Pac_Man(y*10,x*10);
				
				//Adding the Pacman to the square
				this.list.get(x).get(y).setPacMan(this.pacman);
			}
			
			//Checking if bonus location
			if(line.toCharArray()[0] == 'S') {
				//Getting the X and Y location of the ghost
				String[] bonusLocLine = line.split(" ");
				int x = Integer.parseInt(bonusLocLine[1].trim());
				int y = Integer.parseInt(bonusLocLine[2].trim());
				
				assert(x > 0) : "X location of bonus negative";
				assert(y > 0) : "Y location of bonus negative";
				assert(x < this.list.size()) : "X location of bonus greater than board maximum";
				assert(y >  this.list.get(0).size()) : "Y location of bonus greater than board maximum";
				
				System.out.println("Bonus detected @ " + x + "," + y);
				BonusEntity newBonus = new BonusEntity("S", this.index, y*10,x*10);
				this.bonusList.add(newBonus);
				this.list.get(x).get(y).setBonus(newBonus);
			}
			
			//Checking if ghost location
			if(line.toCharArray()[0] == 'F') {
				//Getting the X and Y location of the ghost
				String[] ghostLocLine = line.split(" ");
				int x = Integer.parseInt(ghostLocLine[1].trim());
				int y = Integer.parseInt(ghostLocLine[2].trim());
				
				assert(x > 0) : "X location of ghost negative";
				assert(y > 0) : "Y location of ghost negative";
				assert(x < this.list.size()) : "X location of ghost greater than board maximum";
				assert(y >  this.list.get(0).size()) : "Y location of ghost greater than board maximum";
				
				System.out.println("Ghost detected @ " + x + "," + y);
				
				//Setting the ghost on the right board square
				if(this.list.get(x).get(y).getBonus()!=null)
					this.list.get(x).get(y).setBonus(null);
				
				//Keeping a reference to the new ghost
				Fantome newGhost = new Fantome(y*10,x*10);
				this.ghostList.add(newGhost);
				
				//Adding it to the board
				this.list.get(x).get(y).setFantome(newGhost);
			}
		}
				
		System.out.println(list.size());
	}
	
	/**
	 * 
	 */
	//TODO : A Reorganiser en d�coupant dans les bonnes fonctions et en utilisant les bonnes m�thodes 
	public void computeNextFrame() {
		
		try {
			Thread.sleep(33);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: A METTRE DANS UPDATEGHOST
		for (Fantome ghost : ghostList) {
			int deplacement = (int) (1 + Math.random() * ( 5 - 1 ));
			switch(deplacement) {
				case 1:
					int x = (ghost.posHor/10)-1;
					int y = ghost.posVer/10;
					if(this.list.get(y).get(x).isWalkable()) {
						//this.list.get(y).get(x+1).setBonus(new BonusEntity("", this.index, y*10, x*10));
						this.list.get(y).get(x+1).draw();
						BonusEntity bonus = this.list.get(y).get(x+1).getBonus();
						if(bonus!=null) {
							bonus.draw();
						}
						ghost.posHor -= 10;
						ghost.draw();
						
					}
					break;
				case 2:
					x = (ghost.posHor/10);
					y = (ghost.posVer/10)-1;
					if(this.list.get(y).get(x).isWalkable()) {
						//this.list.get(y+1).get(x).setBonus(new BonusEntity("", this.index, y*10, x*10));
						this.list.get(y+1).get(x).draw();
						BonusEntity bonus = this.list.get(y+1).get(x).getBonus();
						if(bonus!=null) {
							bonus.draw();
						}
						ghost.posVer -= 10;
						//System.out.println(ghost.checkCollision(ghost, pacman));
						ghost.draw();
					}
					break;
				case 3:
					x = (ghost.posHor/10)+1;
					y = (ghost.posVer/10);
					if(this.list.get(y).get(x).isWalkable()) {
						//this.list.get(y).get(x-1).setBonus(new BonusEntity("", this.index, y*10, x*10));
						this.list.get(y).get(x-1).draw();
						BonusEntity bonus = this.list.get(y).get(x-1).getBonus();
						if(bonus!=null) {
							bonus.draw();
						}
						ghost.posHor += 10;
						ghost.draw();
					}
					break;
				case 4:
					x = (ghost.posHor/10);
					y = (ghost.posVer/10)+1;
					if(this.list.get(y).get(x).isWalkable()) {
						//this.list.get(y-1).get(x).setBonus(new BonusEntity("", this.index, y*10, x*10));
						this.list.get(y-1).get(x).draw();
						BonusEntity bonus = this.list.get(y-1).get(x).getBonus();
						if(bonus!=null) {
							bonus.draw();
						}
						ghost.posVer += 10;		
						ghost.draw();
					}
					break;
				}
			
			}
		//Setting the label to the correct score
		Canvas.getCanvas().getScoreLabel().setText("Score : " + Integer.toString(this.pacman.getScore()));
	}
	
	/**
	 * 
	 */
	public void updateGhost() {
		ArrayList<Fantome> ghostlist = new ArrayList<Fantome>();
		
		for(ArrayList<Case> caseList : list) {
			for(Case boardCase : caseList) {
				if(boardCase.getPacMan() != null) {
				}
				if(boardCase.getFantome() != null) {
					ghostlist.add(boardCase.getFantome());
				}
			}
		}
		int i = 0;
		int z = 0;
		for(Fantome ghost : ghostlist) {
			int x = ghost.posHor;
			int y = ghost.posVer;
			this.list.get(14).get(11+i).setFantome(ghost);
			ghost.posHor = 110+z;
			ghost.posVer  =140;
			ghost.draw();
			this.list.get(x/10).get(y/10).setFantome(null);
		}
		i=i+1;
		z=z+10;
	}
	
	/**
	 * 
	 */
	public void updatePacMan() {
		boolean gauche = Canvas.getCanvas().isLeftPressed();
		boolean droite = Canvas.getCanvas().isRightPressed();
		boolean up = Canvas.getCanvas().isUpPressed();
		boolean down = Canvas.getCanvas().isDownPressed();
		
		int x = pacman.getX()/10;
		int y = pacman.getY()/10;
		
		Case currSquare = this.list.get(x).get(y);
		
		for(Fantome ghost : ghostList) {
			if(ghost.checkCollision(pacman)) {
				//Removing the pacman from the current square
				currSquare.setPacMan(null);
				
				//Moving it to the beginning position
				this.list.get(23).get(13).setPacMan(this.pacman);
				this.pacman.moveTo(130, 230);
				
				//Removing 1 life
				pacman.perdVie();
				
				//Drawing everything back.
				this.drawList();			
			}
		}
		
		if(gauche) {
			x = (pacman.getX()/10)-1;
			y = pacman.getY()/10;			
			if(this.list.get(y).get(x).isWalkable()) {
				this.list.get(y).get(x+1).draw();
				pacman.posHor -= 10;
				if(this.list.get(y).get(x).getBonus()!=null) {
					pacman.addscore(10);
					this.list.get(y).get(x).setBonus(null);
				}
				pacman.draw();
			}
		}
		if(droite) {
			x = (pacman.posHor/10)+1;
			y = pacman.posVer/10;
			if(this.list.get(y).get(x).isWalkable()) {
				this.list.get(y).get(x-1).draw();
				
				pacman.posHor += 10;
				if(this.list.get(y).get(x).getBonus()!=null) {
					pacman.addscore(10);
					this.list.get(y).get(x).setBonus(null);
				}
				pacman.draw();
			}
		}
		if(up) {
			x = (pacman.posHor/10);
			y = (pacman.posVer/10)-1;
			if(this.list.get(y).get(x).isWalkable()) {
				this.list.get(y+1).get(x).draw();
				
				pacman.posVer -= 10;
				if(this.list.get(y).get(x).getBonus()!=null) {
					pacman.addscore(10);
					this.list.get(y).get(x).setBonus(null);
				}
				pacman.draw();
			}
		}
		if(down) {
			x = (pacman.posHor/10);
			y = (pacman.posVer/10)+1;
			if(this.list.get(y).get(x).isWalkable()) {
				this.list.get(y-1).get(x).draw();
				
				pacman.posVer += 10;
				if(this.list.get(y).get(x).getBonus()!=null) {
					pacman.addscore(10);
					this.list.get(y).get(x).setBonus(null);
				}
				pacman.draw();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int checkEndGame() {
		if(this.pacman.getPacManLives() <= 0)
			return 1;
		
		//Checking if there are any bonuses left. If there are none, change to next level
		//and return 2
		
		
		return 0; 
	}
	
	/**
	 * Fetches the score from pacman, and writes it to scores.dat
	 */
	public void writeScore() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(Level.SCORE_FILE, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//If the writer was correctly setup
		if(bw != null) {
			try {
				System.out.println(this.pacman.getScore());
				bw.write(Integer.toString(this.pacman.getScore()) + "\n");
				bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				//Whatever happened, try to close the stream and release the file
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Point of entry
	 * 
	 * @param args Automatically sent by the system when the program is called
	 */
	public static void main(String[] args) {/*
		// TODO Auto-generated method stub
		Level board = new Level();
		Canvas.getCanvas().wait(50);
		board.drawList();		*/
		
		// TODO Auto-generated method stub
		Level board = new Level();
		Canvas.getCanvas().wait(50);
		board.drawList();
		Pac_Man pacman = null;
		for(ArrayList<Case> caseList : board.list) {
			for(Case boardCase : caseList) {
				if(boardCase.getPacMan() != null) {
					pacman = boardCase.getPacMan();
				}
			}
		}
		
		while(pacman.getPacManLives()>0) {
			Canvas.getCanvas().wait(50);
			board.computeNextFrame();
			board.updatePacMan();
			
		}
		
		board.writeScore();
		
		System.out.println("Fin du game");
	}

}
