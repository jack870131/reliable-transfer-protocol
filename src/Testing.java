import java.io.*;

public class Testing
{
    public final static void main(String[] argv)
    {
        NetworkSimulator simulator;
        
        int nsim = Integer.parseInt(argv[0]);
        double loss = Double.parseDouble(argv[1]);
        double corrupt = Double.parseDouble(argv[2]);
        double delay = Double.parseDouble(argv[3]);
        int trace = Integer.parseInt(argv[4]);
        long seed = Long.parseLong(argv[5]);
    
                                   
        System.out.println("******************************************************************************");
        System.out.println("******** Running network simulator with parameters *********");
        System.out.println("******************************************************************************");
        System.out.println("nsim="+nsim);
        System.out.println("loss="+loss);
        System.out.println("corrupt="+corrupt);
        System.out.println("delay="+delay);
        System.out.println("trace="+trace);
        System.out.println("seed="+seed);
         
        simulator = new NetworkSimulator(nsim, loss, corrupt, delay,
                                                trace, seed);
                                                
        simulator.runSimulator();
        System.out.println("******* End of Simulation with parameters *********");
        System.out.println("nsim="+nsim);
        System.out.println("loss="+loss);
        System.out.println("corrupt="+corrupt);
        System.out.println("delay="+delay);
        System.out.println("trace="+trace);
        System.out.println("seed="+seed);
        System.out.println("*********************************************");
        System.out.println("Received Data ******************************");
        System.out.println(simulator.getReceivedData());
        System.out.println("*********************************************");
        System.out.println("Simulation time:"+simulator.getTime());
        System.out.println("******************************************************************************");
        

    }
}
