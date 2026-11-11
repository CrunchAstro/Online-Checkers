package org.example;

public class GameSession implements Runnable {
    private final ClientHandler player1;
    private final ClientHandler player2;

    //8x8 array to represent checkers board
    //0= empty, 1 = p1 piece, 2 = p2 piece
    private final int[][] boardState =  new int[8][8];

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;

        //initializeBoard();
        System.out.println("New Game Session started between two players!");
    }


    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
