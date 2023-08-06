

import java.sql.SQLException;

import static java.lang.System.exit;

public class Guest extends User {
    private static String payment_info;

    public Guest(int UID, String SIN, String name, String DoB, String occupation, int AID, String email, String password, String payment_info,String account) {
        super(UID, SIN, name, DoB, occupation, AID, email, password,account);
        this.payment_info = payment_info;
    }
    public Guest(User user, String payment_info){
        super(user);
        this.payment_info = payment_info;
    }

    public static void Signup (String SIN, String name,String DoB, String occupation,String email, String password, String city, String Country, String postalcode,String street,String payment_info, Database data)
            throws IllegalArgumentException, SQLException
    {
        if(data.legal_user(DoB)==false)
        {
            System.out.println("User must be 18 years old");
            //return false;
        }
        User.verify_account(SIN, name, DoB, occupation, email,  password,  city,  Country, postalcode,street);
        if (payment_info.isEmpty()){
              throw new IllegalArgumentException();
        }
        int AID= data.add_address(city.trim(),Country.trim(),postalcode.trim(),street.trim());
        int UID = data.add_user(SIN.toLowerCase().trim(), name.trim(), DoB.toLowerCase().trim(), occupation.trim(),AID,email.toLowerCase().trim(), password.toLowerCase().trim());
        data.add_guest(UID, payment_info);

    }

    public static User login(String email , String password , Database data) throws SQLException {
        User user = data.get_user(email);
        if (user == null) {
            System.out.println("No Such User");
            System.exit(0);
        }
        Guest guest = data.get_guest(user);
        if (guest == null) {
            System.out.println("No such Guest");
            System.exit(0);
        }
        if (!guest.Password().equals(password)) {
            System.out.println("Invalid Password");
            System.exit(0);
        }
        if (guest.Account().equals("[INACTIVE]")) {
            System.out.println("Not an Active Account");
            System.exit(0);
        }
        return guest;


    }

    private static void exit() {
    }

    public boolean check_guest(String SIN, String name,String DoB, String occupation,String email, String password, String city, String Country, String postalcode,String street, Database data) throws SQLException {
        User.verify_account(SIN, name, DoB, occupation, email, password, city, Country, postalcode, street);

        User user = data.get_user(email);
        Guest guest = data.get_guest(user);

        if(user == null){
            return false;
        }
        if(guest == null){
            return false;
        }
        return true;

    }
    
}
