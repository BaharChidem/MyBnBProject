import java.time.LocalDate;

public class Reservation {
    private int RID;
    private int LID;
    private int UID;
    private double price;
    private String availability;
    private LocalDate start_date;
    private LocalDate end_date;
    private int rating;
    private String comment;

    public Reservation(int RID, int LID, int UID, double price, String availability, LocalDate start_date, LocalDate end_date, int rating, String comment){
        this.RID = RID;
        this.LID = LID;
        this.UID = UID;
        this.price = price;
        this.availability = availability;
        this.start_date = start_date;
        this.end_date = end_date;
        this.rating = rating;
        this.comment = comment;
    }

    public Reservation (int RID,int LID,double price,LocalDate start_date,LocalDate end_date){
        this.RID = RID;
        this.LID = LID;
        this.UID = UID;
        this.price = price;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public LocalDate start(){return start_date;}
    public LocalDate end(){return end_date;}
    public  int LID(){return LID;}
    public  int UID(){return UID;}
    public  int RID(){return RID;}
    public double price(){return price;};
    public String availability(){return availability;}
    public int rating(){return rating;}
    public String comment(){return comment;}




}
