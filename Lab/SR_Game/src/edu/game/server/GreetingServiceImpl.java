package edu.game.server;

import java.util.Random;
import java.util.HashMap;

import edu.game.client.GreetingService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
	private final short _nbMaxPlayers = 4;
	private int _gridx = 20;
	private int _gridy = 20;
	private int _nbCookies = 50;
	private byte _nbPlayers;
	private HashMap<Byte,int[]> _mapPlayers;
	private byte[][] _grid;
	private short[] _score;

	/**
	 * Constructor avec This server
	 */
	public GreetingServiceImpl() {
		_nbPlayers = 0;
		_grid=new byte[_gridx][_gridy];
		_score = new short[_nbMaxPlayers];
		_mapPlayers = new HashMap<Byte,int[]>();
		init();
	}
	
	public void init(){
		initGrid(_nbCookies);
		for(int i=0;i<_nbMaxPlayers;i++){
			_score[i]=0;
		}
	}

	/**
	 * Insert new Player in the grid
	 * @param nID : ID of the new player
	 * @return localisation
	 */
	private int[] newPlayer(byte nID){
		int[] coord = new int[2];
		switch(nID){
		case 1:
			coord[0]=0;
			coord[1]=0;
			break;
		case 2:
			coord[0]=_gridx-1;
			coord[1]=_gridy-1;
			break;
		case 3:
			coord[0]=_gridx-1;
			coord[1]=0;
			break;
		case 4:
			coord[0]=0;
			coord[1]=_gridy-1;
			break;
		}

		_mapPlayers.put(nID, coord);
		System.out.println("new Player "+nID+" : ["+coord[0]+"]["+coord[1]+"]");
		_grid[coord[0]][coord[1]]=(byte) (nID+10);

		return coord;
	}


	/**
	 * Initialise the grid with cookies 
	 * @param nbCookies : number of cookies to put
	 */
	private void initGrid(int nbCookies){
		//Init empty grid
		for(int i=0;i<_gridx;i++){
			for(int j=0;j<_gridy;j++){
				_grid[i][j]=0;
			}			
		}
		// add cookies in the grid
		Random r = new Random();
		for(int i=0;i<nbCookies;i++){
			int x = r.nextInt(_gridx);	
			int y = r.nextInt(_gridy);
			//System.out.println("Cookie "+x+":"+y);
			if(_grid[x][y]==1) i--;
			else _grid[x][y]=1;
		}
	}

	private boolean isInOfRange(int [] coord){
		return !(coord[0]<0 & coord[0]>_gridx & coord[1]>_gridy & coord[1]<0);
	}

	private boolean isPossibleToMove(int[] coord){
		return isInOfRange(coord)&_grid[coord[0]][coord[1]] <2;
	}

	/**
	 * Move the player in the grid if it's possible
	 * @param myID 		: ID of player
	 * @param oldCoord 	: Localisation of player
	 * @param newCoord 	: next move of player
	 * @return			: if player had moved
	 */
	private boolean movePlayerTo(byte myID, int[] oldCoord,int[] newCoord){
		if(isPossibleToMove(newCoord)){
			if(_grid[newCoord[0]][newCoord[1]]==1){ // There is a cookie
				_score[myID]++;
			}
			_grid[oldCoord[0]][oldCoord[1]] = 0;
			_grid[newCoord[0]][newCoord[1]] = (byte) (myID+10);
			_mapPlayers.put(myID, newCoord);
			return true;
		}
		return false;
	}


	// ##############################################################
	// ################		Call From Client 	#####################
	// ##############################################################

	/**
	 * Call from client to register player in the grid
	 * @return the player ID
	 */
	public byte registerMe(){
		if(_mapPlayers.size()<_nbMaxPlayers){
			_nbPlayers++;
			newPlayer(_nbPlayers);
			return _nbPlayers;
		}
		return -1;	// the grid is full of players
	}

	@Override
	public boolean moveUp(byte myID) {
		int [] myCoord = _mapPlayers.get(myID);
		int [] newCoord = {myCoord[0],myCoord[1]-1};
		return movePlayerTo(myID, myCoord, newCoord);
	}

	@Override
	public boolean moveDown(byte myID) {
		int [] myCoord = _mapPlayers.get(myID);
		int [] newCoord = {myCoord[0],myCoord[1]+1};
		return movePlayerTo(myID, myCoord, newCoord);
	}

	@Override
	public boolean moveLeft(byte myID) {
		int [] myCoord = _mapPlayers.get(myID);
		int [] newCoord = {myCoord[0]-1,myCoord[1]};
		return movePlayerTo(myID, myCoord, newCoord);
	}

	@Override
	public boolean moveRight(byte myID) {
		int [] myCoord = _mapPlayers.get(myID);
		int [] newCoord = {myCoord[0]+1,myCoord[1]};
		return movePlayerTo(myID, myCoord, newCoord);
	}

	@Override
	public byte[][] getGrid() {
		return _grid;
	}

	@Override
	public short[] getScore() {
		return _score;
		
	}
}
