import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final String Project = "project";
    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";

    private static final String User = "root";
    private static final String Password ="Bhr_1232003";
    public static User current_user=null;
    static Database data;

    public static void displayMenu(){
        System.out.println("Enter the number next to the operation you would like to do");
        System.out.println("1: Log In");
        System.out.println("2: Sign Up");
        System.out.println("3: Reports");
        System.out.println("4: Exit");
    }

    public static void displayGuestMenu(){
        System.out.println("Enter the number next to the operation you would like to do");
        System.out.println("1: View Listings"); // make a reservation after search
        System.out.println("2: My Reservations"); // view info and cancel a reservation within upcoming reservations
        System.out.println("3: Review My Stay");
        System.out.println("4: Delete My Account");
        System.out.println("5: Log Out");
    }

    public static void displayHostMenu(){
        System.out.println("Enter the number next to the operation you would like to do");
        System.out.println("1: Create a Listing");
        System.out.println("2: My Listings"); // see the listing and update it
        System.out.println("3: My Listing Reservations"); // view the info and cancel
        System.out.println("4: Review My Guest");
        System.out.println("5: Delete My Account");
        System.out.println("6: Log Out");
    }

    public static void Signup() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1: Guest");
        System.out.println("2: Host");
        int num = scanner.nextInt();

        System.out.print("Email: ");
        String email = scanner.next();

        System.out.print("Password: ");
        String password = scanner.next();

        // Check if the user already exists in your system
        User user = data.get_user(email.toLowerCase().trim());
        if (user != null) {
                if (user.Account().equals("[INACTIVE]")) {
                    // check if the password matches
                    if (!user.Password().equals(password)) {
                        System.out.println("Wrong password - Unable to proceed with reactivation");
                        return;
                    }
                    // Reactivate the account if it's inactive
                    data.activate_account(user.uid());
                    System.out.println("Your account has been reactivated!");
                    return;
                }
        }

        scanner.nextLine();
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("SIN: ");
        String SIN = scanner.next();

        System.out.print("DOB (YYYY-MM-DD): ");
        String DoB = scanner.next();

        scanner.nextLine();
        System.out.print("Occupation: ");
        String occupation = scanner.nextLine();

        System.out.print("Street: ");
        String street = scanner.nextLine();

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Country: ");
        String country = scanner.nextLine();

        System.out.print("Postal Code: ");
        String postalcode = scanner.next();

        if (num == 1) {
            System.out.print("Payment: ");
            String payment_info = scanner.next();
            if (user != null) {
                Guest guest = data.get_guest(user);
                if(guest == null) {
                    data.add_guest(user.uid(), payment_info);
                    System.out.println("Your account is successfully registered as Guest");
                }
            } else {
                Guest.Signup(SIN, name,DoB,occupation,email,password,city,country,postalcode,street, payment_info, data);
            }
        } else if (num == 2) {
            if (user != null) {
                Host host = data.get_host(user);
                if(host == null) {
                    data.add_host(user.uid());
                    System.out.println("Your account is successfully registered as Host");
                }
            } else {
                Host.Signup(SIN, name,DoB,occupation,email,password,city,country,postalcode,street, data);
            }
        }
    }


    public static void Login() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1: Guest");
        System.out.println("2: Host");

        int num = scanner.nextInt();

        System.out.print("Email: ");
        String email = scanner.next();

        System.out.print("Password: ");
        String password = scanner.next();
        if ( num == 1){

            current_user=Guest.login(email,password,data);

        }
        if( num==2){
            current_user=Host.login(email,password,data);
        }

    }

    public static void Host_options(int num,User user) throws SQLException {
        if(num==1){
            create_listings();
        }
        if (num==2){
            handle_listings_host();
        }
        if (num==3){
            handle_reservations();

        }
        if (num==4){
            review_guest();
        }
        if(num==5) {
            data.deactivate_host(user.uid());
        }
        if(num==6) {
            //logout
        }

    }

    public static void Guest_options(int num,User user) throws SQLException {
        if(num==1){
            view_listings();
        }
        if(num == 2){
            handle_reservations();
        }
        if(num == 3){
            review_reservations();
        }
        if(num==4) {
            data.deactivate_guest(user.uid());
        }
        if(num==5) {
            //logout
        }

    }

    public static void create_listings() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter The Type of Listing");
        System.out.println("1:House");
        System.out.println("2:Apartment");
        System.out.println("3:Cabin ");
        System.out.println("4:GuestHouse");
        System.out.println("5:Hotel");
        System.out.println("6:Tiny home ");
        String type = scanner.nextLine();
        if (!(type.equalsIgnoreCase("House") || type.equalsIgnoreCase("Apartment") || type.equalsIgnoreCase("Cabin") || type.equalsIgnoreCase("GuestHouse") || type.equalsIgnoreCase("Hotel") || type.equalsIgnoreCase("Tiny home"))) {
            System.out.println("Not a Valid type");
            return;
        }
        System.out.println("Enter the Latitude of the Listing (-90 to 90)");
        double lat= scanner.nextDouble();
        System.out.println("Enter the Longitude of the Listing (-180 to 180)");
        double lon= scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Street: ");
        String street = scanner.nextLine();

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Country: ");
        String country = scanner.nextLine();

        System.out.print("Postal Code: ");
        String postalcode = scanner.next();

        int lid = Listing.post_listings(data,current_user.uid(),type,lon,lat,street,city,postalcode,country);

        System.out.println("Add Amenities:");
        add_amenities(lid);
        scanner.nextLine();

        System.out.println("Would you like to use HostToolKit to recommend price 1:YES or 2:NO");
        int num = scanner.nextInt();
        if(num == 1){
            recommend_price(lid, type, city, country, postalcode, lat,lon, true);
        }
        System.out.println("Would you like to use HostToolKit to recommend Amenities 1:YES or 2:NO");
        int num2 = scanner.nextInt();
        if(num2==1){
            HashMap<String,Double> revenue=recommend_amenities(lid,type,city ,country);
            System.out.println(" Do you want to select the amenities to display your anticipated revenue (1:Yes|2:No)");
            int num3= scanner.nextInt();
            if(num3==1){
                double price = recommend_price(lid, type, city, country, postalcode, lat,lon, false);
                int num_reservations = data.find_num_reservations(type, country, city);
                calculate_revenue(revenue,price, num_reservations);
            }


        }


    }

    public static HashMap<String,Double> recommend_amenities( int Lid,String type , String city, String country) throws SQLException {
        ArrayList<Amenity> amenities = data.listing_amenities(Lid);

        ArrayList<Amenity> unpopular = new ArrayList<>();
        String set =  make_string(amenities);
        ArrayList<Amenity> suggested_amenities = new ArrayList<>();
        HashMap<String, Double> revenueIncreaseMap = new HashMap<>();
        System.out.println("--------------------------");
        System.out.println("1: Essentials:");
        suggested_amenities=data.offer_essentials(set,city,country,type,"Essentials");
        String essentials = make_string(suggested_amenities);
        System.out.println(essentials);
        unpopular = data.offer_unpopular(set, essentials, "Essentials");
        double rev_increase = 5.0;
        System.out.println("Trending Essentials:");
        for (int i=0; i<suggested_amenities.size(); i++){
            double percentage = data.find_revenue(suggested_amenities.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(suggested_amenities.get(i).Amenity_Name(),result);
            System.out.println(suggested_amenities.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
            rev_increase= rev_increase-0.5;
        }
        rev_increase = 3.0;
        System.out.println();
        System.out.println("Non-Trending Essentials:");
        for(int i=0; i< unpopular.size(); i++){
            double percentage = data.find_revenue(unpopular.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(unpopular.get(i).Amenity_Name(),result);
            System.out.println(unpopular.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
        }
        System.out.println("--------------------------");
        System.out.println("2: Safety:");
        rev_increase= 3.0;
        suggested_amenities=data.offer_essentials(set,city,country,type,"Safety");
        String safety = make_string(suggested_amenities);
        unpopular = data.offer_unpopular(set, safety, "Safety");
        System.out.println("Important Safety Amenities:");
        for ( int i=0; i<suggested_amenities.size(); i++){
            double percentage = data.find_revenue(suggested_amenities.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(suggested_amenities.get(i).Amenity_Name(),result);
            System.out.println(suggested_amenities.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
            rev_increase= rev_increase-0.5;
        }
        rev_increase = 4.0;
        System.out.println();
        System.out.println("Other Safety Amenities:");
        for(int i=0; i< unpopular.size(); i++){
            double percentage = data.find_revenue(unpopular.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(unpopular.get(i).Amenity_Name(),result);
            System.out.println(unpopular.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
        }
        System.out.println("--------------------------");
        System.out.println("3: Standout:");
        rev_increase=5.0;
        suggested_amenities=data.offer_essentials(set,city,country,type,"Standout");
        String standout = make_string(suggested_amenities);
        unpopular = data.offer_unpopular(set, standout, "Standout");
        System.out.println("Trending Standouts:");
        for ( int i=0; i<suggested_amenities.size(); i++){
            double percentage = data.find_revenue(suggested_amenities.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(suggested_amenities.get(i).Amenity_Name(),result);
            System.out.println(suggested_amenities.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
            rev_increase= rev_increase-0.5;
        }
        rev_increase = 0.1;
        System.out.println();
        System.out.println("Cool Standouts:");
        for(int i=0; i< unpopular.size(); i++){
            double percentage = data.find_revenue(unpopular.get(i).Amenity_Name());
            double result = rev_increase + percentage;
            revenueIncreaseMap.put(unpopular.get(i).Amenity_Name(),result);
            System.out.println(unpopular.get(i).Amenity_Name()+": (Revenue Increase by : "+ percentage + " + "+ rev_increase+ " = " + result +" % per night)");
        }


        return revenueIncreaseMap;
    }

    public static void calculate_revenue(HashMap<String, Double> revenue, double price, int num) {
        Scanner scanner = new Scanner(System.in);
        double totalPriceIncrease = 0.0;
        while (!revenue.isEmpty()) {
            for (String amenityName : revenue.keySet()) {
                System.out.println(amenityName + ": " + revenue.get(amenityName) + "% increase");
            }
            System.out.println("Enter the amenity name you want to select (or 'exit' to finish): ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            if (revenue.containsKey(input)) {
                totalPriceIncrease += revenue.get(input);
                revenue.remove(input);
            } else {
                System.out.println("Invalid input, please try again.");
            }
        }
        double finalPrice = price + (price * totalPriceIncrease / 100);
        System.out.println("*********************************************************************************************");
        System.out.println("The base price before considering all chosen amenities is: " +price);
        System.out.println("The final price, considering all chosen amenities, is: " + finalPrice);
        System.out.println("The percentage increase in your revenue  : "+totalPriceIncrease+" %");
        System.out.println("The Revenue increase in Dollars  :  $ "+(finalPrice-price));
        if(num == 0){
            System.out.println("There is no enough data to calculate expected revenue based on reservation");
        }
        else{
            System.out.println("The average number of Reservation for such Listing is : " + num);
            System.out.println("The expected revenue for the Listing based on the Reservation data : " + (finalPrice * num));
        }
    }




    public static void add_amenities(int lid) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> amenities = new ArrayList<>();
        boolean typeOfPlaceSelected = false;

        while(true) {
            System.out.println(typeOfPlaceSelected ? "1: Type of Place (already selected)" : "1: Type of Place (Must be entered in order to quit)");
            System.out.println("2: Essentials");
            System.out.println("3: Standout");
            System.out.println("4: Safety");
            System.out.println("5: Exit");
            int num = scanner.nextInt();
            scanner.nextLine();

            if (num == 5 && !typeOfPlaceSelected) {
                System.out.println("You must select the 'Type of Place' before exiting.");
                continue; // Continue to next iteration, forcing user to make a selection
            }

            if (num == 5 && typeOfPlaceSelected) {
                break; // Exit if "Type of Place" has been selected
            }

            if (num == 1 && !typeOfPlaceSelected) {
                amenities = data.find_amenities(1);
                for (int i = 0; i < amenities.size(); i++) {
                    System.out.println(i + 1 + " : " + amenities.get(i));
                }
                String ans = scanner.nextLine();
                if (amenities.contains(ans)) {
                    amenities.remove(ans);
                    int id = data.find_amenityID(num, ans);
                    data.add_amenity(lid, id);
                }
                typeOfPlaceSelected = true;
            }

            if (num >= 2 && num <= 4) {
                amenities = data.find_amenities(num);
                while (true) {
                    System.out.println("Amenities under this category: (Quit - 0)");
                    for (int i = 0; i < amenities.size(); i++) {
                        System.out.println(i + 1 + " : " + amenities.get(i));
                    }
                    String ans = scanner.nextLine();
                    if (ans.equals("0")) {
                        break;
                    }
                    if (amenities.contains(ans)) {
                        amenities.remove(ans);
                        int id = data.find_amenityID(num, ans);
                        data.add_amenity(lid, id);
                    }
                }
            }
        }


    }



    public static void handle_listings_host() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        display_host_lisitings();
        System.out.println("1: Delete My Listing");
        System.out.println("2: Update My Listing");
        int num = scanner.nextInt();
        ArrayList<Listing> host_listing = data.get_host_Listing(current_user.uid());
        if(num == 1){
            System.out.println("Enter the number next to the Listing");
            int num2 = scanner.nextInt();
            if(num2 >= 0 && num2 < host_listing.size()){
                data.delete_listing(host_listing.get(num2).LID());
            }

        }
        else if(num == 2){
            System.out.println("1: Add dates");
            System.out.println("2: Remove dates");
            System.out.println("3: Update Price");
            int num1 = scanner.nextInt();
            System.out.println("Enter the number next to the Listing");
            int num2 = scanner.nextInt();
            if(num2 >= 0 && num2 < host_listing.size()){
                calendar_for_listing(num1, host_listing.get(num2).LID());  // updating the calendar dates for the listing.
            }
        }

    }
    public static void display_host_lisitings() throws SQLException {
        ArrayList<Listing>host_listing= data.get_host_Listing(current_user.uid());
        for ( int i=0; i<host_listing.size();i++){
            System.out.println(i+":"+host_listing.get(i));
        }

    }

    public static void calendar_for_listing(int num, int lid) throws SQLException {
        if(num == 1){
            add_dates(lid);
        }
        else if(num == 2){
            remove_dates(lid);
        }
        else if(num == 3){
            update_price(lid);
        }

    }
    public static void add_dates(int lid) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the start date (YYYY-MM-DD)");
        String start = scanner.nextLine();
        System.out.println("Enter the end date (YYYY-MM-DD)");
        String end = scanner.nextLine();
        System.out.println("Enter the price per night");
        double price = scanner.nextDouble();

        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

        if(data.check_status_calendar(lid, startDate, endDate, "[OPEN]")){
            System.out.println("There are [OPEN] days exist within this range");
        }
        int added = data.add_calendar(lid, start, end, price);
        System.out.println("There are " + added + " newly ADDED dates between " + start +" and " + end);
    }

    public static void remove_dates(int lid) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the start date (YYYY-MM-DD)");
        String start = scanner.nextLine();
        System.out.println("Enter the end date (YYYY-MM-DD)");
        String end = scanner.nextLine();

        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

        // check if the dates were booked
        if(data.check_reservation(lid, startDate, endDate)){
            System.out.println("There are [RESERVED] days between " + start + " and " + end);
        }

        int removed = data.remove_calendar(lid, startDate, endDate);
        System.out.println("There are " + removed + " number of dates REMOVED between" + start + " and " + end);
    }

    public static void update_price(int lid) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the start date (YYYY-MM-DD)");
        String start = scanner.nextLine();
        System.out.println("Enter the end date (YYYY-MM-DD)");
        String end = scanner.nextLine();
        System.out.println("Enter the new price per night");
        double price = scanner.nextDouble();

        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

        // check if the dates were booked
        if(data.check_reservation(lid, startDate, endDate)){
            System.out.println("There are [RESERVED] days between " + start + " and " + end);
        }

        int updated = data.update_price(lid, startDate, endDate, price);
        System.out.println("There are " + updated + " number of days between" + start + " and " + end + " with " + price +" per night");
    }


    public static void view_listings() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("1:Search by Latitude and Longitude");
        System.out.println("2:Search by Postalcode");
        System.out.println("3:Search by Address");
        int num = scanner.nextInt();
        StringBuilder filter_query = new StringBuilder();
        ArrayList<Listing>Listing = new ArrayList<>();
        if(num==1){
            Listing=search_by_coords();
        }
        if(num==2){
            Listing=search_by_postal();
        }
        if(num==3){
            Listing=search_by_address();
        }
        System.out.println("Make a Reservation 1:Yes or 2:No");
        int num2= scanner.nextInt();
        if(num2==1){
            Make_a_reservation(Listing);
        }
    }
    public static ArrayList<Listing> search_by_coords() throws SQLException {
        double latitude;
        double longitude;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Latitude (-90 to 90)");
        latitude= scanner.nextDouble();
        System.out.println("Enter Longitude (-180 to 180)");
        longitude= scanner.nextDouble();
        System.out.println("1: Enter a Specific distance in (Km)");
        System.out.println("2: Default within (5 Km)");
        double dist=5000;
        double num = scanner.nextInt();
        if (num==1){
            System.out.println("Enter the distance:");
            dist= scanner.nextDouble();
            dist=dist*1000;

        }
        System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-15s %n", "LID", "Type", "Latitude", "Longitude", "Street", "City", "Country", "Postalcode", "Status","Distance");
        ArrayList<Listing>coords=data.search_coords(latitude,longitude,dist);
        // Print each Listing in a table format
        for (Listing listing :coords) {
            Address address = listing.address();
            System.out.printf("%-5d %-10s %-10f %-10f %-10s %-10s %-10s %-10s %-10s %-15s %n", listing.LID(), listing.type(), listing.Latitude(), listing.Longitude(), address.Street(), address.city(), address.Country(), address.postal_code(), listing.status(),listing.distance());
        }
        data.remove_view("Filter0");
        data.remove_view("Filter1");
        data.remove_view("Filter2");
        data.remove_view("Filter3");
        ArrayList<Listing> listings;
        StringBuilder filter_query0 = new StringBuilder();
        filter_query0.append("SELECT *, ST_Distance_Sphere(point(Longitude, Latitude), point("+longitude+", "+latitude+")) as Distance FROM Listings JOIN Address USING (AID) WHERE Status='[ACTIVE]' Having Distance <= " + dist + " ORDER BY Distance");
        data.view("Filter0",filter_query0.toString());
        System.out.println("Search by Filter 1:Yes or 2:No");
        int num3 = scanner.nextInt();
        if (num3==1) {
            System.out.println("Filter by date range 1:Yes or 2:No");
            int fil1 = scanner.nextInt();
            scanner.nextLine();
            StringBuilder filter_query1 = new StringBuilder();
            if (fil1 == 1) {
                System.out.println("Enter the start date (YYYY-MM-DD)");
                String start = scanner.nextLine();
                //scanner.nextLine();
                System.out.println("Enter the end date (YYYY-MM-DD)");
                //scanner.nextLine();
                String end = scanner.nextLine();
                LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

                filter_query1.append("SELECT D.* FROM Filter0 D JOIN Listings L ON D.LID = L.LID JOIN Calendar C ON D.LID=C.LID WHERE C.Availability='[OPEN]' AND C.Date BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY D.LID HAVING COUNT(*) >= DATEDIFF('" + endDate + "', '" + startDate + "') + 1");
                data.view("Filter1", filter_query1.toString());
                //listings = data.Listings_from_view(filter_query1.toString());
            } else {
                data.view("Filter1", "SELECT * FROM Filter0");
               // listings = data.Listings_from_view(filter_query0.toString());
            }

            System.out.println("Filter by Price range 1:Yes or 2:No");
            int fil2 = scanner.nextInt();
            StringBuilder filter_query2 = new StringBuilder();
            if (fil2 == 1) {
                System.out.println("Enter the Minimum price range");
                double min = scanner.nextDouble();
                System.out.println("Enter the Maximum price range");
                double max = scanner.nextDouble();
                filter_query2.append("SELECT F.* FROM Filter1 F JOIN(SELECT L.LID FROM LISTINGS L JOIN CALENDAR C ON L.LID = C.LID GROUP BY L.LID HAVING AVG(Price) BETWEEN " + min + " AND " + max + ")AS subquery ON F.LID = subquery.LID");
                data.view("Filter2", filter_query2.toString());
            } else {
                data.view("Filter2", "SELECT * FROM Filter1");
            }
            System.out.println("Filter by Amenities 1:Yes or 2:No");
            int fil3= scanner.nextInt();
            StringBuilder filter_query3 = new StringBuilder();
            scanner.nextLine();
            if(fil3==1){
                set_of_amenities();
                System.out.println(" Enter the amenities by which you would like to filter the results");
                String string = scanner.nextLine();
                String [] amenities= string.split(",");
                StringBuilder set = new StringBuilder();
                set.append("(");
                for (int i=0; i<amenities.length; i++) {
                    if (i==0) {
                        set.append("'" + amenities[i].trim() + "'");
                    } else {
                        set.append("," + "'" + amenities[i].trim() + "'");
                    }
                }
                set.append(")");
                filter_query3.append("SELECT * FROM Filter2 WHERE LID IN (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) >= "+ amenities.length +")");
                data.view("Filter3", filter_query3.toString());
            }
            else{
                data.view("Filter3", "SELECT * FROM Filter2");
            }



            PreparedStatement s = data.get_queries("Filter3");
            listings = data.Listings_from_view(s);

            System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-15s %n", "LID", "Type", "Latitude", "Longitude", "Street", "City", "Country", "Postalcode", "Status","Distance");
            for (Listing listing :listings) {
                Address address = listing.address();
                double km = listing.distance()/1000;
                System.out.printf("%-5d %-10s %-10f %-10f %-10s %-10s %-10s %-10s %-10s %-15s %n", listing.LID(), listing.type(), listing.Latitude(), listing.Longitude(), address.Street(), address.city(), address.Country(), address.postal_code(), listing.status(),km);
            }
            data.remove_view("Filter0");
            data.remove_view("Filter1");
            data.remove_view("Filter2");
            data.remove_view("Filter3");

            return listings;
        }
            // System.out.println("Filter by Amenities 1:Yes or 2:No");
//         int fil3 = scanner.nextInt();
       return coords;

    }

    public static ArrayList<Listing> search_by_postal() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Postal Code");
        String postalcode = scanner.nextLine();
        String code = postalcode.toUpperCase().trim().substring(0,3);
        ArrayList<Listing>post_listing= data.search_postalcode(postalcode);
        print_table(post_listing);
        data.remove_view("Filter3");
        data.remove_view("Filter4");
        data.remove_view("Filter5");
        data.remove_view("Filter6");
        ArrayList<Listing> listings;
        StringBuilder filter_query0 = new StringBuilder();
        filter_query0.append("SELECT * FROM LISTINGS NATURAL JOIN ADDRESS WHERE Status='[ACTIVE]'AND SUBSTRING(Postal_code,1,3) ='"+code+"'");
        data.view("Filter3",filter_query0.toString());
        System.out.println("Search by Filter 1:Yes or 2:No");
        int num3 = scanner.nextInt();
        if (num3==1) {
            System.out.println("Filter by date range 1:Yes or 2:No");
            int fil1 = scanner.nextInt();
            scanner.nextLine();
            StringBuilder filter_query1 = new StringBuilder();
            if (fil1 == 1) {
                System.out.println("Enter the start date (YYYY-MM-DD)");
                String start = scanner.nextLine();
                //scanner.nextLine();
                System.out.println("Enter the end date (YYYY-MM-DD)");
                //scanner.nextLine();
                String end = scanner.nextLine();
                LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

                filter_query1.append("SELECT D.* FROM Filter3 D JOIN Listings L ON D.LID = L.LID JOIN Calendar C ON D.LID=C.LID WHERE C.Availability='[OPEN]' AND C.Date BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY D.LID HAVING COUNT(*) >= DATEDIFF('" + endDate + "', '" + startDate + "') + 1");
                data.view("Filter4", filter_query1.toString());
                //listings = data.Listings_from_view(filter_query1.toString());
            } else {
                data.view("Filter4", "SELECT * FROM Filter3");
                // listings = data.Listings_from_view(filter_query0.toString());
            }

            System.out.println("Filter by price range 1:Yes or 2:No");
            int fil2 = scanner.nextInt();
            StringBuilder filter_query2 = new StringBuilder();
            if (fil2 == 1) {
                System.out.println("Enter the Minimum price range");
                double min = scanner.nextDouble();
                System.out.println("Enter the Maximum price range");
                double max = scanner.nextDouble();
                filter_query2.append("SELECT F.* FROM Filter4 F JOIN(SELECT L.LID FROM LISTINGS L JOIN CALENDAR C ON L.LID = C.LID GROUP BY L.LID HAVING AVG(Price) BETWEEN " + min + " AND " + max + ")AS subquery ON F.LID = subquery.LID");
                data.view("Filter5", filter_query2.toString());
            } else {
                data.view("Filter5", "SELECT * FROM Filter4");
            }
            System.out.println("Filter by Amenities 1:Yes or 2:No");
            int fil3= scanner.nextInt();
            StringBuilder filter_query3 = new StringBuilder();
            scanner.nextLine();
            if(fil3==1){
                set_of_amenities();
                System.out.println(" Enter the amenities by which you would like to filter the results");
                String string = scanner.nextLine();
                String [] amenities= string.split(",");
                StringBuilder set = new StringBuilder();
                set.append("(");
                for (int i=0; i<amenities.length; i++) {
                    if (i==0) {
                        set.append("'" + amenities[i].trim() + "'");
                    } else {
                        set.append("," + "'" + amenities[i].trim() + "'");
                    }
                }
                set.append(")");
                filter_query3.append("SELECT * FROM Filter5 WHERE LID IN (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) >= "+ amenities.length +")");
                data.view("Filter6", filter_query3.toString());
            }
            else{
                data.view("Filter6", "SELECT * FROM Filter5");
            }

            PreparedStatement s = data.get_queries("Filter6");
            listings = data.Listings_from_postal(s);

            print_table(listings);
            data.remove_view("Filter3");
            data.remove_view("Filter4");
            data.remove_view("Filter5");
            data.remove_view("Filter6");

            return listings;
        }
        return post_listing;

    }

    public static ArrayList<Listing>search_by_address() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Street: ");
        String street = scanner.nextLine();

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Country: ");
        String country = scanner.nextLine();
        ArrayList<Listing>address_listing= data.search_address(street,city,country);
        print_table(address_listing);
        data.remove_view("Filter3");
        data.remove_view("Filter4");
        data.remove_view("Filter5");
        data.remove_view("Filter6");
        ArrayList<Listing> listings;
        StringBuilder filter_query0 = new StringBuilder();
        filter_query0.append("SELECT * FROM LISTINGS NATURAL JOIN ADDRESS WHERE Status='[ACTIVE]'AND Street='"+street+"' AND City= '"+city+"'AND Country='"+country+"'");
        data.view("Filter3",filter_query0.toString());
        System.out.println("Search by Filter 1:Yes or 2:No");
        int num3 = scanner.nextInt();
        if (num3==1) {
            System.out.println("Filter by date range 1:Yes or 2:No");
            int fil1 = scanner.nextInt();
            scanner.nextLine();
            StringBuilder filter_query1 = new StringBuilder();
            if (fil1 == 1) {
                System.out.println("Enter the start date (YYYY-MM-DD)");
                String start = scanner.nextLine();
                //scanner.nextLine();
                System.out.println("Enter the end date (YYYY-MM-DD)");
                //scanner.nextLine();
                String end = scanner.nextLine();
                LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);

                filter_query1.append("SELECT D.* FROM Filter3 D JOIN Listings L ON D.LID = L.LID JOIN Calendar C ON D.LID=C.LID WHERE C.Availability='[OPEN]' AND C.Date BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY D.LID HAVING COUNT(*) >= DATEDIFF('" + endDate + "', '" + startDate + "') + 1");
                data.view("Filter4", filter_query1.toString());
                //listings = data.Listings_from_view(filter_query1.toString());
            } else {
                data.view("Filter4", "SELECT * FROM Filter3");
                // listings = data.Listings_from_view(filter_query0.toString());
            }

            System.out.println("Filter by price range 1:Yes or 2:No");
            int fil2 = scanner.nextInt();
            StringBuilder filter_query2 = new StringBuilder();
            if (fil2 == 1) {
                System.out.println("Enter the Minimum price range");
                double min = scanner.nextDouble();
                System.out.println("Enter the Maximum price range");
                double max = scanner.nextDouble();
                filter_query2.append("SELECT F.* FROM Filter4 F JOIN(SELECT L.LID FROM LISTINGS L JOIN CALENDAR C ON L.LID = C.LID GROUP BY L.LID HAVING AVG(Price) BETWEEN " + min + " AND " + max + ")AS subquery ON F.LID = subquery.LID");
                data.view("Filter5", filter_query2.toString());
            } else {
                data.view("Filter5", "SELECT * FROM Filter4");
            }
            System.out.println("Filter by Amenities 1:Yes or 2:No");
            int fil3= scanner.nextInt();
            StringBuilder filter_query3 = new StringBuilder();
            scanner.nextLine();
            if(fil3==1){
                set_of_amenities();
                System.out.println(" Enter the amenities by which you would like to filter the results");
                String string = scanner.nextLine();
                String [] amenities= string.split(",");
                StringBuilder set = new StringBuilder();
                set.append("(");
                for (int i=0; i<amenities.length; i++) {
                    if (i==0) {
                        set.append("'" + amenities[i].trim() + "'");
                    } else {
                        set.append("," + "'" + amenities[i].trim() + "'");
                    }
                }
                set.append(")");
                filter_query3.append("SELECT * FROM Filter5 WHERE LID IN (SELECT Listings.LID FROM Listings JOIN AmenitiesListing ON Listings.LID = AmenitiesListing.LID JOIN Amenities ON AmenitiesListing.Amenities_ID = Amenities.Amenities_ID WHERE Amenity_Name IN "+ set +" GROUP BY Listings.LID HAVING COUNT(*) >= "+ amenities.length +")");
                data.view("Filter6", filter_query3.toString());
            }
            else{
                data.view("Filter6", "SELECT * FROM Filter5");
            }


            PreparedStatement s = data.get_queries("Filter6");
            listings = data.Listings_from_postal(s);

            print_table(listings);
            data.remove_view("Filter3");
            data.remove_view("Filter4");
            data.remove_view("Filter5");
            data.remove_view("Filter6");

            return listings;
        }

        return address_listing;
    }

    public static void set_of_amenities(){
        System.out.println("Here are the set amenities to search from ");
        System.out.println("1.Type of place: Entire Place , Shared Room , Room ");
        System.out.println("2.Essentials : Wifi,TV,Kitchen,Washer,Free parking, Air conditioning, Dedicated workspace");
        System.out.println("3. Standout : Pool,Hot tub, Outdoor Dining area ,Beach Access, Lakes access, Gym,Prime locations, Public transport access");
        System.out.println("4. Safety: Smoke alarm, First aid kit, Fire extinguisher, CO alarm");
    }
    public static void print_table(ArrayList<Listing> List){
        System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-15s %n", "Index", "Type", "Latitude", "Longitude", "Street", "City", "Country", "Postalcode", "Status");
        int index=0;
        // Print each Listing in a table format
        for (Listing listing :List) {
            Address address = listing.address();
            System.out.printf("%-5d %-10s %-10f %-10f %-10s %-10s %-10s %-10s %-15s %n",index++, listing.type(), listing.Latitude(), listing.Longitude(), address.Street(), address.city(), address.Country(), address.postal_code(), listing.status());
        }


    }

    public static void print_reservation(ArrayList<Reservation> reservations){
        System.out.printf("%-5s %-10s %-10s %-10s %-15s %-10s %-10s %-10s %-15s %n","RID", "UID", "LID", "Price", "Availability", "start_date", "end_date", "Rating", "Comment");
        int index = 0;
        // Print each Listing in a table format
        for (Reservation rs : reservations) {
            System.out.printf("%-5d %-5d %-10d %-10d %-10f %-15s %-10s %-10s %-10d %-15s %n",index++, rs.RID(), rs.UID(), rs.LID(), rs.price(), rs.availability(), rs.start(), rs.end(), rs.rating(), rs.comment());
        }
    }

    public static void handle_reservations() throws SQLException {
        System.out.println("1:View upcoming Reservations");
        System.out.println("2:View past Reservations");
        System.out.println("3:View Canceled Reservations");
        Scanner scanner = new Scanner(System.in);
        int num = scanner.nextInt();
        if(current_user.getClass().equals(Guest.class)){
            if(num == 1){
                ArrayList<Reservation> reservations = data.Guest_reservations(current_user.uid());
                print_reservation(reservations);
                System.out.println("Do you want to CANCEL any reservation? (1:YES | 2:NO)");
                int ans = scanner.nextInt();
                if(ans == 1){
                    cancel_reservation(reservations);
                }
            }
            else if(num == 2){
                ArrayList<Reservation> past = data.Guest_past(current_user.uid());
                print_reservation(past);
            }
            else if(num == 3){
                ArrayList<Reservation> canceled = data.Guest_canceled(current_user.uid());
                print_reservation(canceled);
            }
        }
        else if(current_user.getClass().equals(Host.class)){
            if(num == 1){
                ArrayList<Reservation> reservations = data.host_reservation(current_user.uid());
                print_reservation(reservations);
                System.out.println("Do you want to CANCEL any reservation? (1:YES | 2:NO)");
                int ans = scanner.nextInt();
                if(ans == 1){
                    cancel_reservation(reservations);
                }
            }
            else if(num == 2){
                ArrayList<Reservation> past = data.host_past(current_user.uid());
                print_reservation(past);
            }
            else if(num == 3){
                ArrayList<Reservation> canceled = data.host_canceled(current_user.uid());
                print_reservation(canceled);

            }
        }

    }

    public static void Make_a_reservation(ArrayList<Listing> List) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Index Value next to the listing that you prefer");
        int num = scanner.nextInt();
        int LID=-1;
        if(num >= 0 && num < List.size()){
            LID=List.get(num).LID();
        }
        scanner.nextLine();
        System.out.println("Enter the Start date of your Reservation (YYYY-MM-DD):");
        String start = scanner.nextLine();

        System.out.println("Enter the End date of your Reservation (YYYY-MM-DD):");
        String end = scanner.nextLine();

        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("Here are the Availabilities:");
        ArrayList<Calendar>availability=data.Availabilities(LID,startDate,endDate);
        int size = availability.size();
        System.out.printf("%-5s %-10s %-10s %-10s %-10s %n", "CID", "LID", "Availability", "Price", "Date");

        // Print each Listing in a table format
        for (Calendar calendar :availability) {
            //System.out.printf("%-5d %-10s %-10f %-10f %-10s %n",calendar.CID(),calendar.LID(),calendar.availability(),calendar.Price(),calendar.date());
            System.out.printf("%-5d %-10s %-10s %-10f %-10s %n", calendar.CID(), calendar.LID(), calendar.availability(), calendar.Price(), calendar.date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        }
        while(size==0){
            System.out.println("No Availability for this range of dates");
            System.out.println("Enter the new range of dates");
            System.out.println("Enter the Start date (YYYY-MM-DD):");
            String start2 = scanner.nextLine();

            System.out.println("Enter the End date (YYYY-MM-DD):");
            String end2 = scanner.nextLine();

            LocalDate startDate2 = LocalDate.parse(start2, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate2 = LocalDate.parse(end2, DateTimeFormatter.ISO_LOCAL_DATE);
            System.out.println("Here are the Availabilities:");
            ArrayList<Calendar>availability2=data.Availabilities(LID,startDate2,endDate2);
            System.out.printf("%-5s %-10s %-10s %-10s %-10s %n", "CID", "LID", "Availability", "Price", "Date");
            for (Calendar calendar :availability2) {
                //System.out.printf("%-5d %-10s %-10f %-10f %-10s %n",calendar.CID(),calendar.LID(),calendar.availability(),calendar.Price(),calendar.date());
                System.out.printf("%-5d %-10s %-10s %-10f %-10s %n", calendar.CID(), calendar.LID(), calendar.availability(), calendar.Price(), calendar.date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            }
            size=availability2.size();
        }
        System.out.println("Would you like to make a reservation at this listing on the date range entered?(1:YES | 2:NO)");

            int num3 = scanner.nextInt();
            if (num3 == 1) {
                double total_cost = data.Total_price(startDate, endDate, LID);
                System.out.println(total_cost);
                data.Make_Reservation(current_user.uid(), LID, startDate, endDate, data.Total_price(startDate, endDate, LID));
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    data.update_day_status(LID, currentDate, "[RESERVED]");
                    currentDate = currentDate.plusDays(1);
                }
            }
        }


    public static void cancel_reservation(ArrayList<Reservation> reservations) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Index Value next to the Reservation that you want to CANCEL");
        int num = scanner.nextInt();
        if(num < 0 || num > reservations.size()){
            System.out.println("Invalid Input");
        }
        scanner.nextLine();
        Reservation rs = reservations.get(num);
        LocalDate currentDate = rs.start();
        System.out.println(rs.start());
        LocalDate endDate = rs.end();
        while (!currentDate.isAfter(endDate)) {
            data.update_day_status(rs.LID(), currentDate,"[OPEN]");
            currentDate = currentDate.plusDays(1);
        }
        data.cancel_reservation_status(rs.RID());

        System.out.println("Your Reservation is CANCELED");
    }

    public static void review_reservations() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1: Review My Reservation");
        System.out.println("2: Review My Host");
        int num = scanner.nextInt();
        if(num == 1){
            ArrayList<Reservation> past = data.Guest_past(current_user.uid());
            print_reservation(past);
            rate_reservation(past);
        }
        else if(num == 2){
            ArrayList<User> hosts = data.get_host(current_user.uid());
            print_users(hosts);
            rate_user(hosts);
        }
    }

    public static void review_guest() throws SQLException {
        ArrayList<User> guests = data.get_guest(current_user.uid());
        print_users(guests);
        rate_user(guests);
    }

    public static void rate_reservation(ArrayList<Reservation> reservations) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Index Value next to the Reservation that you want to REVIEW");
        int num = scanner.nextInt();
        if(num < 0 || num > reservations.size()){
            System.out.println("Invalid Input");
        }
        scanner.nextLine();
        Reservation rs = reservations.get(num);
        System.out.println("Enter the rating for the reservation (1-5):");
        int rating = scanner.nextInt();
        System.out.println("Enter your opinion about your stay:");
        scanner.nextLine();
        String comment = scanner.nextLine();
        data.rate_reservation(rs.RID(), comment, rating);
        System.out.println("Your Reservation is REVIEWED");
    }

    public static void rate_user(ArrayList<User> user) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int num = scanner.nextInt();
        if(num < 0 || num > user.size()){
            System.out.println("Invalid Input");
        }
        if(current_user.getClass().equals(Guest.class)){
            System.out.println("Enter the Index Value next to the HOST that you want to REVIEW");
            scanner.nextLine();
            User u = user.get(num);
            System.out.println("Enter the rating for the HOST (1-5):");
            int rating = scanner.nextInt();
            System.out.println("Comment your opinions about the HOST:");
            String comment = scanner.nextLine();
            data.add_review(current_user.uid(), u.uid(), comment, rating);
            System.out.println("Your HOST is REVIEWED");
        }
        else if(current_user.getClass().equals(Host.class)){
            System.out.println("Enter the Index Value next to the GUEST that you want to REVIEW");
            scanner.nextLine();
            User u = user.get(num);
            System.out.println("Enter the rating for the GUEST (1-5):");
            int rating = scanner.nextInt();
            System.out.println("Comment your opinions about the GUEST:");
            String comment = scanner.nextLine();
            data.add_review(current_user.uid(), u.uid(), comment, rating);
            System.out.println("Your GUEST is REVIEWED");
        }

    }

    public static void print_users(ArrayList<User> users){
        System.out.printf("%-5s %-10s %-10s %n", "Name", "Email", "Occupation");
        int index = 0;
        // Print each Listing in a table format
        for (User us : users) {
            System.out.printf("%-5d %-5d %-10d %-10d %-10f %-15s %-10s %-10s %-10d %-15s %n",index++, us.name(), us.email(), us.occupation());
        }
    }

    public static void display_reports() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1: Total number of Reservations in a specific date range by city");
        System.out.println("2: Total number of Reservations in a specific date range by postal code");
        System.out.println("3: Total number of Listings per country");
        System.out.println("4: Total number of Listings per country and city");
        System.out.println("5: Total number of Listings per country, city and postal code");
        System.out.println("6: Rank Hosts by the total number of Listings they have per county");
        System.out.println("7: Rank Hosts by the total number of Listings they have per city");
        System.out.println("8: Find the Hosts that have more than 10% of the Listings in city and country");
        System.out.println("9: Rank the Guests by the total number of Reservations in a specific date range");
        System.out.println("10: Rank the Guests by the total number of Reservations in a specific date range per city");
        System.out.println("11: Find the Hosts and Guests with the largest number of cancellations within a year");
        System.out.println("12: Report of noun phrases associated with the Listing");
        int num = scanner.nextInt();

        if(num == 1){
            System.out.println("Enter the start date (YYYY-MM-DD):");
            String start = scanner.next().trim();
            System.out.println("Enter the end date (YYYY-MM-DD):");
            String end = scanner.next().trim();
            LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
            data.report_case1(startDate, endDate);
        }
        else if(num == 2){
            System.out.println("Enter the start date (YYYY-MM-DD):");
            String start = scanner.next().trim();
            System.out.println("Enter the end date (YYYY-MM-DD):");
            String end = scanner.next().trim();
            LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
            data.report_case2(startDate, endDate);
        }
        else if(num == 3){
            data.report_case3();
        }
        else if(num == 4){
            data.report_case4();
        }
        else if(num == 5){
            data.report_case5();
        }
        else if(num == 6){
            data.report_case6();
        }
        else if(num == 7){
            data.report_case7();
        }
        else if(num == 8){
            data.report_case8();
        }
        else if(num == 9){
            System.out.println("Enter the start date (YYYY-MM-DD):");
            String start = scanner.next().trim();
            System.out.println("Enter the end date (YYYY-MM-DD):");
            String end = scanner.next().trim();
            LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
            data.report_case9(startDate, endDate);
        }
        else if(num == 10){
            System.out.println("Enter the start date (YYYY-MM-DD):");
            String start = scanner.next().trim();
            System.out.println("Enter the end date (YYYY-MM-DD):");
            String end = scanner.next().trim();
            LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE);
            data.report_case10(startDate, endDate);
        }
        else if(num == 11){
            System.out.println("Enter the year that you would like the see the report on:");
            int year = scanner.nextInt();
            data.report_case11(year);
        }
        else if(num == 12){
            report_case12();
        }

    }

    public static void report_case12() throws IOException, SQLException {
        InputStream modelFile = new FileInputStream("/Users/bahar/IdeaProjects/C43MyBnB/en-parser-chunking.bin");
        ParserModel model = new ParserModel(modelFile);
        Parser parser = ParserFactory.create(model);
        ResultSet rs = data.get_comments();
        LinkedHashMap<Integer, Integer> table = data.lid_totalReservation();
        Set<Integer> lids = table.keySet();
        for(Integer lid : lids){
            int num = table.get(lid);
            HashMap<String, Integer> noun_phrases = new HashMap<>();
            for(int i = 0; i < num; i++){
                rs.next();
                String comment = rs.getString("Comment");
                String[] sentence = comment.toLowerCase().split("[.!?]");
                for(String sentences: sentence){
                    Parse[] parse = ParserTool.parseLine(sentences, parser, 1);
                    for(Parse p : parse){
                        NounPhrases(p, noun_phrases);
                    }
                }
            }
            for(String str : noun_phrases.keySet()){
                int frq = noun_phrases.get(str);
                System.out.println(lid + " - " + str  + " : " + frq);
            }
        }
    }

    public static void NounPhrases(Parse p, HashMap<String, Integer> noun_phrases){
        if (p.getType().equals("NP")) {
            String np = p.getCoveredText();
            noun_phrases.put(np, noun_phrases.getOrDefault(np, 0) + 1);
        }
        for (Parse child : p.getChildren())
            NounPhrases(child, noun_phrases);
    }

    public static double recommend_price(int lid, String type, String city, String country, String postalcode, double lat, double lon, boolean var) throws SQLException {
        ArrayList<Amenity> amenities = data.listing_amenities(lid);
        String set =  make_string(amenities);
        double price=-1;
        price = data.listing_avg_price(set,type, country, city , amenities.size());
        if(price > 0){
            if(var){
                System.out.println("Recommended price for such Listing per night: " + price);
                helper_distance(lat,lon,city,price);
            }
            return price;
        }
        price = data.listing_avg_price(set, type, country, amenities.size());
        if(price > 0){
            if(var){
                System.out.println("Recommended price for such Listing per night: " + price);
                helper_distance(lat,lon,city,price);
            }
            return price;
        }
        price= data.listing_avg_price(set,type,country,city, postalcode, amenities.size());
        if(price > 0){
            if(var){
                System.out.println("Recommended price for such Listing per night: " + price);
                helper_distance(lat,lon,city,price);
            }
            return price;
        }

        if(price == -1){
            System.out.println("Not enough data to recommend price");
        }
     return price;

    }

    public static void helper_distance(double lat, double lon, String city, double price) throws SQLException {
        double latitude=0;
        double longitude=0;
        String attraction ="";
        if(city.equals("Toronto")){ // Cn tower
            latitude=43.6426;
            longitude= -79.3871;
            attraction="CN Tower";
        }
        if(city.equals("New York")){// Times square
            latitude=40.7580;
            longitude= -73.9855;
            attraction="Times Square";
        }
        if(city.equals("Vancouver")){// Art Gallery
            latitude=49.2827;
            longitude= -123.1207;
            attraction="Vancouver Art Gallery";

        }
        if(city.equals("San Francisco")){ // Marina District
            latitude=37.8028;
            longitude=-122.4376;
            attraction="Marina District";
        }
        double distance = data.avg_dist_from_attraction(latitude,longitude,lat,lon);
        double km=distance/1000;
        if(km<=5){
            System.out.println("Your Listing is within 5 km to "+ attraction);
            System.out.println("Your new recommended price per night: "+ (price+80.00));

        }
        else if(km>5 && km<=15){
            System.out.println("Your Listing is within (5-15 km) to "+ attraction);
            System.out.println("Your new recommended price per night: "+ (price+50.00));

        }
        else{
            System.out.println("We are unable to find a landmark close to your listing at the moment.");
        }

    }

    public static String make_string(ArrayList<Amenity> amenities){
        StringBuilder set = new StringBuilder();
        set.append("(");
        for (int i=0; i<amenities.size(); i++) {
            if (i==0) {
                set.append("'" + amenities.get(i).Amenity_Name() + "'");
            } else {
                set.append("," + "'" + amenities.get(i).Amenity_Name() + "'");
            }
        }
        set.append(")");
        return set.toString();
    }




    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Class.forName(dbClassName);
        data = new Database(Project, User, Password);
        boolean Logged_into_bnb = false; // to know if the user is logged in to MyBnB
        Scanner scanner = new Scanner(System.in);
        data.update_reservation();
        System.out.println(" Welcome to MyBnB!");
        System.out.println();

        if (!Logged_into_bnb) {
            displayMenu();
            System.out.print("Enter your choice: ");
            int num = scanner.nextInt();
            switch (num) {
                case 1:
                    Login();
                    Logged_into_bnb = true;
                    break;
                case 2:
                    Signup();
                    break;
                case 3:
                    display_reports();
                    break;
            }
        }

        if (Logged_into_bnb) {
            if (current_user.getClass() == Host.class) {
                displayHostMenu();
                System.out.print("Enter your choice: ");
                int host_num = scanner.nextInt();
                Host_options(host_num, current_user);

            } else {
                displayGuestMenu();
                System.out.print("Enter your choice: ");
                int guest_num = scanner.nextInt();
                Guest_options(guest_num, current_user);


            }
        }
    }



}

