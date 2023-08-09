package io.deeplay.grandmastery.core;

import io.deeplay.grandmastery.exceptions.GameException;
import io.deeplay.grandmastery.utils.Boards;
import io.deeplay.grandmastery.utils.LongAlgebraicNotation;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GameHistoryTest {
  private GameHistory gameHistory;
  private Board board;

  @BeforeEach
  void init() {
    gameHistory = new GameHistory();
    board = new HashBoard();
    Boards.defaultChess().accept(board);
    gameHistory.startup(board);

    var movesStr =
        "d2d4,d7d5,c2c4,d5c4,e2e4,b8c6,g1f3,g8f6,e4e5,f6d7,f1e2,e7e6,b1c3,c6b4,c3b1,b4c6";
    var moves = LongAlgebraicNotation.getMovesFromString(movesStr);

    for (Move move : moves) {
      var piece = board.getPiece(move.from());
      piece.move(board, move);
      gameHistory.addBoard(board);
      gameHistory.makeMove(move);
    }
  }

  @Test
  void setBoardTest() {
    var newGameHistory = new GameHistory();
    Assertions.assertAll(
        () -> Assertions.assertNull(newGameHistory.getCurBoard()),
        () -> Assertions.assertNotNull(gameHistory.getCurBoard()));
  }

  @Test
  @Disabled
  void getMovesWithoutTakingAndAdvancingPawnsTest() {
    Assertions.assertEquals(4, gameHistory.getMovesWithoutTakingAndAdvancingPawns());
  }

  @Test
  void getMovesWithoutTakingAndAdvancingPawnsAfterMovePawnTest() {
    var move =
        new Move(Position.getPositionFromString("b2"), Position.getPositionFromString("b3"), null);

    var piece = board.getPiece(move.from());
    piece.move(board, move);
    gameHistory.addBoard(board);
    gameHistory.makeMove(move);

    Assertions.assertEquals(0, gameHistory.getMovesWithoutTakingAndAdvancingPawns());
  }

  @Test
  void getMovesWithoutTakingAndAdvancingPawnsAfterTakingTest() {
    var moves =
        List.of(
            LongAlgebraicNotation.getMoveFromString("b1c3"),
            LongAlgebraicNotation.getMoveFromString("c6e5"));

    for (Move move : moves) {
      var piece = board.getPiece(move.from());
      piece.move(board, move);
      gameHistory.addBoard(board);
      gameHistory.makeMove(move);
    }

    Assertions.assertEquals(0, gameHistory.getMovesWithoutTakingAndAdvancingPawns());
  }

  @Test
  void getLastMoveTest() {
    var move =
        new Move(
            new Position(new Column(1), new Row(3)), new Position(new Column(2), new Row(5)), null);

    Assertions.assertAll(
        () -> Assertions.assertEquals(move, gameHistory.getLastMove()),
        () -> Assertions.assertFalse(gameHistory.isEmpty()));
  }

  @Test
  void getLastMoveFromEmptyHistoryTest() {
    var newGameHistory = new GameHistory();

    Assertions.assertAll(
        () -> Assertions.assertThrows(GameException.class, newGameHistory::getLastMove),
        () -> Assertions.assertTrue(newGameHistory.isEmpty()));
  }

  @Test
  @Disabled
  void getMaxRepeatPositionTestWithNewPosition() {
    var moves =
        List.of(
            LongAlgebraicNotation.getMoveFromString("b1c3"),
            LongAlgebraicNotation.getMoveFromString("c6e5"));

    for (Move move : moves) {
      var piece = board.getPiece(move.from());
      piece.move(board, move);
      gameHistory.addBoard(board);
      gameHistory.makeMove(move);
    }

    Assertions.assertEquals(1, gameHistory.getMaxRepeatPosition(board));
  }

  @Test
  @Disabled
  void getMaxRepeatPositionTest() {
    var move = LongAlgebraicNotation.getMoveFromString("b1c3");
    var piece = board.getPiece(move.from());
    piece.move(board, move);
    gameHistory.addBoard(board);
    gameHistory.makeMove(move);

    Assertions.assertEquals(2, gameHistory.getMaxRepeatPosition(board));
  }

  @Test
  void getMovesTest() {
    Assertions.assertEquals(16, gameHistory.getMoves().size());
  }
}