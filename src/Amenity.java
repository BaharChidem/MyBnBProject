public class Amenity {
    int Amenity_ID;
    String Amenity_Name;
    int Category_ID;

    public Amenity(int Amenity_ID, String Amenity_Name, int Category_ID) {
        this.Amenity_ID = Amenity_ID;
        this.Amenity_Name = Amenity_Name;
        this.Category_ID = Category_ID;
    }

    public int Amenity_ID() {
        return Amenity_ID;
    }

    public String Amenity_Name(){
        return Amenity_Name;
    }

}
