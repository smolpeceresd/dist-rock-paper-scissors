import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import java.io.*;
import java.util.Properties;

import RockPaperScissors.GameController;
import RockPaperScissors.GameControllerHelper;

public class RPSDistClient {
	public static void main(String args[]) {
		int partidas = 0;
		int perdido = 0;
		int ganado = 0;
		try {
			// initialise props
			Properties props = new Properties();
			props.put("org.omg.CORBA.ORBInitialPort", "1050");
			org.omg.CORBA.ORB orb = ORB.init(args, props);

			// get name service reference
			org.omg.CORBA.Object nameObj = orb
					.resolve_initial_references("NameService");
			NamingContext rootCtx = NamingContextHelper.narrow(nameObj);// orb.resolve_initial_references("NameService"));//nameObj);
			NameComponent[] name = new NameComponent[2];

			// get object reference from its location in the tree
			name[0] = new NameComponent("Context1", "Context");
			name[1] = new NameComponent("Game", "Object");
			org.omg.CORBA.Object corbaObject = rootCtx.resolve(name);
			GameController gameController = GameControllerHelper
					.narrow(corbaObject);

			int menuChoice;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));

			do {
				partidas = 0;
				ganado = 0;
				perdido = 0;
				System.out.println("Welcome to the Online Distributed Rock Paper Scissors game!\n");
				System.out.println("Select An Option");
				System.out.println("1. Find an opponent");
				System.out.println("2. Quit");
				menuChoice = Integer.parseInt(reader.readLine());

				switch (menuChoice) {
					case 1: { // Find an opponent
						System.out.println("Finding an opponent... Please wait.");

						// NewGame method returns a string which is the users name for the game
						// and an Any which will contain the ID of the game
						AnyHolder a = new AnyHolder();
						// null is used in place of the callback that the GUI client would use
						String userName = gameController.NewGame(a);

						// use TypeCodes to make sure we get the right type back from the servant
						TCKind longTC = TCKind.tk_long;
						if (!(a.value.type().kind() == longTC)) {
							// return some information and exit the program
							System.out.println("Error: Wrong TypeCode returned!");
							System.out.println("Please try again.");
							return;
						}
						int GameId = a.value.extract_long();

						// uncomment this if you wish the client to print the user's name and game id
						// System.out.println("Your username for game with ID " + GameId
						// + " is " + userName);

						// check the servant every 0.5s to see if the game is ready
						// using a synchronous blocking call
						boolean opponentFound = false;
						while (!opponentFound) {
							Thread.sleep(500);
							if (gameController.gameIsReadySynchronous(GameId)) {
								System.out.println("SOMETHING IS HAPPENING!!");
								opponentFound = true;
							}
						}

						// opponent has been found
						System.out.println("Opponent Found!\n");
						do {
							partidas=partidas+1;
							System.out.println("Please Pick your Move! You have approximately 5 seconds!");
							System.out.println("1. Rock");
							System.out.println("2. Paper");
							System.out.println("3. Scissors");
							int subMenuChoice = Integer.parseInt(reader.readLine());

							// if user entered a valid choice
							if (subMenuChoice > 0 && subMenuChoice < 4) {
								// result contains the name of the winner, or "tie"
								String result = gameController.SynchronousResult(GameId,
										subMenuChoice, userName);

								// uncomment this if you wish the client to print the response
								// System.out.println("result is: " + result);
								if (result.equals("tie")) {
									System.out.println("It was a tie!");
								} else {
									boolean winner = (userName.equals(result));
									if (winner) {
										System.out.println("You Won!");
										ganado = ganado + 1;
									} else {
										System.out.println("You Lost! :(");
										perdido = perdido + 1;
									}
								}
							} else {
								System.out.println("You entered an invalid number.\n");
							}
							System.out.println(
									"----------------------------------------------------------------------------------");
							System.out.println("GAME: " + partidas);
							System.out.println("Win: " + ganado);
							System.out.println("LOSE: " + perdido);
							System.out.println(
									"----------------------------------------------------------------------------------");
						} while (ganado != 3 && perdido !=3 );
						if(ganado == 3){
							System.out.println("YOU ARE THE WINNER!!!!!!");
						}else{
							System.out.println("YOU LOOSE :P");
						}
					} // End case 1
				}// End switch
			} while (menuChoice != 2);
		} catch (Exception e) {
			System.err.println("Error : \n" + e);
			e.printStackTrace(System.out);
		}
	}
}
