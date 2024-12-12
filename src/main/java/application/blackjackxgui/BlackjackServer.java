package application.blackjackxgui;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BlackjackServer {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final int MAX_PLAYERS = 3;
    private final List<PlayerHandler> players = Collections.synchronizedList(new ArrayList<>());
    private Deck deck;
    private List<String> dealerHand;
    private boolean gameInProgress;

    public BlackjackServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);
        gameInProgress = false;
        System.out.println("Blackjack server started on port " + port);
    }

    public void run() {
        try {
            while (true) {
                // Accept incoming connections
                Socket clientSocket = serverSocket.accept();
                synchronized (players) {
                    if (players.size() < MAX_PLAYERS) {
                        PlayerHandler player = new PlayerHandler(clientSocket, this);
                        players.add(player);
                        threadPool.execute(player);
                        System.out.println("Player connected: " + clientSocket.getInetAddress());
                    } else {
                        rejectConnection(clientSocket);
                    }

                    // Check if a new game can start
                    if (players.size() == MAX_PLAYERS && !gameInProgress) {
                        System.out.println("All players connected. Starting game...");
                        startGame();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error accepting client connection: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void rejectConnection(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println("INFO|Game in progress. Please wait for the next round.");
        } catch (IOException e) {
            System.err.println("Error rejecting connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void startGame() {
        synchronized (this) {
            gameInProgress = true;
        }

        deck = new Deck();
        dealerHand = new ArrayList<>();
        deck.shuffle();

        System.out.println("Game started. Dealing cards...");

        synchronized (players) {
            for (PlayerHandler player : players) {
                player.dealInitialCards();
            }
        }
        dealInitialDealerCards();

        broadcastGameState();

        playTurns();

        playDealerTurn();

        announceResults();

        resetGameState();

        synchronized (this) {
            gameInProgress = false;
        }

        // Check if there are still enough players to restart the game
        synchronized (players) {
            if (players.size() == MAX_PLAYERS) {
                System.out.println("Restarting game...");
                startGame();
            } else {
                System.out.println("Waiting for more players...");
            }
        }
    }

    private void dealInitialDealerCards() {
        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());
        broadcast("STATE|Dealer's visible card: " + dealerHand.get(0));
    }

    private void playTurns() {
        for (int i = 0; i < players.size(); i++) {
            PlayerHandler player = players.get(i);

            if (!player.isBusted() && !player.hasStood()) {
                broadcast("INFO|Player " + (i + 1) + "'s turn.");
                try {
                    player.takeTurn();
                } catch (Exception e) {
                    System.err.println("Error with player " + (i + 1) + ": " + e.getMessage());
                    player.sendMessage("INFO|Disconnected. You are standing by default.");
                    player.reset();
                }
                broadcastGameState();
            }
        }
    }

    private void playDealerTurn() {
        int dealerValue = calculateHandValue(dealerHand);
        broadcast("INFO|Dealer's turn...");
        while (dealerValue < 17) {
            String card = deck.drawCard();
            dealerHand.add(card);
            broadcast("STATE|Dealer drew: " + card);
            dealerValue = calculateHandValue(dealerHand);
        }
        broadcast("STATE|Dealer's final hand: " + dealerHand + " (Value: " + dealerValue + ")");
    }

    private void announceResults() {
        int dealerValue = calculateHandValue(dealerHand);
        synchronized (players) {
            for (int i = 0; i < players.size(); i++) {
                PlayerHandler player = players.get(i);
                if (player.isBusted()) {
                    player.sendMessage("RESULT|You lost! You're busted.");
                } else if (dealerValue > 21 || player.getHandValue() > dealerValue) {
                    player.sendMessage("RESULT|Congratulations! You win!");
                } else if (player.getHandValue() == dealerValue) {
                    player.sendMessage("RESULT|It's a tie!");
                } else {
                    player.sendMessage("RESULT|You lost. Dealer wins.");
                }
            }
        }
    }

    private int calculateHandValue(List<String> hand) {
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

    private void broadcastGameState() {
        synchronized (players) {
            if (dealerHand == null || dealerHand.isEmpty()) return; // Prevent null/empty access
            for (int i = 0; i < players.size(); i++) {
                PlayerHandler player = players.get(i);
                player.sendMessage("STATE|Dealer's Hand: " + dealerHand.get(0) + " (visible card)");
                player.sendMessage("STATE|Your Hand: " + String.join(", ", player.getHand()));

                // Send other players' hands
                for (int j = 0; j < players.size(); j++) {
                    if (i != j) {
                        player.sendMessage("STATE|Player " + (j + 1) + "'s Hand: " + String.join(", ", players.get(j).getHand()));
                    }
                }
            }
        }
    }

    public void handlePlayerDisconnection(PlayerHandler player) {
        synchronized (players) {
            players.remove(player);
            System.out.println("Player " + player.getPlayerId() + " removed from the game.");

            // Check if the game should end due to insufficient players
            if (players.size() < 2) {
                System.out.println("Not enough players to continue. Ending game.");
                gameInProgress = false;
                broadcast("INFO|Game ended due to insufficient players. Waiting for new players...");
                resetGameState();
            } else {
                // Broadcast updated game state to remaining players
                broadcast("INFO|Player " + player.getPlayerId() + " has disconnected.");
                broadcastGameState();
            }
        }
    }

    public void syncPlayerState(PlayerHandler player) {
        if (dealerHand == null || dealerHand.isEmpty()) {
            player.sendMessage("INFO|Game not started yet. Please wait.");
            return;
        }
        player.sendMessage("STATE|Dealer's Hand: " + dealerHand.get(0) + " (visible card)");
        for (int i = 0; i < players.size(); i++) {
            player.sendMessage("STATE|Player " + (i + 1) + "'s Hand: " + String.join(", ", players.get(i).getHand()));
        }
    }

    public void broadcast(String message) {
        synchronized (players) {
            for (PlayerHandler player : players) {
                player.sendMessage(message);
            }
        }
    }

    private void resetGameState() {
        synchronized (players) {
            for (PlayerHandler player : players) {
                player.reset();
            }
        }
        deck = null; // Clear the deck for the next game
        dealerHand = null; // Clear the dealer's hand
        System.out.println("Game state reset. Waiting for players...");
    }

    public Deck getDeck() {
        return deck;
    }

    private void shutdown() {
        try {
            serverSocket.close();
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            BlackjackServer server = new BlackjackServer(12345);
            server.run();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
