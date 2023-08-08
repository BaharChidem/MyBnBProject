import java.sql.SQLException;

public class Host extends User {

    public Host(int UID, String SIN, String name, String DoB, String occupation, int AID, String email, String password,String account) {
        super(UID, SIN, name, DoB, occupation, AID, email, password,account);
    }
    public Host(User user){
        super(user);
    }
    public static void Signup (String SIN, String name,String DoB, String occupation,String email, String password, String city, String Country, String postalcode,String street, Database data)
            throws IllegalArgumentException, SQLException
    {
        if(data.legal_user(DoB)==false)
        {
            System.out.println("User must be 18 years old");
            //return false;
        }
        User.verify_account(SIN, name, DoB, occupation, email,  password,  city,  Country, postalcode,street);
        int AID= data.add_address(city.toLowerCase().trim(),Country.toLowerCase().trim(),postalcode.toLowerCase().trim(),street.toLowerCase().trim());
        int UID = data.add_user(SIN.toLowerCase().trim(), name.toLowerCase().trim(), DoB.toLowerCase().trim(), occupation.toLowerCase().trim(),AID,email.toLowerCase().trim(), password.toLowerCase().trim());
        data.add_host(UID);

    }
    public static User login(String email , String password , Database data) throws SQLException {
        User user = data.get_user(email);
        if (user == null) {
            System.out.println("No Such User");
            System.exit(0);
        }
        Host host = data.get_host(user);
        if (host == null) {
            System.out.println("No such Host");
            System.exit(0);
        }
        if (!host.Password().equals(password)) {
            System.out.println("Invalid Password");
            System.exit(0);
        }
        if (host.Account().equals("[INACTIVE]")) {
            System.out.println("Not a Valid Account");
            System.exit(0);
        }

        return host;
    }
}
