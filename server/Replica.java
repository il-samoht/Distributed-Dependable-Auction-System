import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.xml.catalog.Catalog;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.KeyPair;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Signature;
import java.io.FileInputStream;
import java.util.Arrays;
import java.io.Serializable;
import java.net.ConnectException;


public class Replica implements ReplicaCommunication, Serializable{
    List<ActiveAuction> auctions = new ArrayList<>();
    List<User> users = new ArrayList<>();
    int ReplicaID;
    boolean isPrimary = false;

    public Replica(int  ReplicaID) {
        super();
        this.ReplicaID = ReplicaID;
        connectToRMI(ReplicaID);
        
    }
  
    public AuctionItem getSpec(int itemID){
        shareData(findReplicas());
        isPrimary = true;

        AuctionItem item;
        try{
            
            for (int i = 0; i < this.auctions.size(); i++){
                item = auctions.get(i).auctionItem;
                if(item.itemID == itemID){
                    return item;
                    //return encry(item);
                }
            }
            throw new Exception("Item doesn't exist");
        }catch(Exception e){
            
        }
        
        return null;
    }
  
    public static void main(String[] args) {
        try {
            Replica s = new Replica(Integer.parseInt(args[0]));

            GenerateServerKey();

            
            
        }catch(Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
            
        }
    }
    private void connectToRMI(int ReplicaID){
        try{
            System.setProperty("java.rmi.server.hostname","127.0.0.1");
            String name = "Replica_" + ReplicaID;
            ReplicaCommunication stub = (ReplicaCommunication) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready Server " + name);
            System.out.println("connectToRMI() registry port: " + registry.REGISTRY_PORT);
        }catch(Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
        
    }
    private void rebindRMI(){
        System.out.println("rebindRMI() called");
        try{
            String name = "Replica_" + ReplicaID;
            Registry registry = LocateRegistry.getRegistry();
            ReplicaCommunication stub = (ReplicaCommunication) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(name, stub);
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    public int getPrimaryReplicaID(){
        try{
            return 0;
        }catch(Exception e){
            throw e;
        }
    }

    private static void GenerateServerKey(){
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();
    
            //save public key
            File file = new File("../keys/server_public.key");
            byte[] rawData = pub.getEncoded();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(rawData);
            fos.flush();
            fos.close();
            /*
            String encodedKey = Base64.getEncoder().encodeToString(rawData);
            //System.out.println(encodedKey);
            filewrite.write(encodedKey);
            filewrite.close();
            */


            //save private key
            file = new File("../keys/server_private.key");
            rawData = pvt.getEncoded();
            fos = new FileOutputStream(file);
            fos.write(rawData);
            fos.flush();
            fos.close();
            /*
            encodedKey = Base64.getEncoder().encodeToString(rawData);
            //System.out.println(encodedKey);
            filewrite.write(encodedKey);
            filewrite.close();
            */
            
        }catch(Exception e){
            
        }
    }
    public SealedObject encry(AuctionItem obj){
        //shareData(findReplicas());
        isPrimary = true;
        try{
            SealedObject item;
            KeyGenerator key = KeyGenerator.getInstance("AES");
            //key.init(128);
            SecretKey aesKey = key.generateKey();
            //save key
            File file = new File("../keys/testKey.aes");
            FileWriter filewrite = new FileWriter("../keys/testKey.aes");
            byte[] rawData = aesKey.getEncoded();
            String encodedKey = Base64.getEncoder().encodeToString(rawData);
            //System.out.println(encodedKey);
            filewrite.write(encodedKey);
            filewrite.close();
            //
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            item = new SealedObject(obj, cipher);
            return item;
        }catch(Exception e){
            return null;
        }
        
    }

    public NewUserInfo newUser(String email){
        shareData(findReplicas());
        //System.out.println("findReplicas() length = " + findReplicas() == null);
        isPrimary = true;
        try{
            User new_user = new User(users.size() + 1, email);
            users.add(new_user);
            

            ////generate keys
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();
    
            //save public key
            new_user.getNewUserinfo().publicKey = pub.getEncoded();

            //save private key
            new_user.getNewUserinfo().privateKey = pvt.getEncoded();
            //challenge(1);
            return new_user.getNewUserinfo();

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        
    }

    public int newAuction(int userID, AuctionSaleItem item){
        shareData(findReplicas());
        isPrimary = true;
        try{
            ActiveAuction auction = new ActiveAuction(auctions.size()+1, item, userID);
            auctions.add(auction);
            
            return auction.getItemID();
        }catch(Exception e){
            throw e;
        }
        
     }

    public AuctionItem[] listItems(){
        //shareData(findReplicas());
        isPrimary = true;
        //System.out.println(ReplicaID + " : listItems() called");
        try{
            AuctionItem[] items = new AuctionItem[auctions.size()];
            for(int i = 0; i < auctions.size(); i++){
                items[i] = auctions.get(i).getAuctionItem();
            }
            return items;
        }catch(Exception e){
            System.out.println(ReplicaID + " : listItems() error");
            e.printStackTrace();
            return null;
        }

    }

    
    private int findAuctionIndex(int itemID){
        for(int i = 0; i < auctions.size(); i++){
            if(auctions.get(i).getItemID() == itemID){
                return i;
            }
        }
        return -1;
    }

    private String getUserEmail(int userID){
        for(int i = 0; i < users.size(); i++){
            if(users.get(i).getUserID() == userID){
                return users.get(i).getEmail();
            }
        }
        return null;
    }
    public AuctionCloseInfo closeAuction(int userID, int itemID){
        shareData(findReplicas());
        isPrimary = true;
        try{
            int index = findAuctionIndex(itemID);
            ActiveAuction auction = auctions.get(index);
            if(userID == auction.getAuctionHolder() && index >= 0){ //check if is the real owner
                if(auction.getHighestBid() >= auction.getReservePrice()){
                    AuctionCloseInfo aci = new AuctionCloseInfo();
                    aci.winningEmail = getUserEmail(auction.getAuctionHolder());
                    aci.winningPrice = auction.getHighestBid();
                    auctions.remove(index);
                    return aci;
                }
            }
            return null;
        }catch(Exception e){
            throw e;
        }
    }

    public boolean bid(int userID, int itemID, int price){
        shareData(findReplicas());
        isPrimary = true;
        int index = findAuctionIndex(itemID);
        ActiveAuction auction = auctions.get(index);
        System.out.println("auction holder = " + auction.getAuctionHolder());
        if(auction.getAuctionHolder() != userID){   //auction owner cannot bid
            System.out.println("highest bid = " + auction.getHighestBid());
            if(price > auction.getHighestBid()){    //bid has to be higher than current bid
                System.out.println("update highest bid");
                auction.updateHighestBid(price);
                auction.updateHighestBidder(userID);
                return true;
            }
        }
        return false;
    }

    public byte[] challenge(int userID){
        shareData(findReplicas());
        isPrimary = true;
        try{
            Signature sig = Signature.getInstance("auction");

            File file = new File("../keys/server_public.key");
            FileInputStream fis = new FileInputStream("../keys/server_public.key");
            byte[] privKey = new byte[(int) file.length()];
            fis.read(privKey);
            fis.close();
            for (int i = 0; i < privKey.length; i++)
            {
              System.out.print((char) privKey[i]);
            }

            //https://stackoverflow.com/questions/7224626/how-to-sign-string-with-private-key
            return null;
        }catch (Exception e){
            //throw e;
            return null;
        }
    }

    public boolean authenticate(int userID, byte signature[]){
        shareData(findReplicas());
        isPrimary = true;
        try{
            return false;
        }catch (Exception e){
            throw e;
        }
    }
    /*
    private void generateKeys(){
        //https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
    }
    */

    public void shareAuctions(ActiveAuction[] auctions){
        try{
            this.auctions = Arrays.asList(auctions);
            isPrimary = false;
            System.out.println("Replica " + ReplicaID + "receaved auctions data");
        }catch(Exception e){
            throw e;
        }
        
    }
    public void shareUsers(User[] users){
        try{
            this.users = Arrays.asList(users);
            isPrimary = false;
            System.out.println("Replica " + ReplicaID + "receaved users data");
        }catch(Exception e){
            throw e;
        }
        
    }
    private void shareData(String[] replicaIDs){ //shareData(findReplicas()) when called
        //find all the replicas and call the share functions on them
        //System.out.println("shareData() null pointer error replicaIDs.length" + replicaIDs.length);
        try {
            //prepare data convert to array
            ActiveAuction[] auctions = new ActiveAuction[this.auctions.size()];
            User[] users = new User[this.users.size()];

            for(int i = 0; i < auctions.length; i++){
                auctions[i] = this.auctions.get(i);
            }
            for(int i = 0; i < users.length; i++){
                users[i] = this.users.get(i);
            }
            
            //connect to each replica and call shareAuctions and shareUsers
            for(int i = 0; i < replicaIDs.length; i++){
                if(ReplicaID != Integer.parseInt(replicaIDs[i])){ //exclude own id
                    String name = "Replica_" + replicaIDs[i];
                    Registry registry = LocateRegistry.getRegistry();
                    ReplicaCommunication replica = (ReplicaCommunication) registry.lookup(name);

                    replica.shareAuctions(auctions);
                    replica.shareUsers((users));
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("shareData() catch e instanceof ConnectException" + (e instanceof java.rmi.ConnectException));
            System.out.println("e is an instance of " + (e.getClass().getCanonicalName()));
            e.printStackTrace();
            
            if(e instanceof java.rmi.ConnectException){
                rebindRMI();
            }
            System.out.println("shareData() null pointer error replicaIDs.length" + replicaIDs.length);
            if(replicaIDs.length > 1){
                shareData(Arrays.copyOfRange(replicaIDs, 0, replicaIDs.length-1));
            }
            
        } 
    }
    private String[] findReplicas(){
        try{
            Registry registry = LocateRegistry.getRegistry();
            String[] services = registry.list();
            List<String> replicaIDs = new ArrayList<>();

            //
            for(int i = 0; i < services.length; i++){
                //System.out.println(services[i]);
                String[] split = services[i].split("_");
                if(split[0].equals("Replica")){
                    replicaIDs.add(split[1]);
                }
            }
            String[] idList = new String[replicaIDs.size()];
            for(int i = 0; i < idList.length; i++){
                idList[i] = replicaIDs.get(i);
            }
            //test
            /*
            System.out.println("ReplicaIDs num = " + replicaIDs.size());
            for(int i = 0; i < replicaIDs.size(); i++){
                System.out.print(replicaIDs.get(i));
            }
            */

            //return (String[]) replicaIDs.toArray();
            return idList;
        }catch(Exception e){
            return null;
        }  
    }

}