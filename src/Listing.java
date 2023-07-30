import java.sql.SQLException;

public class Listing {
    private int LID;
    private String type;
    private double Longitude;
    private double Latitude;
    private String Status;
    private double distance;
    private Address address;

    public Listing(int LID,String type, double Longitude, double Latitude, Address address,String status){
        this.LID=LID;
        this.type=type;
        this.Longitude=Longitude;
        this.Latitude=Latitude;
        this.Status=status;
        this.address = address;
    }
    public Listing(int LID,String type, double Longitude, double Latitude, Address address,String status,double distance){
        this.LID=LID;
        this.type=type;
        this.Longitude=Longitude;
        this.Latitude=Latitude;
        this.Status=status;
        this.address = address;
        this.distance = distance;
    }

    public static void verify_Listing(String type, double Longitude, double Latitude,  String city, String Country, String postalcode, String street)
            throws IllegalArgumentException
    {
        if (type.isEmpty()||city.isEmpty()||Country.isEmpty()||postalcode.isEmpty()||street.isEmpty())
        {
            System.out.println("This field is empty");
        }

    }

    public static int post_listings(Database data,int host_id,String type, double Longitude, double Latitude, String street, String City, String Postalcode, String Country) throws SQLException {
        int AID = data.add_address(City.toLowerCase().trim(),Country.toLowerCase().trim(),Postalcode.toLowerCase().trim(),street.toLowerCase().trim());
        Listing.verify_Listing(type,Longitude,Latitude,City,Country,Postalcode,street);
        int LID= data.add_listing(host_id,AID,type,Longitude,Latitude);
        return LID;

    }

    @Override
    public String toString() {
        return "Listing{" +
                //"LID=" + LID +
                ", type='" + type + '\'' +
                ", longitude=" + Longitude +
                ", latitude=" + Latitude +
                ", status='" + Status + '\'' +
                ", Address '" +address.toString()+
                '}';
    }

    public int LID(){
        return LID;
    }
    public Address address(){
        return address;
    }

    public String status(){ return Status;}
    public double Longitude (){return Longitude;}
    public double Latitude (){return Latitude;}
    public String type (){ return type;}
    public double distance(){return distance;}











}
