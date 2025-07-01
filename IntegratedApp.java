import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class IntegratedApp {

    // ======= AI ChatBot Classes =======
    static class ChatBot {
        private Map<String, String> faqMap;
        public ChatBot() {
            faqMap = new HashMap<>();
            loadFAQs();
        }
        private void loadFAQs() {
            faqMap.put("hi", "Hello! How can I assist you?");
            faqMap.put("hello", "Hi there! What can I do for you?");
            faqMap.put("what is your name", "I am your AI ChatBot.");
            faqMap.put("bye", "Goodbye! Have a great day.");
            faqMap.put("hotel", "We offer Standard, Deluxe, and Suite rooms.");
        }
        public String getResponse(String input) {
            input = input.toLowerCase().trim();
            for (String key : faqMap.keySet()) {
                if (input.contains(key)) {
                    return faqMap.get(key);
                }
            }
            return "I'm sorry, I didn't understand that. Please try something else.";
        }
    }

    static class ChatBotGUI {
        public static void launch() {
            ChatBot bot = new ChatBot();
            JFrame frame = new JFrame("AI ChatBot");
            JTextArea chatArea = new JTextArea();
            JTextField inputField = new JTextField();

            chatArea.setEditable(false);
            chatArea.setLineWrap(true);
            frame.setSize(400, 450);
            frame.setLayout(null);

            chatArea.setBounds(10, 10, 360, 340);
            inputField.setBounds(10, 360, 360, 30);

            frame.add(chatArea);
            frame.add(inputField);

            inputField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String userInput = inputField.getText();
                    chatArea.append("You: " + userInput + "\\n");
                    String response = bot.getResponse(userInput);
                    chatArea.append("Bot: " + response + "\\n");
                    inputField.setText("");
                }
            });

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    // ======= Hotel Reservation System Classes =======
    static class Room {
        enum RoomType { STANDARD, DELUXE, SUITE }
        private int roomNumber;
        private RoomType type;
        private boolean isBooked;
        public Room(int number, RoomType type) {
            this.roomNumber = number;
            this.type = type;
            this.isBooked = false;
        }
        public int getRoomNumber() { return roomNumber; }
        public RoomType getType() { return type; }
        public boolean isBooked() { return isBooked; }
        public void setBooked(boolean booked) { this.isBooked = booked; }
        public String toString() {
            return roomNumber + "," + type + "," + (isBooked ? "Booked" : "Available");
        }
        public static Room fromString(String line) {
            String[] parts = line.split(",");
            Room r = new Room(Integer.parseInt(parts[0]), RoomType.valueOf(parts[1]));
            r.setBooked(parts[2].equals("Booked"));
            return r;
        }
    }

    static class Booking {
        private int bookingId;
        private String customerName;
        private Room room;
        public Booking(int id, String name, Room room) {
            this.bookingId = id;
            this.customerName = name;
            this.room = room;
        }
        public int getBookingId() { return bookingId; }
        public Room getRoom() { return room; }
        public String toString() {
            return bookingId + "," + customerName + "," + room.getRoomNumber();
        }
        public static Booking fromString(String line, Room[] rooms) {
            String[] parts = line.split(",");
            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            int roomNo = Integer.parseInt(parts[2]);
            Room matched = null;
            for (Room r : rooms) {
                if (r.getRoomNumber() == roomNo) {
                    matched = r;
                    break;
                }
            }
            return new Booking(id, name, matched);
        }
    }

    static class HotelSystem {
        private List<Room> rooms = new ArrayList<>();
        private List<Booking> bookings = new ArrayList<>();
        private int nextBookingId = 1000;
        private final String ROOM_FILE = "rooms.txt";
        private final String BOOKING_FILE = "bookings.txt";

        public HotelSystem() {
            loadRooms();
            loadBookings();
        }

        private void loadRooms() {
            try {
                File file = new File(ROOM_FILE);
                if (!file.exists()) {
                    rooms.add(new Room(101, Room.RoomType.STANDARD));
                    rooms.add(new Room(102, Room.RoomType.DELUXE));
                    rooms.add(new Room(103, Room.RoomType.SUITE));
                    saveRooms();
                } else {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        rooms.add(Room.fromString(line));
                    }
                    br.close();
                }
            } catch (Exception e) {
                System.out.println("Error loading rooms: " + e.getMessage());
            }
        }

        private void saveRooms() throws IOException {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ROOM_FILE));
            for (Room r : rooms) {
                bw.write(r.toString());
                bw.newLine();
            }
            bw.close();
        }

        private void loadBookings() {
            try {
                File file = new File(BOOKING_FILE);
                if (!file.exists()) return;

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                Room[] roomArray = rooms.toArray(new Room[0]);
                while ((line = br.readLine()) != null) {
                    bookings.add(Booking.fromString(line, roomArray));
                }
                br.close();
            } catch (Exception e) {
                System.out.println("Error loading bookings: " + e.getMessage());
            }
        }

        private void saveBookings() throws IOException {
            BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE));
            for (Booking b : bookings) {
                bw.write(b.toString());
                bw.newLine();
            }
            bw.close();
        }

        public void showAvailableRooms() {
            for (Room r : rooms) {
                if (!r.isBooked()) {
                    System.out.println(r.getRoomNumber() + " - " + r.getType());
                }
            }
        }

        public void bookRoom(String name, Room.RoomType type) throws IOException {
            for (Room r : rooms) {
                if (r.getType() == type && !r.isBooked()) {
                    r.setBooked(true);
                    Booking b = new Booking(nextBookingId++, name, r);
                    bookings.add(b);
                    saveRooms();
                    saveBookings();
                    System.out.println("Booking successful! ID: " + b.getBookingId());
                    return;
                }
            }
            System.out.println("No available rooms in this category.");
        }

        public void cancelBooking(int id) throws IOException {
            Iterator<Booking> it = bookings.iterator();
            while (it.hasNext()) {
                Booking b = it.next();
                if (b.getBookingId() == id) {
                    b.getRoom().setBooked(false);
                    it.remove();
                    saveRooms();
                    saveBookings();
                    System.out.println("Booking cancelled.");
                    return;
                }
            }
            System.out.println("Booking ID not found.");
        }
    }

    // ======= Main method for menu =======
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        HotelSystem hotelSystem = new HotelSystem();

        while (true) {
            System.out.println("\n===== Integrated Application Menu =====");
            System.out.println("1. Open AI ChatBot GUI");
            System.out.println("2. Run Hotel Reservation System (Console)");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> {
                    // Launch chatbot GUI in separate thread
                    SwingUtilities.invokeLater(IntegratedApp.ChatBotGUI::launch);
                }
                case "2" -> {
                    runHotelConsole(hotelSystem, sc);
                }
                case "3" -> {
                    System.out.println("Exiting application. Goodbye!");
                    sc.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Hotel System Console UI
    private static void runHotelConsole(HotelSystem system, Scanner sc) {
        try {
            while (true) {
                System.out.println("\n--- Hotel Reservation System ---");
                System.out.println("1. View Available Rooms");
                System.out.println("2. Book Room");
                System.out.println("3. Cancel Booking");
                System.out.println("4. Back to Main Menu");
                System.out.print("Choice: ");
                String ch = sc.nextLine();

                switch (ch) {
                    case "1" -> system.showAvailableRooms();
                    case "2" -> {
                        System.out.print("Enter your name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter room type (STANDARD, DELUXE, SUITE): ");
                        String typeStr = sc.nextLine().toUpperCase();
                        try {
                            Room.RoomType type = Room.RoomType.valueOf(typeStr);
                            system.bookRoom(name, type);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid room type entered.");
                        }
                    }
                    case "3" -> {
                        System.out.print("Enter booking ID to cancel: ");
                        try {
                            int id = Integer.parseInt(sc.nextLine());
                            system.cancelBooking(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid booking ID format.");
                        }
                    }
                    case "4" -> {
                        System.out.println("Returning to main menu.");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error during hotel operations: " + e.getMessage());
        }
    }
}
