public class Revenue {
    int Revenue_ID;
    String Amenity_Name;
    Double percentage;

    public Revenue(int Revenue_ID, String Amenity_Name, Double percentage){
        this.Revenue_ID = Revenue_ID;
        this.Amenity_Name = Amenity_Name;
        this.percentage = percentage;
    }

    public double percentage(){
        return percentage;
    }




}
