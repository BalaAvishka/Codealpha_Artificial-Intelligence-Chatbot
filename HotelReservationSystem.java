import java.io.*;
import java.util.*;

class Room implements Serializable {
    public enum RoomType { STANDARD, DELUXE, SUITE }
    private int roomNumber;
    private RoomType type;
    private boolean isBooked;

    public Room(int roomNumber, RoomType type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isBooked = false;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ") - " + (isBooked ? "Booked" : "Available");
    }
}

class Booking implements Serializable {
    private int bookingId;
    private String customerName;
    private Room room;
    private double amountPaid;

    public Booking(int bookingId, String customerName, Room room, double amountPaid) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.room = room;
        this.amountPaid = amountPaid;
    }

    public int getBookingId() { return bookingId; }
    public Room getRoom() { return room; }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId +
               "\nCustomer: " + customerName +
               "\nRoom: " + room +
               "\nAmount Paid: Rs. " + amountPaid;
    }
}

class HotelSystem {
    private List<Room> rooms;
    private List<Booking> bookings;
    private final String ROOMS_FILE = "rooms.dat";
    private final String BOOKINGS_FILE = "bookings.dat";
    private int nextBookingId = 1000;

    public HotelSystem() {
        rooms = new ArrayList<>();
        bookings = new ArrayList<>();
        loadRooms();
        loadBookings();
    }

    @SuppressWarnings("unchecked")
    private void loadRooms() {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) {
            rooms.add(new Room(101, Room.RoomType.STANDARD));
            rooms.add(new Room(102, Room.RoomType.DELUXE));
            rooms.add(new Room(103, Room.RoomType.SUITE));
            rooms.add(new Room(104, Room.RoomType.STANDARD));
            saveRooms();
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                rooms = (ArrayList<Room>) ois.readObject();
            } catch (Exception e) {
                System.out.println("Error loading rooms.");
            }
        }
    }

    private void saveRooms() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(rooms);
        } catch (IOException e) {
            System.out.println("Error saving rooms.");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadBookings() {
        File file = new File(BOOKINGS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                bookings = (ArrayList<Booking>) ois.readObject();
                if (!bookings.isEmpty()) {
                    nextBookingId = bookings.get(bookings.size() - 1).getBookingId() + 1;
                }
            } catch (Exception e) {
                System.out.println("Error loading bookings.");
            }
        }
    }

    private void saveBookings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookings);
        } catch (IOException e) {
            System.out.println("Error saving bookings.");
        }
    }

    public void showAvailableRooms() {
        boolean found = false;
        for (Room r : rooms) {
            if (!r.isBooked()) {
                System.out.println(r);
                found = true;
            }
        }
        if (!found) System.out.println("No available rooms.");
    }

    public Booking bookRoom(String customerName, Room.RoomType type) {
        for (Room r : rooms) {
            if (r.getType() == type && !r.isBooked()) {
                r.setBooked(true);
                double price = getPriceForRoom(type);
                Booking booking = new Booking(nextBookingId++, customerName, r, price);
                bookings.add(booking);
                saveRooms();
                saveBookings();
                System.out.println("Payment Successful. Booking Confirmed!");
                return booking;
            }
        }
        System.out.println("No rooms available in selected category.");
        return null;
    }

    private double getPriceForRoom(Room.RoomType type) {
        return switch (type) {
            case STANDARD -> 2000;
            case DELUXE -> 3500;
            case SUITE -> 5000;
        };
    }

    public void cancelBooking(int bookingId) {
        Iterator<Booking> it = bookings.iterator();
        while (it.hasNext()) {
            Booking b = it.next();
            if (b.getBookingId() == bookingId) {
                b.getRoom().setBooked(false);
                it.remove();
                saveRooms();
                saveBookings();
                System.out.println("Booking cancelled successfully.");
                return;
            }
        }
        System.out.println("Booking ID not found.");
    }

    public void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings yet.");
        } else {
            for (Booking b : bookings) {
                System.out.println("--------------------\n" + b);
            }
        }
    }
}

public class HotelReservationSystem {
    public static void main(String[] args) {
        HotelSystem hotel = new HotelSystem();
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to the Hotel Reservation System\n");

        while (true) {
            System.out.println("1. View Available Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. Cancel Booking");
            System.out.println("4. View Bookings");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> hotel.showAvailableRooms();
                case 2 -> {
                    System.out.print("Enter your name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter room type (STANDARD, DELUXE, SUITE): ");
                    Room.RoomType type = Room.RoomType.valueOf(sc.nextLine().toUpperCase());
                    Booking booking = hotel.bookRoom(name, type);
                    if (booking != null) System.out.println(booking);
                }
                case 3 -> {
                    System.out.print("Enter Booking ID to cancel: ");
                    int id = sc.nextInt();
                    hotel.cancelBooking(id);
                }
                case 4 -> hotel.viewBookings();
                case 5 -> {
                    System.out.println("Thank you for using our system!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}