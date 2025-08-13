
package com.example.tic_tac_toe_m.tictactoegame;

    public class Minmax {

        public static class Move {
            public int row, col;
        }

        // Symbols used by AI and human player
        public static char player = 'x';     // AI's symbol
        public static char opponent = 'o';   // Human's symbol

        // Check if there are moves left on the board
        public static boolean isMovesLeft(char[][] board) {
            for (char[] row : board) {
                for (char cell : row) {
                    if (cell == '_') {
                        return true;
                    }
                }
            }
            return false;
        }

        // Evaluate current board state
        public static int evaluate(char[][] b) {
            // Check rows
            for (int row = 0; row < 3; row++) {
                if (b[row][0] != '_' && b[row][0] == b[row][1] && b[row][1] == b[row][2]) {
                    return (b[row][0] == player) ? +10 : -10;
                }
            }

            // Check columns
            for (int col = 0; col < 3; col++) {
                if (b[0][col] != '_' && b[0][col] == b[1][col] && b[1][col] == b[2][col]) {
                    return (b[0][col] == player) ? +10 : -10;
                }
            }

            // Check diagonals
            if (b[0][0] != '_' && b[0][0] == b[1][1] && b[1][1] == b[2][2]) {
                return (b[0][0] == player) ? +10 : -10;
            }

            if (b[0][2] != '_' && b[0][2] == b[1][1] && b[1][1] == b[2][0]) {
                return (b[0][2] == player) ? +10 : -10;
            }

            // No winner
            return 0;
        }

        // Minimax algorithm with depth
        public static int minimax(char[][] board, int depth, boolean isMax) {
            int score = evaluate(board);

            // If someone has won
            if (score == 10 || score == -10) {
                return score - depth; // subtract depth to prefer faster wins / slower losses
            }

            // If it's a draw
            if (!isMovesLeft(board)) {
                return 0;
            }

            if (isMax) {
                int best = Integer.MIN_VALUE;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (board[i][j] == '_') {
                            board[i][j] = player;
                            best = Math.max(best, minimax(board, depth + 1, false));
                            board[i][j] = '_';
                        }
                    }
                }
                return best;
            } else {
                int best = Integer.MAX_VALUE;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (board[i][j] == '_') {
                            board[i][j] = opponent;
                            best = Math.min(best, minimax(board, depth + 1, true));
                            board[i][j] = '_';
                        }
                    }
                }
                return best;
            }
        }

        // Finds the best move for the AI
        public static Move findBestMove(char[][] board) {
            int bestVal = Integer.MIN_VALUE;
            Move bestMove = new Move();
            bestMove.row = -1;
            bestMove.col = -1;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == '_') {
                        board[i][j] = player;

                        int moveVal = minimax(board, 0, false);

                        board[i][j] = '_';

                        if (moveVal > bestVal) {
                            bestMove.row = i;
                            bestMove.col = j;
                            bestVal = moveVal;
                        }
                    }
                }
            }

            return bestMove;
        }
    }

        /*
    * char[][] board = {
    {'x', 'o', 'x'},
    {'_', 'o', '_'},
    {'_', '_', '_'}
};
*/
