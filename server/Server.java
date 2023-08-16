import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
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
import java.security.Key;
import java.security.Signature;
import java.io.FileInputStream;

public class Server implements Auction{
    List<ActiveAuction> auctions = new ArrayList<>();
    List<User> users = new ArrayList<>();
    int ReplicaID;

    public Server() {
        super();
        
        
    }
  
    public int getPrimaryReplicaID(){
        try{
            return 0;
        }catch(Exception e){
            throw e;
        }
    }

    public AuctionItem getSpec(int itemID){
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
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");

            GenerateServerKey();

            
            
        }catch(Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
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
            return null;
        }
        
    }

    public int newAuction(int userID, AuctionSaleItem item){
        try{
            ActiveAuction auction = new ActiveAuction(auctions.size()+1, item, userID);
            auctions.add(auction);
            
            return auction.getItemID();
        }catch(Exception e){
            throw e;
        }
        
     }

    public AuctionItem[] listItems(){
        try{
            List<AuctionItem> auctionItems = new ArrayList<>();

            for(int i = 0; i < auctions.size(); i++){
                auctionItems.add(auctions.get(i).getAuctionItem());
            }

            return (AuctionItem[]) auctionItems.toArray();
        }catch(Exception e){
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
    };

    public boolean authenticate(int userID, byte signature[]){
        try{
            return false;
        }catch (Exception e){
            throw e;
        }
    };
    /*
    private void generateKeys(){
        //https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
    }
    */
}