
public class User {
    private int UID;
    private String SIN;
    private String name;
    private String DoB;
    private String occupation;
    private int AID;

    private String email;
    private String password;
    private String account;
    private int rid;

    public User(int UID, String SIN, String name, String DoB, String occupation, int AID, String email, String password, String account){
        this.UID = UID;
        this.SIN = SIN; 
        this.name = name;
        this.DoB = DoB;
        this.occupation = occupation;
        this.AID = AID;
        this.email = email;
        this.password = password;
        this.account=account;
    }
    public User(int UID, String SIN, String name, String DoB, String occupation, int AID, String email, String password, String account , int rid){
        this.UID = UID;
        this.SIN = SIN;
        this.name = name;
        this.DoB = DoB;
        this.occupation = occupation;
        this.AID = AID;
        this.email = email;
        this.password = password;
        this.account=account;
        this.rid = rid;
    }
    public User (User user){
        this.UID = user.UID;
        this.SIN = user.SIN;
        this.name = user.name;
        this.DoB = user.DoB;
        this.occupation = user.occupation;
        this.AID = user.AID;
        this.email = user.email;
        this.password = user.password;
        this.account=user.account;
    }

    public static void verify_account( String SIN, String name,String DoB, String occupation,String email, String password, String city, String Country, String postalcode,String street)
    throws IllegalArgumentException
    {
        if (name.isEmpty() || SIN.isEmpty()|| DoB.isEmpty()|| occupation.isEmpty()||email.isEmpty()||email.isEmpty()||password.isEmpty()||city.isEmpty()||Country.isEmpty()|| postalcode.isEmpty()||street.isEmpty())
        {
            System.out.println("This field is empty");
        }
        if(password.length()<6){
            System.out.println("Password must have a length more than 6");
        }

    }

    public int uid(){
        return UID;
    }
    public String Password(){
        return password;
    }
    public String Account(){
        return account;
    }

    public String email(){
        return email;
    }
    public String name(){
        return name;
    }
    public String occupation(){
        return occupation;
    }

    public int rid(){
        return rid;
    }
    
}
