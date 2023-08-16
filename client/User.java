public class User {
    String email;
    int userID;
    NewUserInfo userInfo = new NewUserInfo();

    public User(int userID, String email){
        this.userInfo.userID = userID;
        //this.userID = userID;
        this.email = email;
    }

    public int getUserID(){
        return userInfo.userID;
    }
    public NewUserInfo getNewUserinfo(){
        return userInfo;
    }
    public String getEmail(){
        return email;
    }
}
