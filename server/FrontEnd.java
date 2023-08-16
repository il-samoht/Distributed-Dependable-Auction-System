import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Random;


public class FrontEnd implements Auction{
    ReplicaCommunication replica;
    String primaryReplicaName;
    public FrontEnd(){
        super();
        connectToNewPrimaryReplica(findReplicas());
    }
    public static void main(String[] args) {
        try {
            FrontEnd f = new FrontEnd();
            System.setProperty("java.rmi.server.hostname","127.0.0.1");
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(f, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready FRONTEND");
            
            
        }catch(Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    private String[] findReplicas(){
        try{
            Registry registry = LocateRegistry.getRegistry();
            String[] services = registry.list();
            List<String> replicaIDs = new ArrayList<>();
            System.out.println("findReplicas() registry port: " + registry.REGISTRY_PORT);
            //
            for(int i = 0; i < services.length; i++){
                //System.out.println(services[i]);
                String[] split = services[i].split("_");
                if(split[0].equals("Replica")){
                    replicaIDs.add(split[1]);
                }
            }

            //test
            /* 
            System.out.println("ReplicaIDs num = " + replicaIDs.size());
            for(int i = 0; i < replicaIDs.size(); i++){
                System.out.print(replicaIDs.get(i));
            }
            */
            String[] idList = new String[replicaIDs.size()];
            for(int i = 0; i < idList.length; i++){
                idList[i] = replicaIDs.get(i);
            }
            System.out.println("size of idList: " + idList.length);
            return idList;
        }catch(Exception e){
            return null;
        }  
    }

    private boolean connectToNewPrimaryReplica(String[] idList){
        try{
            //if it is the first time calling this function then find a new list using findReplicas that would save the available list in global
            //if it failed to connect to the first item in the list then remove the first option from the list and call the function again
            //function then would try to connect to the first item in the list that used to be the second item
            //if the list runs out of item then that means there are no replicas online
            
            Random rand = new Random();
            
            String name = "Replica_" + idList[rand.nextInt(idList.length)];

            this.primaryReplicaName = name;
            Registry registry = LocateRegistry.getRegistry();
            System.out.println("connecToNPR() registry port: " + registry.REGISTRY_PORT);
            this.replica = (ReplicaCommunication) registry.lookup(name);

            System.out.println("Connected to " + name);
            //test
            //System.out.println(firstCall);
            StackTraceElement[] st = new Throwable().getStackTrace();
            for(int i = 0; i < st.length; i++){
                //System.out.println(st[i]);
            }
            //test
            return true;
        }catch(Exception e){
            if(idList.length > 0){
                String[] new_idList = Arrays.copyOfRange(idList, 0, idList.length-1);
                System.out.println("idList size : " + idList.length);
                return connectToNewPrimaryReplica(new_idList);
            }
            System.out.println("No replica online");
            e.printStackTrace();
            return false;
        }
    }
    public NewUserInfo newUser(String email){
        try{
            return replica.newUser(email);
        }catch(Exception e){
            e.printStackTrace();
            if(connectToNewPrimaryReplica(findReplicas())){
                return newUser(email);
            }else{
                return null;
            }
        }
    }
    public byte[] challenge(int userID){
        try{
            return null;
        }catch(Exception e){
            throw e;
        }
    }
    public boolean authenticate(int userID, byte signature[]){
        try{
            return true;
        }catch(Exception e){
            throw e;
        }
    }
    public AuctionItem getSpec(int itemID){
        try{
            return replica.getSpec(itemID);
        }catch(Exception e){
            if(connectToNewPrimaryReplica(findReplicas())){
                return getSpec(itemID);
            }else{
                return null;
            }
        }
    }
    public int newAuction(int userID, AuctionSaleItem item){
        try{
            return replica.newAuction(userID, item);
        }catch(Exception e){
            if(connectToNewPrimaryReplica(findReplicas())){
                return newAuction(userID, item);
            }else{
                return -1;
            }
        }
    }
    public AuctionItem[] listItems(){
        try{
            System.out.println("FrontEnd listItems() called");
            AuctionItem[] items = replica.listItems();
            
            return replica.listItems();
        }catch(Exception e){
            e.printStackTrace();
            
            if(connectToNewPrimaryReplica(findReplicas())){
                return listItems();
            }else{
                return null;
            }
        }
    }
    public AuctionCloseInfo closeAuction(int userID, int itemID){
        try{
            return replica.closeAuction(userID, itemID);
        }catch(Exception e){
            if(connectToNewPrimaryReplica(findReplicas())){
                return closeAuction(userID, itemID);
            }else{
                return null;
            }
        }
    }
    public boolean bid(int userID, int itemID, int price){
        try{
            return replica.bid(userID, itemID, price);
        }catch(Exception e){
            if(connectToNewPrimaryReplica(findReplicas())){
                return bid(userID, itemID, price);
            }else{
                return false;
            }
        }
    }
    public int getPrimaryReplicaID(){
        try{
            return Integer.parseInt(primaryReplicaName.split("_")[1]);
        }catch(Exception e){
            throw e;
        }
    }

    /*
    public void shareAuctions(ActiveAuction[] auctions){
        try{
            
        }catch(Exception e){
            throw e;
        }
    }
    public void shareUsers(User[] users){
        try{
            
        }catch(Exception e){
            throw e;
        }
    }
    */
}
