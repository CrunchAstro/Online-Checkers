package org.Checkers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class checkers {
    //game state constants
    public static final int EMPTY = 0;
    public static final int RED = 1;
    public static final int BLACK = 2;
    public static final int RED_KING = 3;
    public static final int BLACK_KING = 4;

    //2d array to keep track of board logic
    public static int[][] boardState;
    public static BoardSquare[][] visualSquares;

    //tracking variables for moving pieces
    public static int selectedRow = -1;
    public static int selectedCol = -1;

    //turn enforcement (red first)
    public static int currentTurn = RED;

    //multi jump state lock
    public static boolean isChainJumping = false;

    //visual turn display label
    public static JLabel turnLabel;
    public static JFrame mainFrame;

    public static void updateTurnDisplay() {
        if (turnLabel != null) {
            if (currentTurn == RED) {
                turnLabel.setText("RED'S TURN");
                turnLabel.setForeground(Color.RED);
                turnLabel.setBackground(Color.BLACK);
            } else {
                turnLabel.setText("BLACK'S TURN");
                turnLabel.setForeground(Color.BLACK);
                turnLabel.setBackground(Color.RED);
            }
        }
    }

    public static void main(String[] args){
        mainFrame = new JFrame("Game 1");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800,850);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        //set up the turn display labal
        turnLabel = new JLabel("RED'S TURN", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 24));
        turnLabel.setForeground(Color.RED);
        turnLabel.setBackground(Color.BLACK);
        turnLabel.setOpaque(true);
        turnLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        mainFrame.add(turnLabel, BorderLayout.NORTH);
        
        boardState = new int[8][8];
        visualSquares = new BoardSquare[8][8];

        JPanel gridPanel = new JPanel(new GridLayout(8,8));

        //initialize state logic and map visual squares
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                boolean isDarkSquare = (row + col) % 2 != 0;
                
                //set intial piece data into 2d array
                if (isDarkSquare){
                    if (row < 3) {
                        boardState[row][col] = BLACK;
                    } else if (row > 4) {
                        boardState[row][col] = RED;
                    } else {
                        boardState[row][col] = EMPTY;
                    }
                } else {
                    boardState[row][col] = EMPTY;
                }
                //add custom BoardSquare
                BoardSquare square = new BoardSquare(row, col);
                visualSquares[row][col] = square;
                gridPanel.add(square);
            }
        }

        mainFrame.add(gridPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }
}

class BoardSquare extends JPanel {
    private final int row;
    private final int col;
    private boolean isSelected = false;

    public BoardSquare(int row, int col){
        this.row = row;
        this.col = col;

        //set base square color
        if ((row + col) % 2 == 0){
            setBackground(Color.WHITE);
        } else {
            setBackground(Color.GRAY);
        }

        //click listener for selecting and moving pieces
        addMouseListener(new MouseAdapter(){
            @Override 
            public void mousePressed(MouseEvent e){
                handleSquareClick(row, col);
            }
        });
    }
    public void setSelected(boolean selected){
        this.isSelected = selected;
        repaint();
    }

    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //draw a green outline if the square is currently selected
        if (isSelected){
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
        }

        //read from array to determine what piece to paint
        int piece = checkers.boardState[row][col];
        if(piece != checkers.EMPTY){
            //calculate padding to center the piece inside the square
            int padding = 15;
            int size = Math.min(getWidth(), getHeight()) - (padding * 2);
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;  
                    
            //draw the piece shadow/border
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(x + 2, y + 2, size, size);

            //draw the main checker piece
            boolean isRed = (piece == checkers.RED || piece == checkers.RED_KING);
            g2d.setColor(isRed ? Color.RED : Color.BLACK);
            g2d.fillOval(x, y, size, size);

            if(piece == checkers.RED_KING || piece == checkers.BLACK_KING){
                g2d.setColor(Color.YELLOW);
                int crownWidth = (int) (size * 0.5);
                int crownHeight = (int) (size * 0.4);

                int cx = x + (size - crownWidth) / 2;
                int cy = y + (size - crownHeight) / 2 + 3;

                int[] xPoints = {
                    cx,                         //bottom left
                    cx,                         //left peak top
                    cx + crownWidth / 4,        //left inner valley
                    cx + crownWidth / 2,        //center peak top
                    cx + 3 * crownWidth / 4,    //right inner valley
                    cx + crownWidth,            //right peak top
                    cx + crownWidth             //bottom right
                };
                int[] yPoints = {
                    cy + crownHeight,           //bottom left base
                    cy,                         //left peak height
                    cy + crownHeight / 2,       //left valley floor
                    cy - 2,                     //center peak height
                    cy + crownHeight / 2,       //right valley floor
                    cy,                         //right peak height
                    cy + crownHeight             //bottom right base
                };
                g2d.fillPolygon(xPoints, yPoints, 7);

                g2d.setColor(piece == checkers.RED_KING ? Color.RED : Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(cx + 4, cy + crownHeight - 3, cx + crownWidth - 4, cy + crownHeight - 3);
            }
        }  
    }
    //handles core movement and click logic
    public static void handleSquareClick(int row, int col){
        int clickedPiece = checkers.boardState[row][col];

        //if in a multi jump chain, clicking the active piece finishes the turn manually
        if (checkers.isChainJumping && row == checkers.selectedRow && col == checkers.selectedCol) {
            endTurn();
            return;
        }
        //clicked on a piece belonging to active player -> select it
        if (isPlayerPiece(clickedPiece, checkers.currentTurn)){
            if (checkers.isChainJumping) {
                return;
            }
            if (checkers.selectedRow != -1 && checkers.selectedCol != -1) {
                checkers.visualSquares[checkers.selectedRow][checkers.selectedCol].setSelected(false);
            }

            checkers.selectedRow = row;
            checkers.selectedCol = col;
            checkers.visualSquares[row][col].setSelected(true);
        //clicked an empty tile when a piece is already selected -> attempt move or jump
        } else if (clickedPiece == checkers.EMPTY && checkers.selectedRow != -1 && checkers.selectedCol != -1){
            if (!checkers.isChainJumping && isValidMove(checkers.selectedRow, checkers.selectedCol, row, col)) {
                executeMove(checkers.selectedRow, checkers.selectedCol, row, col);
            } else if (isValidJump(checkers.selectedRow, checkers.selectedCol, row, col)) {
                executeJump(checkers.selectedRow, checkers.selectedCol, row, col);
            }
        }
    }
    private static boolean isPlayerPiece(int piece, int turn){
        if (turn == checkers.RED) {
            return (piece == checkers.RED || piece == checkers.RED_KING);
        }
        if (turn == checkers.BLACK) {
            return (piece == checkers.BLACK || piece == checkers.BLACK_KING);
        }
        return false;
    }
    private static boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol){
        if (toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7) {
            return false;
        }
        if (checkers.boardState[toRow][toCol] != checkers.EMPTY) {
            return false;
        }
        int piece = checkers.boardState[fromRow][fromCol];
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        //checkers can only move onto dark squares
        if ((toRow + toCol) % 2 == 0) {
            return false;
        }
        if (colDiff != 1) {
            return false;
        }
        //kings move any direction, red moves up the board (negative change), black moves down (positive change)
        if (piece == checkers.RED_KING || piece == checkers.BLACK_KING) {
            return (Math.abs(rowDiff) == 1);
        } else if (piece == checkers.RED) {
            return (rowDiff == -1);
        } else if (piece == checkers.BLACK) {
            return (rowDiff == 1);
        }
        return false;
    }
    private static boolean isValidJump(int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7) {
            return false;
        }
        if (checkers.boardState[toRow][toCol] != checkers.EMPTY) {
            return false;
        }
        int piece = checkers.boardState[fromRow][fromCol];
        if (piece == checkers.EMPTY) {
            return false;
        }
        if ((toRow + toCol) % 2 == 0) {
            return false;
        }

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        //jumps must be exactly 2 squares out diagonally
        if (colDiff != 2) {
            return false;
        }

        //validate directional movement
        if (piece == checkers.RED && rowDiff != -2) {
            return false;
        }
        if (piece == checkers.BLACK && rowDiff != 2) {
            return false;
        }
        if ((piece == checkers.RED_KING || piece == checkers.BLACK_KING) && Math.abs(rowDiff) != 2) {
            return false;
        }

        //find coordinates of square being jumped over
        int middleRow = fromRow + (rowDiff / 2);
        int middleCol = fromCol + (toCol - fromCol) / 2;
        int middlePiece = checkers.boardState[middleRow][middleCol];

        //verify the piece being jumped is an enemy
        if (piece == checkers.RED || piece == checkers.RED_KING) {
            return (middlePiece == checkers.BLACK || middlePiece == checkers.BLACK_KING);
        } else if (piece == checkers.BLACK || piece == checkers.BLACK_KING){
            return (middlePiece == checkers.RED || middlePiece == checkers.RED_KING);
        }
        return false;
    }
    private static void executeMove(int fromRow, int fromCol, int toRow, int toCol){
        int piece = checkers.boardState[fromRow][fromCol];

        //check for king promotion upon reaching board edge
        if (piece == checkers.RED && toRow == 0) {
            piece = checkers.RED_KING;
        }
        if (piece == checkers.BLACK && toRow == 7) {
            piece = checkers.BLACK_KING;
        }
        //update data structure
        checkers.boardState[toRow][toCol] = piece;
        checkers.boardState[fromRow][fromCol] = checkers.EMPTY;

        //update UI visual states
        checkers.visualSquares[fromRow][fromCol].setSelected(false);
        checkers.visualSquares[fromRow][fromCol].repaint();
        checkers.visualSquares[toRow][toCol].repaint();

        endTurn();
    }
    private static void executeJump(int fromRow, int fromCol, int toRow, int toCol) {
        int piece = checkers.boardState[fromRow][fromCol];

        //check for king promotion
        if (piece == checkers.RED && toRow == 0) {
            piece = checkers.RED_KING;
        }
        if (piece == checkers.BLACK && toRow == 7) {
            piece = checkers.BLACK_KING;
        }
            
        //move piece to destination
        checkers.boardState[toRow][toCol] = piece;
        checkers.boardState[fromRow][fromCol] = checkers.EMPTY;

        //calculate middle coordinates to delete captured piece
        int middleRow = fromRow + ((toRow - fromRow) / 2);
        int middleCol = fromCol + ((toCol - fromCol) / 2);
        checkers.boardState[middleRow][middleCol] = checkers.EMPTY;

        //refresh graphics for old space, new space, and ca[tured middle space
        checkers.visualSquares[fromRow][fromCol].setSelected(false);
        checkers.visualSquares[fromRow][fromCol].repaint();
        checkers.visualSquares[middleRow][middleCol].repaint();
        checkers.visualSquares[toRow][toCol].repaint();

        if (hasMoreJumps(toRow, toCol)) {
            checkers.isChainJumping = true;
            checkers.selectedRow = toRow;
            checkers.selectedCol = toCol;
            checkers.visualSquares[toRow][toCol].setSelected(true);
            checkers.turnLabel.setText(checkers.currentTurn == checkers.RED ? "RED CHAIN JUMP OPTION" : "BLACK CHAIN JUMP OPTION");
        } else {
            endTurn();
        }
    }
    private static boolean hasMoreJumps(int r, int c) {
        return isValidJump(r, c, r - 2, c - 2) || 
            isValidJump(r, c, r - 2, c + 2) || 
            isValidJump(r, c, r + 2, c - 2) || 
            isValidJump(r, c, r + 2, c + 2);
    }
    private static void endTurn() {
        if (checkers.selectedRow != -1 && checkers.selectedCol != -1) {
            checkers.visualSquares[checkers.selectedRow][checkers.selectedCol].setSelected(false);
            checkers.visualSquares[checkers.selectedRow][checkers.selectedCol].repaint();
        }
        checkers.selectedRow = -1;
        checkers.selectedCol = -1;
        checkers.isChainJumping = false;

        //check win conditions before altering turns, exit immediately to skip the no moves dialog
        if (checkWinCondition()) {
            return;
        }

        checkers.currentTurn = (checkers.currentTurn == checkers.RED) ? checkers.BLACK : checkers.RED;
            
        //check if upcoming player has avaialble moves
        if (!hasAvailableMoves(checkers.currentTurn)) {
            //double check if BOTH players are stuck (stalemate)
            int nextTurn = (checkers.currentTurn == checkers.RED) ? checkers.BLACK : checkers.RED;
            if (!hasAvailableMoves(nextTurn)){
                showThemedGameOverDialog("Stalenate! Game Over!");
                return;
            } else {
                //alert player of skipped turn
                showThemedSkipDialog((checkers.currentTurn == checkers.RED ? "Red" : "Black" ) + " has no moves. Skipping turn!");
                checkers.currentTurn = nextTurn; // pass turn back
            }
        }
        //update the display text and label properties dynamically
        checkers.updateTurnDisplay();
        
    }
    private static boolean hasAvailableMoves(int player) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (isPlayerPiece(checkers.boardState[r][c], player)) {
                    if (isValidMove(r, c, r - 1, c - 1) || isValidMove(r, c, r - 1, c + 1) ||
                        isValidMove(r, c, r + 1, c - 1) || isValidMove(r, c, r + 1, c + 1) ||
                        isValidJump(r, c, r - 2, c - 2) || isValidJump(r, c, r - 2, c + 2) ||
                        isValidJump(r, c, r + 2, c - 2) || isValidJump(r, c, r + 2, c + 2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
        
    //loop through board state to count remaining pieces
    private static boolean checkWinCondition() {
        int redCount = 0;
        int blackCount = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int piece = checkers.boardState[r][c];
                if (piece == checkers.RED || piece == checkers.RED_KING) {
                    redCount++;
                } 
                if (piece == checkers.BLACK || piece == checkers.BLACK_KING) {
                    blackCount++;
                }
            }
        }
        //trigger game over alert message if a player's piece total = 0
        if (redCount == 0) {
            showThemedGameOverDialog("Game Over! Black Wins!");
            return true;
        } else if (blackCount == 0) {
            showThemedGameOverDialog("Game Over! Red Wins!");
            return true;
        }
        return false;
    }
    private static void showThemedSkipDialog(String message) {
        JDialog dialog = new JDialog(checkers.mainFrame, "Turn Skipped", true);
        dialog.setSize(450, 180);
        dialog.setLocationRelativeTo(checkers.mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.YELLOW); // Distinct color alert for skips

        contentPanel.add(messageLabel);

        JButton okButton = new JButton("Continue");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(Color.DARK_GRAY);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        buttonPanel.add(okButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private static void showThemedGameOverDialog(String message) {
        JDialog dialog = new JDialog(checkers.mainFrame, "Match Finished", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(checkers.mainFrame);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.BLACK);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 22));
        messageLabel.setForeground(Color.RED);

        contentPanel.add(messageLabel);

        JButton closeButton = new JButton("Play Again");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(Color.DARK_GRAY);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        closeButton.addActionListener(e -> {
            dialog.dispose();
            resetGameBoard();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0, 15, 0));
        buttonPanel.add(closeButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private static void resetGameBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isDarkSquare = (row + col) % 2 != 0;
                if (isDarkSquare) {
                    if (row < 3) {
                        checkers.boardState[row][col] = checkers.BLACK;
                    } else if (row > 4) {
                        checkers.boardState[row][col] = checkers.RED;
                    } else {
                        checkers.boardState[row][col] = checkers.EMPTY;
                    }
                } else {
                    checkers.boardState[row][col] = checkers.EMPTY;
                }
                checkers.visualSquares[row][col].setSelected(false);
                checkers.visualSquares[row][col].repaint();
            }
        }
        checkers.currentTurn = 1;
        checkers.isChainJumping = false;
        checkers.updateTurnDisplay();
    }
}

    
