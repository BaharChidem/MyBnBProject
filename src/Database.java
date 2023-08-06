
import java.lang.reflect.Array;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class Database {
    public final Connection connection;

    public Database(String project, String user, String password) throws SQLException {
        String URL = "jdbc:mysql://127.0.0.1/Mybnb";
        connection = DriverManager.getConnection(URL, user, password);
    }

    // ------------------------------------------ USER , HOST , GUEST Related --------------------------------------------
    public boolean legal_user(String DoB) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT TIMESTAMPDIFF(YEAR, ?, CURDATE()) < 18 AS ILLEGAL");
        stmt.setString(1, DoB);
        ResultSet rs = stmt.executeQuery();
        if (rs.next() && rs.getInt("ILLEGAL") == 1) {
            return false;
        }
        return true;

    }

    public int add_user(String SIN, String name, String DoB, String occupation, int AID, String email, String password)
            throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Users(Email, Password, Name, SIN,DoB, AID, Occupation,Account)  " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, email);
        stmt.setString(2, password);
        stmt.setString(3, name);
        stmt.setString(4, SIN);
        stmt.setString(5, DoB);
        stmt.setInt(6, AID);
        stmt.setString(7, occupation);
        stmt.setString(8, "[ACTIVE]");
        int updated = stmt.executeUpdate();

        if (updated == 1) {
            try (ResultSet uid = stmt.getGeneratedKeys()) {
                if (uid.next()) {
                    return uid.getInt(1);
                }
            }
        }
        return -1;
    }

    public int add_address(String City, String Country, String Postal_code, String Street_det) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Address (City,Country,Postal_code,Street)" + "VALUES (?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, City);
        stmt.setString(2, Country);
        stmt.setString(3, Postal_code);
        stmt.setString(4, Street_det);
        int updated = stmt.executeUpdate();
        if (updated == 1) {
            try (ResultSet aid = stmt.getGeneratedKeys()) {
                if (aid.next()) {
                    return aid.getInt(1);
                }
            }
        }
        return -1;

    }

    public void add_guest(int UID, String payment_info) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Guests(UID, payment_info) " + "VALUES (?,?)");
        stmt.setInt(1, UID);
        stmt.setString(2, payment_info);
        stmt.executeUpdate();
    }

    public void add_host(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Hosts(UID) " + "VALUES (?)");
        stmt.setInt(1, UID);
        stmt.executeUpdate();

    }

    public User get_user(String email) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE Email=? ");
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        User user = null;
        if (rs.next()) {
            int UID = rs.getInt("UID");
            String SIN = rs.getString("SIN");
            String name = rs.getString("Name");
            String password = rs.getString("Password");
            String DoB = rs.getString("DoB");
            int AID = rs.getInt("AID");
            String occupation = rs.getString("Occupation");
            String account = rs.getString("Account");
            user = new User(UID, SIN, name, DoB, occupation, AID, email, password, account);
        }
        return user;

    }

    public int get_uid(String email) throws SQLException {
        User user = get_user(email);
        return user.uid();
    }

    public Guest get_guest(User user) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Guests WHERE UID=? ");
        stmt.setInt(1, user.uid());
        ResultSet rs = stmt.executeQuery();
        Guest guest = null;
        if (rs.next()) {
            String payment_info = rs.getString("payment_info");
            guest = new Guest(user, payment_info);

        }
        return guest;

    }

    public Host get_host(User user) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Hosts WHERE UID=? ");
        stmt.setInt(1, user.uid());
        ResultSet rs = stmt.executeQuery();
        Host host = null;
        if (rs.next()) {
            host = new Host(user);

        }
        return host;

    }

    public void activate_account(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE Users SET account = ? WHERE UID =?");
        stmt.setString(1, "[ACTIVE]");
        stmt.setInt(2, UID);
        stmt.executeUpdate();
        //add_guest_back(UID);
    }

    public void delete_account(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE Users SET account = [INACTIVE] WHERE UID =?");
        stmt.setInt(1, UID);
        stmt.executeQuery();
    }

    public void deactivate_guest(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Reservation WHERE Availability = '[RESERVED]' AND UID =?");
        stmt.setInt(1, UID);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            int RID = rs.getInt("RID");
            int LID = rs.getInt("LID");
            PreparedStatement stmt2 = connection.prepareStatement("UPDATE CALENDAR SET Availability='[OPEN]'WHERE LID=?");
            stmt2.setInt(1,LID);
            stmt2.executeUpdate();
            cancel_reservation_status(RID, "[GUEST]");
        }
        PreparedStatement updateStmt = connection.prepareStatement("UPDATE Users SET account = ? WHERE UID = ?");
        updateStmt.setString(1, "[INACTIVE]");
        updateStmt.setInt(2, UID);
        updateStmt.executeUpdate();
    }

    public void deactivate_host(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LISTINGS " + "WHERE Status ='[ACTIVE]' AND UID=?");

        stmt.setInt(1, UID);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int LID = rs.getInt("LID");
            delete_listing(LID);
        }
        PreparedStatement acc_inactivation = connection.prepareStatement("UPDATE Users SET account =? WHERE UID =?");
        acc_inactivation.setString(1, "[INACTIVE]");
        acc_inactivation.setInt(2, UID);
        acc_inactivation.executeUpdate();
    }

// ------------------------------------------------- LISTINGS RELATED --------------------------------------------------

    public int add_listing(int host_id, int AID, String type, double Longitude, double Latitude) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Listings (UID,Type,Longitude,Latitude,AID,Status)" + "VALUES (?, ?, ?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, host_id);
        stmt.setString(2, type);
        stmt.setDouble(3, Longitude);
        stmt.setDouble(4, Latitude);
        stmt.setInt(5, AID);
        stmt.setString(6, "[ACTIVE]");
        int updated = stmt.executeUpdate();
        if (updated == 1) {
            try (ResultSet LID = stmt.getGeneratedKeys()) {
                if (LID.next()) {
                    return LID.getInt(1);
                }
            }
        }
        return -1;
    }

    public ArrayList<Listing> get_host_Listing(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LISTINGS NATURAL JOIN ADDRESS WHERE UID =? AND Status='[ACTIVE]'");
        stmt.setInt(1, UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Listing> HostListing = new ArrayList<>();
        while (rs.next()) {
            int LID = rs.getInt("LID");
            double Longitude = rs.getDouble("Longitude");
            double Latitude = rs.getDouble("Latitude");
            int AID = rs.getInt("AID");
            String street = rs.getString("Street");
            String city = rs.getString("City");
            String Country = rs.getString("Country");
            String postalcode = rs.getString("Postal_code");
            String status = rs.getString("Status");
            String type = rs.getString("Type");
            Address listing_address = new Address(AID, city, Country, postalcode, street);
            HostListing.add(new Listing(LID, type, Latitude, Longitude, listing_address, status));

        }
        return HostListing;

    }

    public void delete_listing(int LID) throws SQLException {
        PreparedStatement stmt1 = connection.prepareStatement("SELECT * FROM Reservation WHERE Availability ='[RESERVED]' AND LID=?");
        stmt1.setInt(1,LID);
        ResultSet rs = stmt1.executeQuery();
        while(rs.next()){
            int RID = rs.getInt("RID");
            PreparedStatement stmt2 = connection.prepareStatement("UPDATE CALENDAR SET Availability='[BLOCKED]'WHERE LID=?");
            stmt2.setInt(1,LID);
            stmt2.executeUpdate();
            cancel_reservation_status(RID, "[HOST]");
        }
        PreparedStatement stmt = connection.prepareStatement("UPDATE LISTINGS SET Status='[INACTIVE]' WHERE LID=?");
        stmt.setInt(1, LID);
        stmt.executeUpdate();
    }

    public int add_calendar(int lid, String start, String end, double price) throws SQLException {
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
        int count = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (check_status(lid, currentDate) == false) {
                if (check_day_status(lid, currentDate, "[BLOCKED]")) {
                    update_day_status(lid, currentDate, "[OPEN]");
                    update_day_price(lid, currentDate, price);
                } else {
                    PreparedStatement stmt = connection.prepareStatement("INSERT INTO CALENDAR (LID,Date,Price, Availability)" + "VALUES (?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, lid);
                    stmt.setDate(2, Date.valueOf(currentDate));
                    stmt.setDouble(3, price);
                    stmt.setString(4, "[OPEN]");
                    stmt.executeUpdate();
                }
                count++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return count;
    }

    public boolean check_status(int lid, LocalDate date) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM CALENDAR WHERE LID =? AND DATE =? AND Availability !=?");
        stmt.setInt(1, lid);
        stmt.setDate(2, Date.valueOf(date));
        stmt.setString(3, "[BLOCKED]");
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    // checking the status for the range of the date
    public boolean check_status_calendar(int lid, LocalDate start, LocalDate end, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM CALENDAR WHERE LID =? AND DATE BETWEEN ? AND ? AND Availability =?");
        stmt.setInt(1, lid);
        stmt.setDate(2, Date.valueOf(start));
        stmt.setDate(3, Date.valueOf(end));
        stmt.setString(4, status);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public ArrayList<Calendar> Availabilities(int lid,LocalDate start, LocalDate end) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM CALENDAR WHERE LID =? AND Date BETWEEN ? AND ? AND Availability =?");
        stmt.setInt(1, lid);
        stmt.setDate(2, Date.valueOf(start));
        stmt.setDate(3, Date.valueOf(end));
        stmt.setString(4,"[OPEN]");
        ResultSet rs = stmt.executeQuery();
        ArrayList<Calendar>available=new ArrayList<>();
        while(rs.next()){
            int CID=rs.getInt("CID");
            String avail = rs.getString("Availability");
            Double price = rs.getDouble("Price");
            Date date = rs.getDate("Date");
            available.add(new Calendar(CID,lid,avail,price,date.toLocalDate()));
        }
        return available;
    }

    public boolean check_availability_reservation(int LID, LocalDate start, LocalDate end) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT LID FROM Calendar C WHERE Availability ='[OPEN]' AND LID = ? AND Date BETWEEN ? AND ? GROUP BY LID HAVING COUNT(*)=DATEDIFF(?, ?)+1");
        stmt.setInt(1,  LID);
        stmt.setDate(2, Date.valueOf(start));
        stmt.setDate(3, Date.valueOf(end));
        stmt.setDate(4, Date.valueOf(end));
        stmt.setDate(5, Date.valueOf(start));
        return stmt.executeQuery().next();
    }

    public boolean check_day_status(int lid, LocalDate date, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM CALENDAR WHERE LID =? AND Date =? AND Availability = ?");
        stmt.setInt(1, lid);
        stmt.setDate(2, Date.valueOf(date));
        stmt.setString(3, status);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public void update_day_status(int lid, LocalDate date, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE CALENDAR SET Availability=? WHERE LID=? AND Date=?");
        stmt.setString(1, status);
        stmt.setInt(2, lid);
        stmt.setDate(3, Date.valueOf(date));
        stmt.executeUpdate();
    }

    public void update_day_price(int lid, LocalDate date, Double price) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE CALENDAR SET PRICE=? WHERE LID=? AND Date=?");
        stmt.setDouble(1, price);
        stmt.setInt(2, lid);
        stmt.setDate(3, Date.valueOf(date));
        stmt.executeUpdate();
    }

    public int remove_calendar(int lid, LocalDate start, LocalDate end) throws SQLException {
        int count = 0;
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            if (check_day_status(lid, currentDate, "[OPEN]")) {
                PreparedStatement stmt = connection.prepareStatement("UPDATE CALENDAR SET Availability =? WHERE LID=? AND DATE=?");
                stmt.setString(1, "[BLOCKED]");
                stmt.setInt(2, lid);
                stmt.setDate(3, Date.valueOf(currentDate));

                stmt.executeUpdate();
                count++;

            }

            currentDate = currentDate.plusDays(1);
        }
        return count;
    }

    public int update_price(int lid, LocalDate start, LocalDate end, double price) throws SQLException {
        int count = 0;
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            if (check_day_status(lid, currentDate, "[OPEN]")) {
                update_day_price(lid, currentDate, price);
                count++;
            }

            currentDate = currentDate.plusDays(1);
        }
        return count;
    }

    public boolean check_reservation(int LID, LocalDate start, LocalDate end) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Reservation WHERE LID=? AND Availability!='[CANCELLED]' AND (" +
                                                                    "(StartDate >= ? AND StartDate <= ?) OR " +
                                                                    "(EndDate >= ? AND EndDate <= ?) OR " +
                                                                    "(StartDate <= ? AND EndDate >= ?) OR " +
                                                                    "(StartDate <= ? AND EndDate >= ?))");
        stmt.setInt(1, LID);
        stmt.setDate(2, Date.valueOf(start));
        stmt.setDate(3, Date.valueOf(end));
        stmt.setDate(4, Date.valueOf(start));
        stmt.setDate(5, Date.valueOf(end));
        stmt.setDate(6, Date.valueOf(start));
        stmt.setDate(7, Date.valueOf(end));
        stmt.setDate(8, Date.valueOf(start));
        stmt.setDate(9, Date.valueOf(end));
        ResultSet rs = stmt.executeQuery();
        return rs.next();

    }


    public ArrayList<Listing> search_postalcode(String postalcode) throws SQLException {
            String code = postalcode.toUpperCase().trim().substring(0, 3);
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LISTINGS NATURAL JOIN ADDRESS WHERE Status='[ACTIVE]'AND SUBSTRING(Postal_code,1,3) =?");
            stmt.setString(1, code);
            ArrayList<Listing> Postal_code = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();
            Postal_code=Make_table(rs);
            return Postal_code;

    }

    public ArrayList<Listing> search_address(String street, String city , String country) throws SQLException {
        String street1 = street.toLowerCase().trim();
        String city1 = city.toLowerCase().trim();
        String Country = country.toLowerCase().trim();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LISTINGS NATURAL JOIN ADDRESS WHERE Status='[ACTIVE]'AND Street=?AND City=? AND Country=?");
        stmt.setString(1,street1);
        stmt.setString(2,city1);
        stmt.setString(3,Country);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Listing> Address = new ArrayList<>();
        Address =Make_table(rs);
        return Address;
    }

    public ArrayList<Listing> search_coords(double latitude, double longitude, double distance) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement( "SELECT L.*, A.*, ST_Distance_Sphere(point(Longitude,Latitude), point("+longitude+", "+latitude+")) as Distance FROM Listings L JOIN Address A ON L.AID=A.AID HAVING Distance <="+distance+" ORDER BY Distance ASC ");
        ResultSet rs = stmt.executeQuery();
        ArrayList<Listing>coords=new ArrayList<>();

        while (rs.next()){
            int LID = rs.getInt("LID");
            double Longitude = rs.getDouble("Longitude");
            double Latitude = rs.getDouble("Latitude");
            int AID = rs.getInt("AID");
            String street = rs.getString("Street");
            String city = rs.getString("City");
            String Country = rs.getString("Country");
            String postal_code = rs.getString("Postal_code");
            String status = rs.getString("Status");
            String type = rs.getString("Type");
            double dist = rs.getDouble("Distance");
            double km = dist/1000;
            Address listing_address = new Address(AID, city, Country, postal_code, street);
            coords.add(new Listing(LID, type, Latitude, Longitude, listing_address, status,km));
        }
        return coords;

    }



    public ArrayList<Listing> Make_table(ResultSet rs) throws SQLException {
        ArrayList<Listing> new_list= new ArrayList<>();
        while (rs.next()) {
            int LID = rs.getInt("LID");
            double Longitude = rs.getDouble("Longitude");
            double Latitude = rs.getDouble("Latitude");
            int AID = rs.getInt("AID");
            String street = rs.getString("Street");
            String city = rs.getString("City");
            String Country = rs.getString("Country");
            String postal_code = rs.getString("Postal_code");
            String status = rs.getString("Status");
            String type = rs.getString("Type");
            Address listing_address = new Address(AID, city, Country, postal_code, street);
            new_list.add(new Listing(LID, type, Latitude, Longitude, listing_address, status));


        }
        return new_list;


    }

    //------------------------------------------------------------ RESERVATIONS -----------------------------------------------------------

    public Reservation Make_Reservation(int UID, int LID, LocalDate start, LocalDate end, double total_price) {
        Reservation reservation_made = null;

        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Reservation(UID,LID,StartDate,EndDate,Price,Availability) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, UID);
            stmt.setInt(2, LID);
            stmt.setDate(3, Date.valueOf(start));
            stmt.setDate(4, Date.valueOf(end));
            stmt.setDouble(5, total_price);
            stmt.setString(6, "[RESERVED]");
            int updated = stmt.executeUpdate();
            int RID = -1;
            if (updated == 1) {
                try (ResultSet RID_key = stmt.getGeneratedKeys()) {
                    if (RID_key.next()) {
                        RID = RID_key.getInt(1);
                    }
                }
            }
//            PreparedStatement stmtUpdate = connection.prepareStatement("UPDATE Reservation SET availability = '[RESERVED]' WHERE RID = ?");
//            stmtUpdate.setInt(1, RID);
//            stmtUpdate.executeUpdate();


            PreparedStatement stmt2 = connection.prepareStatement("SELECT * FROM Reservation WHERE LID=? AND StartDate=? AND EndDate=?");
            stmt2.setInt(1, LID);
            stmt2.setDate(2, Date.valueOf(start));
            stmt2.setDate(3, Date.valueOf(end));

            ResultSet rs = stmt2.executeQuery();
            if (rs.next()) {
                double tot_price = rs.getDouble("Price");
                //String availability = rs.getString("availability");
                String comm = rs.getString("Comment");
                int rating = rs.getInt("Rating");
                reservation_made = new Reservation(RID, LID, UID, tot_price, "[RESERVED]", start, end, rating, comm);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservation_made;
    }

    public double Total_price(LocalDate start, LocalDate end, int LID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT SUM(Price) as total_price FROM Calendar WHERE LID = ? AND Date BETWEEN ? AND ?");
        stmt.setInt(1, LID);
        stmt.setDate(2,Date.valueOf(start));
        stmt.setDate(3,Date.valueOf(end));
        ResultSet rs = stmt.executeQuery();
        if (rs.next()==false) {
            return-1;
        }
        return rs.getDouble("total_price");

    }

    public void update_reservation() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE Reservation SET Availability = '[PAST RESERVATION]' WHERE EndDate < CURRENT_DATE AND Availability = '[RESERVED]'");
        stmt.executeUpdate();
    }

    public ArrayList<Reservation> Guest_reservations(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Reservation WHERE UID =? AND Availability= '[RESERVED]'");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> guest_reservation = new ArrayList<>();
        while(rs.next()){
        int rid = rs.getInt("RID");
        int uid = rs.getInt("UID");
        int lid = rs.getInt("LID");
        Date start = rs.getDate("StartDate");
        Date end = rs.getDate("EndDate");
        String s = rs.getString("Availability");
        int ranking = rs.getInt("Rating");
        String comment = rs.getString("Comment");
        double price = rs.getDouble("Price");
        guest_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return guest_reservation;
    }

    public ArrayList<Reservation> Guest_past(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Reservation WHERE UID =? AND Availability= '[PAST RESERVATION]'");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> guest_reservation = new ArrayList<>();
        while(rs.next()){
            int rid = rs.getInt("RID");
            int uid = rs.getInt("UID");
            int lid = rs.getInt("LID");
            Date start = rs.getDate("StarTDate");
            Date end = rs.getDate("EndDate");
            String s = rs.getString("Availability");
            int ranking = rs.getInt("Rating");
            String comment = rs.getString("Comment");
            double price = rs.getDouble("Price");
            guest_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return guest_reservation;
    }
    public ArrayList<Reservation> Guest_canceled(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Reservation WHERE UID =? AND Availability= '[CANCELED]'");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> guest_reservation = new ArrayList<>();
        while(rs.next()){
            int rid = rs.getInt("RID");
            int uid = rs.getInt("UID");
            int lid = rs.getInt("LID");
            Date start = rs.getDate("StartDate");
            Date end = rs.getDate("EndDate");
            String s = rs.getString("Availability");
            int ranking = rs.getInt("Rating");
            String comment = rs.getString("Comment");
            double price = rs.getDouble("Price");
            guest_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return guest_reservation;
    }

    public ArrayList<Reservation> host_reservation(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT R.* FROM Reservation R, Listings L WHERE R.LID=L.LID AND R.Availability = '[RESERVED]' AND L.UID = ?");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> host_reservation = new ArrayList<>();
        while(rs.next()){
            int rid = rs.getInt("RID");
            int uid = rs.getInt("UID");
            int lid = rs.getInt("LID");
            Date start = rs.getDate("StartDate");
            Date end = rs.getDate("EndDate");
            String s = rs.getString("Availability");
            int ranking = rs.getInt("Rating");
            String comment = rs.getString("Comment");
            double price = rs.getDouble("Price");
            host_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return host_reservation;
    }

    public ArrayList<Reservation> host_past(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT R.* FROM Reservation R, Listings L WHERE R.LID=L.LID AND R.Availability = '[PAST RESERVATION]' AND L.UID = ?");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> host_reservation = new ArrayList<>();
        while(rs.next()){
            int rid = rs.getInt("RID");
            int uid = rs.getInt("UID");
            int lid = rs.getInt("LID");
            Date start = rs.getDate("StartDate");
            Date end = rs.getDate("EndDate");
            String s = rs.getString("Availability");
            int ranking = rs.getInt("Rating");
            String comment = rs.getString("Comment");
            double price = rs.getDouble("Price");
            host_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return host_reservation;
    }

    public ArrayList<Reservation> host_canceled(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT R.* FROM Reservation R, Listings L WHERE R.LID=L.LID AND R.Availability = '[CANCELED]' AND L.UID = ?");
        stmt.setInt(1,UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Reservation> host_reservation = new ArrayList<>();
        while(rs.next()){
            int rid = rs.getInt("RID");
            int uid = rs.getInt("UID");
            int lid = rs.getInt("LID");
            Date start = rs.getDate("StartDate");
            Date end = rs.getDate("EndDate");
            String s = rs.getString("Availability");
            int ranking = rs.getInt("Rating");
            String comment = rs.getString("Comment");
            double price = rs.getDouble("Price");
            host_reservation.add(new Reservation(rid, uid, lid, price, s, start.toLocalDate(), end.toLocalDate(), ranking, comment));
        }
        return host_reservation;
    }

    public void cancel_reservation_status(int RID, String user) throws SQLException {
        PreparedStatement stmt= connection.prepareStatement("UPDATE RESERVATION set Availability ='[CANCELED]', CanceledBy=? WHERE RID=?");
        stmt.setString(1,user);
        stmt.setInt(2,RID);
        stmt.executeUpdate();
    }

    //------------------------------------------------------------ RATING & COMMENTS -----------------------------------------------------------
    public void rate_reservation(int RID, String comment, int rating) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE Reservation SET Comment=?, Rating =? WHERE RID=?");
        stmt.setString(1, comment);
        stmt.setInt(2, rating);
        stmt.setInt(3,RID);
        stmt.executeUpdate();
    }

    public ArrayList<User> get_host(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT U.* FROM Reservation R, Listings L, Users U WHERE R.LID = L.LID AND L.UID = U.UID AND R.UID=? AND R.Availability='[PAST RESERVATION]'AND U.Account = '[ACTIVE]'");
        stmt.setInt(1, UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<User> hosts = new ArrayList<>();
        while(rs.next()){
            int UID1 = rs.getInt("UID");
            String SIN = rs.getString("SIN");
            String doB = rs.getString("DoB");
            String name = rs.getString("Name");
            String occupation = rs.getString("Occupation");
            int AID = rs.getInt("AID");
            String email = rs.getString("Email");
            String password = rs.getString("Password");
            String account = rs.getString("Account");
            hosts.add(new User(UID1, SIN, name, doB, occupation, AID, email, password, account));
        }
        return hosts;
    }

    public void add_review(int UID, int UID2, String comment, int rating) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Review(Reviewer, Reviewee, Rating, Comment) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1,UID);
        stmt.setInt(2,UID2);
        stmt.setInt(3, rating);
        stmt.setString(4, comment);
        stmt.executeUpdate();
    }

    public ArrayList<User> get_guest(int UID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT U.* FROM Reservation R, Listings L, Users U WHERE R.LID = L.LID AND R.UID = U.UID AND L.UID=? AND R.Availability='[PAST RESERVATION]'AND U.Account ='[ACTIVE]'");
        stmt.setInt(1, UID);
        ResultSet rs = stmt.executeQuery();
        ArrayList<User> hosts = new ArrayList<>();
        while(rs.next()){
            int UID1 = rs.getInt("UID");
            String SIN = rs.getString("SIN");
            String doB = rs.getString("DoB");
            String name = rs.getString("Name");
            String occupation = rs.getString("Occupation");
            int AID = rs.getInt("AID");
            String email = rs.getString("Email");
            String password = rs.getString("Password");
            String account = rs.getString("Account");
            hosts.add(new User(UID1, SIN, name, doB, occupation, AID, email, password, account));
        }
        return hosts;
    }

    //------------------------------------------------------------ REPORTS -----------------------------------------------------------
    public void report_case1(LocalDate startDate, LocalDate endDate) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT City, COUNT(*) AS TotalReservation FROM Reservation R, Listings L, Address A WHERE L.LID = R.LID AND A.AID=L.AID AND R.StartDate >= ? AND R.EndDate <=? AND R.Availability != '[CANCELED]' GROUP BY City");
        stmt.setDate(1, Date.valueOf(startDate));
        stmt.setDate(2, Date.valueOf(endDate));
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String city = rs.getString("City");
            int num = rs.getInt("TotalReservation");
            System.out.println(city + " : " + num);
        }
    }

    public void report_case2(LocalDate startDate, LocalDate endDate) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT City, Postal_code, COUNT(*) AS TotalReservation FROM Reservation R, Listings L, Address A WHERE L.LID = R.LID AND A.AID=L.AID AND R.StartDate >= ? AND R.EndDate <=? AND R.Availability != '[CANCELED]' GROUP BY City, Postal_code");
        stmt.setDate(1, Date.valueOf(startDate));
        stmt.setDate(2, Date.valueOf(endDate));
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String city = rs.getString("City");
            int num = rs.getInt("TotalReservation");
            String postal_code = rs.getString("Postal_code");
            System.out.println(city + " - " + postal_code+ " : " + num);
        }
    }

    public void report_case3() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Country,COUNT(*) AS TotalListings FROM Listings NATURAL JOIN Address WHERE Status ='[ACTIVE]' GROUP BY Country");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String country = rs.getString("Country");
            int num = rs.getInt("TotalListings");
            System.out.println(country + " : " + num);
        }

    }

    public void report_case4() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT City, Country, COUNT(*) AS TotalListings FROM Listings NATURAL JOIN Address WHERE Status ='[ACTIVE]' GROUP BY City, Country");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String city = rs.getString("City");
            int num = rs.getInt("TotalListings");
            String country = rs.getString("Country");
            System.out.println(country + " - " +city + " : " + num);
        }
    }

    public void report_case5() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Postal_code, City, Country, COUNT(*) AS TotalListings FROM Listings NATURAL JOIN Address WHERE Status ='[ACTIVE]' GROUP BY Postal_code, City, Country");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String city = rs.getString("City");
            int num = rs.getInt("TotalListings");
            String country = rs.getString("Country");
            String postal_code = rs.getString("Postal_code");
            System.out.println(country + " - " + city + " - " + postal_code + " : " + num);
        }
    }

    // per country ranking means ? is it ranking by country to country or is it all ranking by the num of listings only
    public void report_case6() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Country, Name, COUNT(*) AS TotalListings FROM Users U, Listings L, Address A WHERE L.UID=U.UID AND L.AID = A.AID AND Status ='[ACTIVE]' GROUP BY Country, L.UID ORDER BY Country, TotalListings DESC ");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String name = rs.getString("Name");
            String country = rs.getString("Country");
            int num = rs.getInt("TotalListings");
            System.out.println(country + " - " + name + " : " + num);
        }
    }

    public void report_case7() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT City, Country, Name, COUNT(*) AS TotalListings FROM Users U, Listings L, Address A WHERE L.UID=U.UID AND L.AID = A.AID AND Status ='[ACTIVE]' GROUP BY Country, L.UID, City ORDER BY Country, City, TotalListings DESC ");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String name = rs.getString("Name");
            String country = rs.getString("Country");
            int num = rs.getInt("TotalListings");
            String city = rs.getString("City");
            System.out.println(country + " - " + city + " - " + name + " : " + num);
        }
    }

    public void report_case8() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) AS TotalListings, City, Country, Name FROM Listings L JOIN Address A ON L.AID=A.AID JOIN Users U ON U.UID = L.UID WHERE L.Status ='[ACTIVE]' GROUP BY City, Country, Name HAVING TotalListings > 0.1 * (SELECT COUNT(*) FROM Listings L2 JOIN Address A2 ON A2.AID=L2.AID WHERE L2.Status = '[ACTIVE]' AND A2.Country = A.Country AND A.City = A2.City) ORDER BY A.City,A.Country");
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while (rs.next()){
            String name = rs.getString("Name");
            String city = rs.getString("City");
            String country = rs.getString("Country");
            int num = rs.getInt("TotalListings");
            System.out.println(country + " - " + city + " - " + name + " : " + num);
        }
    }

    public void report_case9(LocalDate start, LocalDate end) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("SELECT Name, COUNT(*) AS TotalReservations FROM Reservation R JOIN Users U ON R.UID=U.UID WHERE R.StartDate >= ? AND R.EndDate <=? AND R.Availability != '[CANCELED]' GROUP BY R.UID, U.Name ORDER BY TotalReservations DESC");
        stmt.setDate(1, Date.valueOf(start));
        stmt.setDate(2, Date.valueOf(end));
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String name = rs.getString("Name");
            int num = rs.getInt("TotalReservations");
            System.out.println(name + " : " + num);
        }

    }

    public void report_case10(LocalDate start, LocalDate end) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("SELECT Name, A.City, COUNT(*) AS TotalReservations FROM Reservation R JOIN Users U ON R.UID=U.UID JOIN Listings L ON R.LID=L.LID JOIN Address A ON A.AID=L.AID WHERE R.StartDate >= ? AND R.EndDate <=? AND R.Availability != '[CANCELED]' GROUP BY R.UID, U.Name, A.City HAVING TotalReservations >= 2 ORDER BY A.City, TotalReservations DESC");
        stmt.setDate(1, Date.valueOf(start));
        stmt.setDate(2, Date.valueOf(end));
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        while(rs.next()){
            String name = rs.getString("Name");
            int num = rs.getInt("TotalReservations");
            String city = rs.getString("City");
            System.out.println(city + " - " + name + " : " + num);
        }

    }

    public void report_case11(int year) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Name, COUNT(*) TotalCancellations FROM Reservation R JOIN Users U ON R.UID=U.UID WHERE R.Availability = '[CANCELED]' AND R.CanceledBy = '[GUEST]' AND YEAR(R.StartDate) = ? GROUP BY U.UID HAVING TotalCancellations = (SELECT MAX(TotalCancellations) FROM (SELECT COUNT(*) TotalCancellations FROM Reservation R JOIN Users U ON U.UID=R.UID WHERE R.Availability = '[CANCELED]' AND R.CanceledBy = '[GUEST]' AND YEAR(R.StartDate) = ? GROUP BY U.UID) AS MaxCancellations) ORDER BY TotalCancellations DESC");
        stmt.setInt(1,year);
        stmt.setInt(2,year);
        ResultSet rs = stmt.executeQuery();
        System.out.println("----------------------------");
        System.out.println("Guests:");
        while(rs.next()){
            String name = rs.getString("Name");
            int num = rs.getInt("TotalCancellations");
            System.out.println(name + " : " + num);
        }
        PreparedStatement stmt2 = connection.prepareStatement("SELECT Name, COUNT(*) TotalCancellations FROM Reservation R JOIN Listings L ON R.LID=L.LID JOIN Users U ON U.UID=L.UID WHERE R.Availability = '[CANCELED]' AND R.CanceledBy = '[HOST]' AND YEAR(R.StartDate) = ? GROUP BY U.UID HAVING TotalCancellations = (SELECT MAX(TotalCancellations) FROM (SELECT COUNT(*) TotalCancellations FROM Reservation R JOIN Listings L ON R.LID=L.LID JOIN Users U ON U.UID=L.UID WHERE R.Availability = '[CANCELED]' AND R.CanceledBy = '[HOST]'  AND YEAR(R.StartDate) = ? GROUP BY U.UID) AS MaxCancellations) ORDER BY TotalCancellations DESC");
        stmt2.setInt(1,year);
        stmt2.setInt(2,year);
        ResultSet rs2 = stmt2.executeQuery();
        System.out.println("----------------------------");
        System.out.println("Hosts:");
        while(rs2.next()){
            String name = rs2.getString("Name");
            int num = rs2.getInt("TotalCancellations");
            System.out.println(name + " : " + num);
        }

    }

    public ResultSet get_comments() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT L.LID, R.Comment FROM Listings L, Reservation R WHERE L.LID=R.LID AND L.Status ='[ACTIVE]' AND R.Comment is not null ORDER BY L.LID");
        ResultSet rs = stmt.executeQuery();
        return rs;
    }

    public LinkedHashMap<Integer, Integer> lid_totalReservation() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT L.LID, COUNT(*) AS TotalReviews FROM Listings L, Reservation R WHERE L.LID=R.LID AND L.Status ='[ACTIVE]' AND R.Comment is not null GROUP BY L.LID ORDER BY L.LID");
        ResultSet rs = stmt.executeQuery();
        LinkedHashMap<Integer, Integer> allReviews = new LinkedHashMap<>();
        while(rs.next()){
            int lid = rs.getInt("LID");
            int num = rs.getInt("TotalReviews");
            allReviews.put(lid, num);
        }
        return allReviews;
    }
    //-------------------------------------------------------- FILTERS FOR SEARCH ----------------------------------------------------

    public void view(String filter, String query) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("CREATE VIEW "+filter+" AS ("+query+")");
        stmt.execute();
    }

    public void remove_view(String viewName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP VIEW IF EXISTS " + viewName);
        }
    }
    public PreparedStatement get_queries (String view) throws SQLException {
            return connection.prepareStatement("(SELECT L.* FROM " + view + " L, Calendar C " +
                    "WHERE L.LID=C.LID GROUP BY L.LID) ");
        }

    public ArrayList<Listing> Listings_from_view(PreparedStatement query) throws SQLException {
        //PreparedStatement stmt = connection.prepareStatement(query.);
        ArrayList<Listing> result = new ArrayList<>();
        //ResultSet rs = stmt.executeQuery();
        ResultSet rs = query.executeQuery();
        while(rs.next()) {
            int lid = rs.getInt("LID");
            String type = rs.getString("Type");
            double latitude = rs.getDouble("Latitude");
            double longitude = rs.getDouble("Longitude");

            int aid = rs.getInt("AID");
           String street = rs.getString("Street");
            String city = rs.getString("City");
            String country = rs.getString("Country");
            String postalCode = rs.getString("Postal_code");
            String status = rs.getString("Status");
            Double distance = rs.getDouble("Distance");
            Address newAddress = new Address(aid, street, city, country, postalCode);

            //double price = rs.getDouble("Price");
            //String aux = price == -1 ? "" : "Price: " + price;

            result.add(new Listing(lid, type, latitude, longitude, newAddress, status,distance));
        }

        return result;
    }

    public ArrayList<Listing> Listings_from_postal(PreparedStatement query) throws SQLException {
        //PreparedStatement stmt = connection.prepareStatement(query.);
        ArrayList<Listing> result = new ArrayList<>();
        //ResultSet rs = stmt.executeQuery();
        ResultSet rs = query.executeQuery();
        while(rs.next()) {
            int lid = rs.getInt("LID");
            String type = rs.getString("Type");
            double latitude = rs.getDouble("Latitude");
            double longitude = rs.getDouble("Longitude");

            int aid = rs.getInt("AID");
            String street = rs.getString("Street");
            String city = rs.getString("City");
            String country = rs.getString("Country");
            String postalCode = rs.getString("Postal_code");
            String status = rs.getString("Status");
            //Double distance = rs.getDouble("Distance");
            Address newAddress = new Address(aid, street, city, country, postalCode);

            //double price = rs.getDouble("Price");
            //String aux = price == -1 ? "" : "Price: " + price;
            result.add(new Listing(lid, type, latitude, longitude, newAddress, status));
        }

        return result;
    }

    //-------------------------------------------------------- AMENITIES & HOST TOOL KIT  ----------------------------------------------------

    public ArrayList<String> find_amenities(int category) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Category NATURAL JOIN Amenities where Category_ID =?");
        stmt.setInt(1, category);
        ResultSet rs = stmt.executeQuery();
        ArrayList<String> amenities = new ArrayList<>();
        while(rs.next()){
            String amenity = rs.getString("Amenity_Name");
            amenities.add(amenity);
        }
        return  amenities;
    }

    public int find_amenityID(int cat, String amenity) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Category NATURAL JOIN Amenities where Category_ID =? AND Amenity_Name =?");
        stmt.setInt(1, cat);
        stmt.setString(2,amenity);
        ResultSet rs = stmt.executeQuery();
        int amenityID = 0;
        while(rs.next()){
            amenityID = rs.getInt("Amenities_ID");
        }
        return  amenityID;
    }
    public void add_amenity(int lid, int amenityID) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO AmenitiesListing (LID, Amenities_ID) VALUES (?, ?)");
        stmt.setInt(1, lid);
        stmt.setInt(2, amenityID);
        stmt.executeUpdate();
    }

    public ArrayList<Amenity> listing_amenities(int lid) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Category_ID, Amenity_Name, Amenities_ID FROM Amenities NATURAL JOIN AmenitiesListing WHERE LID = ?");
        stmt.setInt(1, lid);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Amenity> amenities = new ArrayList<>();
        while(rs.next()){
            int amenity_id = rs.getInt("Amenities_ID");
            int cat = rs.getInt("Category_ID");
            String name = rs.getString("Amenity_Name");
            amenities.add(new Amenity(amenity_id, name, cat));
        }
        return amenities;
    }

    public double listing_avg_price(String set, String type, String country, int size) throws SQLException {
        //PreparedStatement stmt = connection.prepareStatement("WITH F1 AS (SELECT LID FROM Listings NATURAL JOIN Address WHERE Type =? AND Country =?), F2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) >= "+ size +"), TEMP AS (SELECT AVG(Price) FROM Calendar WHERE LID IN (SELECT * FROM F1) AND LID IN (SELECT * FROM F2) GROUP BY LID) SELECT AVG(Price) AS RESULT FROM TEMP");
//        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
//                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND Country = ?), " +
//                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) = "+ size +"), " +
//                "temp AS (SELECT AVG(Price) AS Price FROM Calendar " +
//                "WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) " +
//                "GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND Country = ?), " +
                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN " + set + " GROUP BY Listings.LID HAVING COUNT(*) = " + size + "), " +
                "ExactAmenities AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID GROUP BY Listings.LID HAVING COUNT(*) = " + size + "), " +
                "temp AS (SELECT AVG(Price) AS Price FROM Calendar WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) AND LID IN (SELECT * FROM ExactAmenities) GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        stmt.setString(1, type);
        stmt.setString(2, country);
        ResultSet rs = stmt.executeQuery();
        double price = 0;
        if (rs.next()){
            price = rs.getDouble("RESULT");
        }
        return price;
    }

    public double listing_avg_price(String set, String type, String country, String city, int size) throws SQLException {
//        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
//                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND City =? AND Country = ?), " +
//                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) = "+ size +"), " +
//                "temp AS (SELECT AVG(Price) AS Price FROM Calendar " +
//                "WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) " +
//                "GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND City = ? AND Country = ?), " +
                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN " + set + " GROUP BY Listings.LID HAVING COUNT(*) = ?), " +
                "ExactAmenities AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID GROUP BY Listings.LID HAVING COUNT(*) = ?), " +
                "temp AS (SELECT AVG(Price) AS Price FROM Calendar WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) AND LID IN (SELECT * FROM ExactAmenities) GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        stmt.setString(1, type);
        stmt.setString(2, city);
        stmt.setString(3, country);
        stmt.setInt(4, size);
        stmt.setInt(5, size);
        ResultSet rs = stmt.executeQuery();
        double price = 0;
        if (rs.next()){
            price = rs.getDouble("RESULT");
        }
        return price;

    }
    public double listing_avg_price(String set, String type, String country,String city, String postalcode, int size) throws SQLException {
//        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
//                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND City =? AND Country = ? AND Postal_code=?), " +
//                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) = "+ size +"), " +
//                "temp AS (SELECT AVG(Price) AS Price FROM Calendar " +
//                "WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) " +
//                "GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        PreparedStatement stmt = connection.prepareStatement("WITH Filter1 AS " +
                "(SELECT LID FROM Listings NATURAL JOIN Address WHERE Type = ? AND City = ? AND Country = ? AND Postal_code = ?), " +
                "Filter2 AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN " + set + " GROUP BY Listings.LID HAVING COUNT(*) = " + size + "), " +
                "ExactAmenities AS (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID GROUP BY Listings.LID HAVING COUNT(*) = " + size + "), " +
                "temp AS (SELECT AVG(Price) AS Price FROM Calendar WHERE LID IN (SELECT * FROM Filter1) AND LID IN (SELECT * FROM Filter2) AND LID IN (SELECT * FROM ExactAmenities) GROUP BY LID) SELECT AVG(Price) AS RESULT FROM temp");
        stmt.setString(1, type);
        stmt.setString(2,city);
        stmt.setString(3, country);
        stmt.setString(4, postalcode);
        ResultSet rs = stmt.executeQuery();
        double price = 0;
        if (rs.next()){
            price = rs.getDouble("RESULT");
        }
        return price;

    }

//    public double avg_dist_from_attraction(double latitude, double longitude,double lat2, double lon2) throws SQLException {
//        PreparedStatement stmt = connection.prepareStatement( "SELECT ST_Distance_Sphere(point("+lon2+","+lat2+"), point("+longitude+", "+latitude+")) as Distance ");
//        ResultSet rs = stmt.executeQuery();
//    }
public double avg_dist_from_attraction(double latitude, double longitude,double lat2, double lon2) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(
            "SELECT AVG(ST_Distance_Sphere(point(?, ?), point(?, ?))) as AverageDistance "
    );
    stmt.setDouble(1, lon2);
    stmt.setDouble(2, lat2);
    stmt.setDouble(3, longitude);
    stmt.setDouble(4, latitude);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        return rs.getDouble("AverageDistance");
    }
    return -1;
}

public double recommend_price(double latitude, double longitude, int dist1, int dist2 , int size, String set) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(
            "WITH Filter1 AS (" +
                    "SELECT L.LID, ST_Distance_Sphere(point(Longitude, Latitude), point(?, ?)) as Distance " +
                    "FROM Listings L JOIN Address A ON L.AID=A.AID " +
                    "HAVING Distance <= ? AND Distance >= ? " +
                    "), " +
                    "Filter2 AS (" +
                    "SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID " +
                    "JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN " + set +
                    "GROUP BY Listings.LID HAVING COUNT(*) = " + size +
                    "), " +
                    "ExactAmenities AS (" +
                    "SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID " +
                    "GROUP BY Listings.LID HAVING COUNT(*) = " + size +
                    "), " +
                    "temp AS (" +
                    "SELECT AVG(Price) AS Price FROM CALENDAR WHERE LID IN (SELECT LID FROM Filter1) " +
                    "AND LID IN (SELECT LID FROM Filter2) AND LID IN (SELECT LID FROM ExactAmenities) " +
                    ") SELECT AVG(Price) AS RESULT FROM temp");

    stmt.setDouble(1, longitude);
    stmt.setDouble(2, latitude);
    stmt.setInt(3, dist1);
    stmt.setInt(4, dist2);
    ResultSet rs = stmt.executeQuery();
    double price = 0;
    if (rs.next()) {
        price = rs.getDouble("RESULT");
    }
    return price;
}

public ArrayList<Amenity> offer_essentials(String user_amenities, String City, String Country, String type, String Category) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Amenities.*, COUNT(*)/" +
                "(SELECT COUNT(*) FROM LISTINGS NATURAL JOIN ADDRESS WHERE Type =? AND City =? AND Country=?) as Prop FROM AmenitiesListing " +
                "NATURAL JOIN Amenities NATURAL JOIN Category WHERE Category_Name=? " +
                "AND Amenity_Name NOT IN " + user_amenities +
                " GROUP BY Amenity_Name,Category_ID,Amenities_ID ORDER BY Prop DESC LIMIT 10");
        stmt.setString(1,type);
        stmt.setString(2,City);
        stmt.setString(3,Country);
        stmt.setString(4,Category);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Amenity> recommendations = new ArrayList<>();
        while(rs.next()){
            int cid = rs.getInt("Category_ID");
            String amenity_name = rs.getString("Amenity_Name");
            int Amenities_id = rs.getInt("Amenities_ID");
            recommendations.add(new Amenity(Amenities_id,amenity_name,cid));

        }
        return recommendations;
    }

    public ArrayList<Amenity> offer_unpopular(String set, String popular_amenities, String category) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Amenities.* FROM Amenities NATURAL JOIN Category WHERE Category_Name=? AND Amenity_Name NOT IN "+popular_amenities+" AND Amenity_Name NOT IN "+set+" GROUP BY Amenity_Name, Category_ID, Amenities_ID");
        stmt.setString(1,category);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Amenity> unpopular_amenities = new ArrayList<>();
        while (rs.next()){
            int cid = rs.getInt("Category_ID");
            String amenity_name = rs.getString("Amenity_Name");
            int Amenities_id = rs.getInt("Amenities_ID");
            unpopular_amenities.add(new Amenity(Amenities_id,amenity_name,cid));
        }
        return unpopular_amenities;
    }

    public double find_revenue(String name) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Percentage FROM Revenue WHERE Amenity_Name =?");
        stmt.setString(1,name);
        ResultSet rs = stmt.executeQuery();
        double percentage = 0.0;
        if(rs.next()){
           percentage = rs.getDouble("Percentage");
        }
        return percentage;
    }

    public double findAvgPrice_listingHasAmenity(String name, String type, String country, String city) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT AVG(Price) AS AveragePrice "
                + "FROM Listings "
                + "JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID "
                + "JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID "
                + "JOIN Calendar ON Listings.LID = Calendar.LID "
                + "JOIN Address ON Listings.AID = Address.AID "
                + "WHERE Amenities.Amenity_Name = ? AND Type = ? AND City = ? AND Country = ?");
        stmt.setString(1, name);
        stmt.setString(2,type);
        stmt.setString(3, city);
        stmt.setString(4,country);
        ResultSet rs = stmt.executeQuery();
        double price = 0;
        if(rs.next()){
            price = rs.getDouble("AveragePrice");
        }
        return price;
    }

    public double findAvgPrice_listingNoAmenity(String name, String type, String country, String city) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT AVG(Price) AS AveragePrice "
                + "FROM Listings "
                + "JOIN Calendar ON Listings.LID = Calendar.LID "
                + "JOIN Address ON Listings.AID = Address.AID "
                + "WHERE Listings.LID NOT IN ("
                + "  SELECT AmenitiesListing.LID FROM AmenitiesListing "
                + "  JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID "
                + "  WHERE Amenities.Amenity_Name = ?"
                + ") AND Type = ? AND City = ? AND Country = ?");
        stmt.setString(1, name);
        stmt.setString(2,type);
        stmt.setString(3, city);
        stmt.setString(4,country);
        ResultSet rs = stmt.executeQuery();
        double price = 0;
        if(rs.next()){
            price = rs.getDouble("AveragePrice");
        }
        return price;
    }



    public int find_num_reservations(String type, String country, String city) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT AVG(TotalReservation) AS AvgReservation FROM (SELECT L.LID, COUNT(*) AS TotalReservation FROM Reservation R, Listings L, Address A WHERE L.LID = R.LID AND A.AID=L.AID AND R.Availability != '[CANCELED]' AND A.City=? AND A.Country=? AND L.Type =? GROUP BY L.LID) AS SubQuery");
        stmt.setString(1, city);
        stmt.setString(2, country);
        stmt.setString(3, type);
        ResultSet rs = stmt.executeQuery();
        int num = 0;
        if(rs.next()){
            num = rs.getInt("AvgReservation");
        }
        return num;
    }

    public void close_connection() throws SQLException {
        connection.close();
    }



}







