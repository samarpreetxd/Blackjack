
# Multiplayer Blackjack (JavaFX GUI)

## Description

**Multiplayer Blackjack (JavaFX GUI)** is a fully interactive blackjack game implemented in Java using the JavaFX framework. This project allows multiple players to join a session and play against a dealer in real-time. With a polished interface, intuitive controls, and robust multiplayer handling via a server-client architecture, players can experience a smooth, fun, and authentic blackjack gameplay experience.

## Features

- **JavaFX-Powered User Interface:**  
  - Visually appealing and easy-to-use interface.  
  - Clear display areas for dealer and each player’s hand.  
  - Buttons to perform in-game actions (Hit, Stand).

- **Multiplayer Support (Server-Client Architecture):**  
  - A server that manages gameplay logic, player connections, and card distribution.  
  - Multiple clients can join the server until the maximum player count is reached.  
  - Players see each other’s hands (with certain restrictions) and the dealer’s visible card.

- **Realistic Blackjack Rules:**  
  - Standard blackjack rules implemented, including card values, hitting, standing, and dealer logic.  
  - Automatic calculation of hand values and bust conditions.  
  - Dealer follows standard rules: hits until at least 17.

- **Dealer & Players Gameplay Flow:**  
  - Dealer’s hand is displayed at the top; players are shown at the bottom.  
  - Each player takes turns to choose actions.  
  - After all players finalize their turns, the dealer reveals the full hand and draws until the hand reaches 17 or more.  
  - Results are announced to each player individually.

- **Dynamic Deck Management:**  
  - A shuffled deck is created at the start of the game.  
  - Cards are drawn as players request hits and as the dealer takes their turn.

- **State Updates & Messaging:**  
  - The server broadcasts game states, including updated hands and dealer actions.  
  - Players receive clear messages when it’s their turn or when waiting for others.  
  - Final results are shown at the end of each round.

- **Scalable & Cross-Platform:**  
  - Runs on any OS that supports JavaFX.  
  - Easy to modify player maximum or integrate additional features.

## Getting Started

### Prerequisites
- Java 8 or later.
- JavaFX libraries (if not included by your JDK).
- A proper IDE (e.g., IntelliJ, Eclipse, or NetBeans) or command-line tools.

### Installation & Running
1. **Clone the Repository:**
   ```
   git clone https://github.com/yourusername/multiplayer-blackjack-javafx.git
   cd multiplayer-blackjack-javafx
   ```

2. **Start the Server:**
   - Run the `BlackjackServer` main class (e.g., `java application.blackjackxgui.BlackjackServer`).
   - The server starts listening on the specified port (default: 12345).

3. **Start the Client(s):**
   - Run the `Main` class for the JavaFX client.
   - A window will appear displaying dealer and player areas.
   - Multiple clients can be launched to simulate multiple players.

### Controls
- **HIT:** Request another card.
- **STAND:** End your turn without drawing further cards.

## Future Enhancements
- Add betting and chip management.
- Implement advanced options like splitting and doubling down.
- Add animations and sound effects.
- Implement a lobby system to handle multiple concurrent games.

## License
This project is distributed under the [MIT License](LICENSE).

## Credits
Developed by [Samarpreet]([url](https://github.com/samarpreetxd/)).  
Inspired by traditional casino blackjack and the JavaFX community.
