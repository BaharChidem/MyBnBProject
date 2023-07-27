
public class Address {
    private int AID;
    private String city;
    private String country;
    private String postal_code;
    private String street;

    @Override
    public String toString() {
        return "Address{" +
                "AID=" + AID +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", postal_code='" + postal_code + '\'' +
                ", street='" + street + '\'' +
                '}';
    }


    public Address(int AID, String city, String country, String postal_code, String street){
        this.AID = AID;
        this.city = city;
        this.country = country;
        this.postal_code = postal_code;
        this.street=street;
    }

    public String city(){return city;}
    public String Country(){return country;}
    public String postal_code(){return postal_code;}
    public String Street(){return street;}

    
}
