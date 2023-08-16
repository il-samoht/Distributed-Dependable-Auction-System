import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.io.File;
import java.io.FileWriter;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

public class Client{
    Auction server;
    List<NewUserInfo> users = new ArrayList<>();
    List<Integer> items = new ArrayList<>();

    public Client(Auction server){
        super();
        this.server = server;
        InteractableInterface();
    }
    public static void main(String[] args) {
        try {
            String name = "FrontEnd";
            Registry registry = LocateRegistry.getRegistry();
            Auction server = (Auction) registry.lookup(name);
            

            Client c = new Client(server);     
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }


    private void InteractableInterface(){
        try{
            while(true){
                Scanner scan = new Scanner(System.in);
                printMenu();
                int menuChoice = scan.nextInt();
                if(menuChoice == 1){
                    optionlistItems();
                }else if(menuChoice == 2){
                    optionnewUser();
                }else if(menuChoice == 3){
                    optionnewAuction();
                }else if(menuChoice == 4){
                    optionbid();
                }else if(menuChoice == 5){
                    optioncloseAuction();
                }else{
                    InteractableInterface();
                }
            }
        }catch(Exception e){
            System.out.println("error occured please restart action");
            e.printStackTrace();
            InteractableInterface();
        }
        
    }

    private void printMenu(){
        System.out.println("");
        System.out.println("Choose an option: ");
        System.out.println("1. List auctions");
        System.out.println("2. Create new user");
        System.out.println("3. Create new auction");
        System.out.println("4. Bid on auction");
        System.out.println("5. Close auction");
    }

    private void optionlistItems(){
        try{
            AuctionItem[] auctionItems = server.listItems();
            if(auctionItems.length != 0){
                System.out.println("");
                System.out.println("Active Auctions: ");
                System.out.println("***");
                for(int i = 0; i < auctionItems.length; i++){
                    System.out.println("ItemID: " + auctionItems[i].itemID);
                    System.out.println("Name: " + auctionItems[i].name);
                    System.out.println("Descriptions: " + auctionItems[i].description);
                    System.out.println("Highest Bid: " + auctionItems[i].highestBid);
                    System.out.println("***");
                }
            }else{
                System.out.println("No active auctions");
            }
            
        }catch(Exception e){
            System.out.println("error");
            InteractableInterface();
        }
    }

    private void optionnewUser(){
        try{
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter email:");
            NewUserInfo user = server.newUser(scan.nextLine());
            users.add(user);
            System.out.println("New user id: " + user.userID);
        }catch(Exception e){
            System.out.println("error occured please restart action");
            e.printStackTrace();
            InteractableInterface();
        }
    }

    private void optionnewAuction(){
        try{
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter user id: ");
            int userID = scan.nextInt();
            scan.nextLine();
            AuctionSaleItem asi = new AuctionSaleItem();
            System.out.println("Enter item name: ");
            asi.name = scan.nextLine();
            System.out.println("Enter item description: ");
            asi.description = scan.nextLine();
            System.out.println("Enter item reserve price: ");
            asi.reservePrice = scan.nextInt();
            
            int itemID = server.newAuction(userID, asi);
            System.out.println("New auction id: " + itemID);
            items.add(itemID);
        }catch(Exception e){
            System.out.println("error occured please restart action");
            InteractableInterface();
        }
    }
    private void optionbid(){
        try{
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter user id: ");
            int userID = scan.nextInt();
            System.out.println("Enter item id: ");
            int itemID = scan.nextInt();
            System.out.println("Enter bid price: ");
            int price = scan.nextInt();

            if(server.bid(userID, itemID, price)){
                System.out.println("Bid successful");
            }else{
                System.out.println("Bid failed");
            }
        }catch(Exception e){
            System.out.println("error occured please restart action");
            InteractableInterface();
        }
    }
    private void optioncloseAuction(){
        try{
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter user id: ");
            int userID = scan.nextInt();
            System.out.println("Enter item id: ");
            int itemID = scan.nextInt();

            AuctionCloseInfo aci = server.closeAuction(userID, itemID);

            if(aci != null){
                System.out.println("Winner email: " + aci.winningEmail);
                System.out.println("Winning bet: " + aci.winningPrice);
            }
        }catch(Exception e){
            System.out.println("error occured please restart action");
            InteractableInterface();
        }
    }

    public static AuctionItem decry(SealedObject obj){
        try{
            File file = new File("../keys/testKey.aes");
            Scanner reader = new Scanner(file);
            String keyString = reader.nextLine();
            //System.out.println(keyString);
            byte[] decodedKey = Base64.getDecoder().decode(keyString); 
            SecretKey aesKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);

            
            AuctionItem item = (AuctionItem)obj.getObject(cipher);

            return item;
        }catch(Exception e){
            return null;
        }
        
    }
}