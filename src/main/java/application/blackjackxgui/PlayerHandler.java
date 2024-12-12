package application.blackjackxgui;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerHandler implements Runnable {
    private static int nextId = 1; // Static counter for unique player IDs

    private final int playerId;
    private final Socket socket;
    private final BlackjackServer server;
    private PrintWriter out;
    private BufferedReader in;
    private final List<String> hand;
    private boolean stood;
    private boolean busted;

    public PlayerHandler(Socket socket, BlackjackServer server) {
        this.playerId = nextId++;
        this.socket = socket;
        this.server = server;
        this.hand = new ArrayList<>();
        this.stood = false;
        this.busted = false;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Welcome to Multiplayer Blackjack!");

            // Synchronize with the server's current game state
            server.syncPlayerState(this);
        } catch (IOException e) {
            System.err.println("Connection error with player: " + e.getMessage());
            handleDisconnection();
        }
    }

    public void dealInitialCards() {
        hand.add(server.getDeck().drawCard());
        hand.add(server.getDeck().drawCard());
        sendMessage("Your initial hand: " + hand);
    }

    public void takeTurn() {
        try {
            while (!stood && !busted) {
                sendMessage("Your turn");
                String action = in.readLine(); // Wait for player's action

                if (action == null || action.equalsIgnoreCase("DISCONNECT")) {
                    handleDisconnection();
                    break;
                }

                if (action.equalsIgnoreCase("HIT")) {
                    String card = server.getDeck().drawCard();
                    hand.add(card);
                    sendMessage("You drew: " + card + ". Current hand: " + hand);
                    if (calculateHandValue() > 21) {
                        sendMessage("You are busted!");
                        busted = true;
                    }
                } else if (action.equalsIgnoreCase("STAND")) {
                    sendMessage("You chose to stand.");
                    stood = true;
                }

                if (!stood && !busted) {
                    sendMessage("Wait for your next action.");
                }
            }
        } catch (IOException e) {
            handleDisconnection();
        }
    }

    private void handleDisconnection() {
        stood = true; // Automatically stand if the player disconnects
        System.err.println("Player " + playerId + " disconnected: " + socket.getInetAddress());
        server.handlePlayerDisconnection(this);
    }

    public void reset() {
        hand.clear();
        stood = false;
        busted = false;
    }

    public int calculateHandValue() {
        int value = 0;
        int aces = 0;
        for (String card : hand) {
            String rank = card.split(" ")[0];
            switch (rank) {
                case "Jack":
                case "Queen":
                case "King":
                    value += 10;
                    break;
                case "Ace":
                    aces++;
                    value += 11;
                    break;
                default:
                    value += Integer.parseInt(rank);
            }
        }
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        return value;
    }

    public int getHandValue() {
        return calculateHandValue();
    }

    public boolean hasStood() {
        return stood;
    }

    public boolean isBusted() {
        return busted;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public int getPlayerId() {
        return playerId;
    }

    public List<String> getHand() {
        return hand;
    }
}
