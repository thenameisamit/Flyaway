import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Flight {
    private String source;
    private String destination;
    private String airline;
    private double ticketPrice;

    public Flight(String source, String destination, String airline, double ticketPrice) {
        this.source = source;
        this.destination = destination;
        this.airline = airline;
        this.ticketPrice = ticketPrice;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getAirline() {
        return airline;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }
}

class Booking {
    private Flight flight;
    private String fullName;
    private String email;
    private int numberOfPersons;

    public Booking(Flight flight, String fullName, String email, int numberOfPersons) {
        this.flight = flight;
        this.fullName = fullName;
        this.email = email;
        this.numberOfPersons = numberOfPersons;
    }

    public Flight getFlight() {
        return flight;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public int getNumberOfPersons() {
        return numberOfPersons;
    }
}

class FlyAway {
    private Connection connection;

    public FlyAway(Connection connection) {
        this.connection = connection;
    }

    public List<Flight> searchFlights(String source, String destination) {
        List<Flight> availableFlights = new ArrayList<>();
        try {
            String query = "SELECT * FROM flights WHERE source = ? AND destination = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, source);
            statement.setString(2, destination);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String airline = resultSet.getString("airline");
                double ticketPrice = resultSet.getDouble("ticket_price");
                Flight flight = new Flight(source, destination, airline, ticketPrice);
                availableFlights.add(flight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableFlights;
    }

    public Booking bookFlight(Flight flight, String fullName, String email, int numberOfPersons) {
        Booking booking = new Booking(flight, fullName, email, numberOfPersons);
        return booking;
    }
}

class Admin {
    private Connection connection;

    public Admin(Connection connection) {
        this.connection = connection;
    }

    public void changePassword(String newPassword) {
        try {
            String query = "UPDATE admin SET password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlace(String place) {
        try {
            String query = "INSERT INTO places (place_name) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, place);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addAirline(String airline) {
        try {
            String query = "INSERT INTO airlines (airline_name) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, airline);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addFlight(String source, String destination, String airline, double ticketPrice) {
        try {
            String query = "INSERT INTO flights (source, destination, airline, ticket_price) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, source);
            statement.setString(2, destination);
            statement.setString(3, airline);
            statement.setDouble(4, ticketPrice);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flyaway";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            FlyAway flyAway = new FlyAway(connection);
            Admin admin = new Admin(connection);

            // User input for search
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter source: ");
            String source = scanner.nextLine();
            System.out.print("Enter destination: ");
            String destination = scanner.nextLine();

            // Search flights
            List<Flight> availableFlights = flyAway.searchFlights(source, destination);

            if (availableFlights.isEmpty()) {
                System.out.println("No flights available for the given source and destination.");
            } else {
                System.out.println("Available flights:");
                for (Flight flight : availableFlights) {
                    System.out.println("Flight: " + flight.getAirline());
                    System.out.println("Ticket Price: " + flight.getTicketPrice());
                    System.out.println("--------------------");
                }

                // User input for flight selection
                System.out.print("Enter the airline to book: ");
                String selectedAirline = scanner.nextLine();

                Flight selectedFlight = null;
                for (Flight flight : availableFlights) {
                    if (flight.getAirline().equals(selectedAirline)) {
                        selectedFlight = flight;
                        break;
                    }
                }

                if (selectedFlight == null) {
                    System.out.println("Invalid airline selected.");
                } else {
                    // User input for personal details
                    System.out.print("Enter your full name: ");
                    String fullName = scanner.nextLine();
                    System.out.print("Enter your email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter number of persons: ");
                    int numberOfPersons = scanner.nextInt();

                    // Book flight
                    Booking booking = flyAway.bookFlight(selectedFlight, fullName, email, numberOfPersons);

                    // Payment gateway and confirmation
                    System.out.println("Payment successful!");
                    System.out.println("Booking confirmed.");
                    System.out.println("Flight Details:");
                    System.out.println("Airline: " + booking.getFlight().getAirline());
                    System.out.println("Source: " + booking.getFlight().getSource());
                    System.out.println("Destination: " + booking.getFlight().getDestination());
                    System.out.println("Ticket Price: " + booking.getFlight().getTicketPrice());
                    System.out.println("Passenger Details:");
                    System.out.println("Full Name: " + booking.getFullName());
                    System.out.println("Email: " + booking.getEmail());
                    System.out.println("Number of Persons: " + booking.getNumberOfPersons());
                }
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}