package org.example;

public class GameMessage {
    public String action;
    public String username;
    public int fromRow;
    public int fromColumn;
    public int toRow;
    public int toColumn;

    public GameMessage(String action, String username, int fromRow, int fromColumn, int toRow, int toColumn) {
        this.action = action;
        this.username = username;
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.toRow = toRow;
        this.toColumn = toColumn;
    }
}
