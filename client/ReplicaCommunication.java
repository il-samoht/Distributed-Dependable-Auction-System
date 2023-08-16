import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicaCommunication extends Auction{
    public void shareAuctions(ActiveAuction[] auctions) throws RemoteException;
    public void shareUsers(User[] users) throws RemoteException;
}
