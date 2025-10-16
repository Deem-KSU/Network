
package network;

import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler implements Runnable {

    private Socket client;                        // Socket for this client
    private BufferedReader in;                    // Input stream from client
    private PrintWriter out;                      // Output stream to client
    private TrainManager manager;                 // Train manager reference

    // Constructor - initialize the socket and manager
    public ClientHandler(Socket client, TrainManager manager) throws IOException {
        this.client = client;
        this.manager = manager;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("Connected to Train Server! Enter commands...");

            // Wait for input from the client
            String request;
            while ((request = in.readLine()) != null) {

                if (request.equalsIgnoreCase("EXIT")) {
                    out.println("Connection closed. Goodbye!");
                    break;
                }

                // Split command by spaces
                String[] parts = request.split(" ");
                String command = parts[0].toUpperCase();

                switch (command) {

                    case "RESERVE":
                        if (parts.length < 2) {
                            out.println(" Usage: RESERVE <ticketId>"); 
                            break;
                        }
                        String ticketIdToReserve = parts[1]; 
                        if (manager.reserveTicket(ticketIdToReserve)) {
                            out.println("Ticket " + ticketIdToReserve + " reserved successfully.");
                        } else {
                            out.println("ERROR: Reservation failed. Check Ticket ID or availability.");
                        }
                        break;

                    case "FIND":
                        if (parts.length < 5) {
                            out.println("Usage: FIND <source> <destination> <day: 1-7> <class: first/economy>");
                            break;
                        }
                        String source = parts[1];
                        String destination = parts[2];
                        int day;
                        try {
                            day = Integer.parseInt(parts[3]);
                            if (day < 1 || day > 7) throw new IllegalArgumentException();
                        } catch (Exception e) {
                            out.println("ERROR: Day must be a number between 1 and 7.");
                            break;
                        }

                        String seatClass = parts[4]; 
                        ArrayList<Ticket> availableTickets = manager.findAvailableTickets(source, destination, day, seatClass);
                        
                        if (availableTickets.isEmpty()) {
                            out.println("No available tickets found for this route, day, and class.");
                        } else {
                            out.println("Available Tickets:");
                            for (Ticket t : availableTickets) {
                                out.println("- " + t.toString());
                            }
                        }
                        break;
                        
                    case "SHOW_ALL_TRAINS":
                        out.println("--- All Available Tickets ---");
                        out.println("This command is not fully implemented yet. Please use FIND.");
                        break;

                    default:
                        out.println("Unknown command. Try: RESERVE, FIND, SHOW ALL TRAINS, EXIT."); 
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Exception in client thread: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Client disconnected.");
        }
    }

}
