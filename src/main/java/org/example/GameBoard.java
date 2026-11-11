package org.example;

import javax.swing.*;
import java.awt.*;

public class GameBoard {
    private JFrame frame = new JFrame("Game Board");
    private int rows = 8;
    private int cols = 8;
    private JPanel[][] squares = new JPanel[rows][cols];

    // 1. Updated constructor
    public GameBoard(String title, int rows, int cols, int width, int height, int screenX) {
        // 2 & 3. Dynamic array sizing
        this.rows = rows;
        this.cols = cols;
        this.squares = new JPanel[this.rows][this.cols];

        this.frame = new JFrame(title);
        // 5. Dynamic window size
        this.frame.setSize(width, height);
        this.frame.setLocation(screenX, 100);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 4. Dynamic grid layout
        this.frame.setLayout(new GridLayout(this.rows, this.cols));

        // 6. Dynamic loops
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                this.squares[r][c] = new JPanel();
                // The math for alternating colors works for any grid shape!
                if ((r + c) % 2 == 0) {
                    this.squares[r][c].setBackground(Color.MAGENTA);
                } else {
                    this.squares[r][c].setBackground(Color.CYAN);
                }
                this.frame.add(this.squares[r][c]);
            }
        }
    }

    public void setVisible(boolean visible) {
        this.frame.setVisible(visible);
    }

    public void updateBoard(){
        this.squares[0][0].setBackground(Color.BLACK);
    }

    public static void main(String[] args) {
        GameBoard CheckersBoard = new GameBoard("Checkers",8,8,600,600,0);
        CheckersBoard.setVisible(true);
    }
}