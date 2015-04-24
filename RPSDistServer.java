import java.util.Properties;
import org.omg.CORBA.*;
import RockPaperScissors.*;
import org.omg.CosNaming.* ;

public class RPSDistServer{

    public static void main (String args[]) {
        System.out.println("Server starting...");
        try{
        	//initialise props
			Properties props = new Properties();
			props.put("org.omg.CORBA.ORBInitialPort", "1050");
			//initialise orb
            org.omg.CORBA.ORB orb = ORB.init(args, props);
            
            //get NameService object
            org.omg.CORBA.Object nameObj=orb.resolve_initial_references("NameService");

            //build tree as follows root->Context1->Game
            NameComponent nc[] = new NameComponent[1];
            NamingContext rootCtx=NamingContextHelper.narrow(nameObj);

            GameController game = new GameController_Tie(new RPSDistServant(orb));

            nc[0] = new NameComponent("Context1", "Context");
            NamingContext Ctx1 = rootCtx.bind_new_context(nc);

            nc[0] = new NameComponent("Game", "Object");
            Ctx1.rebind(nc, game);
            
            orb.run();

        }catch (Exception e){
                System.err.println("Error : "+e);
                e.printStackTrace(System.out);
        }
    }
}
