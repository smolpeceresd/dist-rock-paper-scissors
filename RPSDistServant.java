import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.ORB;

import RockPaperScissors.*;


public class RPSDistServant implements GameControllerOperations{

	public static ORB orb;
	//in memory data structure of game objects
	List<Game> Games;
	//counter will keep track of game id's - id will be index of the game in the list
	int counter;
	//Queue is the amount of people currently waiting to play
	int Queue;
	
	public RPSDistServant(ORB orb) {
		counter = 0;
		this.orb = orb;
		Games = new ArrayList<Game>();
	}
	
	@Override
	public String NewGame(AnyHolder gameIdAny){
		//create any to hold game ID
		Any gameId = orb.create_any();
		gameId.insert_long(counter);
		gameIdAny.value = gameId;
		
		//initialise string of user name
		String user = "";
		if(Queue == 0){
			//nobody else in the queue - game object not created yet
			Queue = 1;
			user = "User1";
		} else if (Queue==1){
			//opponent found create game object
			Queue = 0;
			user = "User2";
			Game game = new Game();
			game.GameId = counter;
			game.timer = 0;
			
			//add game to list
			Games.add(game);
			counter++;
		}
		return user;
	}
	
	@Override
	public boolean gameIsReadySynchronous(int GameID) {
		//the game does not get added to the game list until there are two players
		//therefore if it is in the list, there are two players ready
		for(Game g : Games){
			if(g.GameId == GameID){
				return true;			
			}
		}
		return false;
	}
	
	public void gameIsReady(GUICallback callback, int gameId){
		boolean gameReady = false;
		while(!gameReady){
			//without sleeping, the method will break the system
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(Game g : Games){
				//if there is a game with the ID passed to the method, callback
				if(g.GameId == gameId){
					gameReady = true;
					callback.callBack("");
				}
			}
		}
	}

	//recursive function to get winner
	@Override
	public String SynchronousResult(int GameID, int move, String userName) {		
		//game will be the requested game
		Game game = null;
		int length = Games.size();
		boolean GameFound = false;
		
		for(int x = 0; x < length; x++){
			Game element = Games.get(x);
			//find matched game
			if(element.GameId == GameID){
				game = element;
				if(userName.equals("User1")){
					game.User1Move = getMoveById(move);
				} else if(userName.equals("User2")){
					game.User2Move = getMoveById(move);
				}
				GameFound = true;
				Games.set(x, game);
				break;
			}
		}
		
		if(game != null && GameFound){
			//wait 1 second - increases chance  both users will have picked
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//get both moves from game object
			String user1Move = game.User1Move;
			String user2Move = game.User2Move;
			
			//if both users have answered within ~5 seconds, return the winner
			if(user1Move != null && user2Move != null && game.timer <=5){
				return determineWinner(user1Move, user2Move);
			} else if(game.timer <= 5){ //otherwise give the user who hasn't answered more time
				game.timer++;
				Games.set(game.GameId, game);
				return SynchronousResult(game.GameId, move, userName);
			} else { //otherwise let the user who gave a move win
				if(userName.equals("User1") && user2Move == null){
					return "User1";
				} else if(userName.equals("User2") && user1Move == null){
					return "User2";
				}else if((userName.equals("User1") && user2Move == null)&&(userName.equals("User2") && user1Move == null)){
					return "tie";
				}
			}
		}
		//if we get this far just return a string - will count as a loss
		return "Error!";

	}
	
	//async result - same as above method except calls the callback instead of returning string
	@Override
	public void Result(GUICallback callback, int GameID, int move, String userName) {
		Game game = null;
		int length = Games.size();
		boolean GameFound = false;
		
		for(int x = 0; x < length; x++){
			Game element = Games.get(x);
			//find matched game
			if(element.GameId == GameID){
				game = element;
				if(userName.equals("User1")){
					game.User1Move = getMoveById(move);
				} else if(userName.equals("User2")){
					game.User2Move = getMoveById(move);
				}
				GameFound = true;
				Games.set(x, game);
				break;
			}
		}
		
		if(game != null && GameFound){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String user1Move = game.User1Move;
			String user2Move = game.User2Move;
			
			if(user1Move != null && user2Move != null && game.timer <=5){
				 callback.callBack(determineWinner(user1Move, user2Move));
				 return;
			} else if(game.timer <= 5){
				game.timer++;
				Games.set(game.GameId, game);
				Result(callback, game.GameId, move, userName);
			} else {
				if(userName.equals("User1") && user2Move == null){
					callback.callBack("User1");
					return;
				} else if(userName.equals("User2") && user1Move == null){
					callback.callBack("User2");
					return;
				}
			}
		}
	}
	
	//each move has an INT associated with it - 1 is rock, 2 is paper, 3 is scissors
	private String getMoveById(int moveId){
		String move = null;
		switch(moveId){
		case 1:
			move = "Rock";
			break;
		case 2:
			move = "Paper";
			break;
		case 3:
			move = "Scissors";
			break;
		}
		
		return move;
	}
	
	//determine which player won based on each player's hand
	private String determineWinner(String hand1, String hand2){
		String user1 = "User1";
		String user2 = "User2";
		String winner = null;
		
		switch(hand1){
		case "Rock":
			if(hand2 == "Rock"){
				winner = "tie";
			} else if (hand2 == "Paper"){
				winner = user2;
			} else if (hand2 == "Scissors"){
				winner = user1;
			}
			break;
		case "Paper":
			if(hand2 == "Rock"){
				winner = user1;
			} else if (hand2 == "Paper"){
				winner = "tie";
			} else if (hand2 == "Scissors"){
				winner = user2;
			}
			break;
		case "Scissors":
			if(hand2 == "Rock"){
				winner = user2;
			} else if (hand2 == "Paper"){
				winner = user1;
			} else if (hand2 == "Scissors"){
				winner = "tie";
			}
			break;
		}
		
		return winner;
	}
}
