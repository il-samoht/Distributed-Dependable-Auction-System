public class ActiveAuction {
    AuctionItem auctionItem = new AuctionItem();
    int highestBidder = -1;
    AuctionSaleItem auctionSaleItems = new AuctionSaleItem();
    int auctionHolder = -1;

    public ActiveAuction(int auctionID, AuctionSaleItem saleItem, int auctionHolder){
        this.auctionSaleItems = saleItem;
        this.auctionHolder = auctionHolder;

        auctionItem.itemID = auctionID;
        auctionItem.name = saleItem.name;
        auctionItem.description = saleItem.description;
        auctionItem.highestBid = -1;
        
    }

    public void setAuctionItem(AuctionItem item){
        this.auctionItem = item;
    }
    public void setHighestBidder(int userID){
        this.highestBidder = userID;
    }
    public void setAuctionSaleItem(AuctionSaleItem item){
        this.auctionSaleItems = item;
    }
    public void setAuctionHolder(int userID){
        this.auctionHolder = userID;
    }
    public void updateHighestBid(int bid){
        this.auctionItem.highestBid = bid;
    }
    public void updateHighestBidder(int userID){
        highestBidder = userID;
    }

    public AuctionItem getAuctionItem(){
        return auctionItem;
    }
    public int getHighestBidder(){
        return highestBidder;
    }
    public AuctionSaleItem getAuctionSaleItem(){
        return auctionSaleItems;
    }
    public int getAuctionHolder(){
        return auctionHolder;
    }
    public int getHighestBid(){
        return auctionItem.highestBid;
    }
    public int getItemID(){
        return auctionItem.itemID;
    }
    public int getReservePrice(){
        return auctionSaleItems.reservePrice;
    }
}
