import java.time.LocalDate;
import java.util.Date;

public class Calendar {
    private int CID;
    private int LID;
    private String availability;
    private double price;
    private LocalDate date;

    public Calendar(int CID, int LID, String availability, double price, LocalDate date){
        this.CID = CID;
        this.LID = LID;
        this.availability = availability;
        this.price = price;
        this.date = date;
    }

    public int CID(){return CID;}
    public int LID(){return LID;}
    public String availability(){return availability;}
    public double Price (){return price;}

    public LocalDate date(){return date;}


}
