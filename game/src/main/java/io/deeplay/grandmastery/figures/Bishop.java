package io.deeplay.grandmastery.figures;

import static java.lang.Math.abs;

import io.deeplay.grandmastery.core.Board;
import io.deeplay.grandmastery.core.Column;
import io.deeplay.grandmastery.core.Move;
import io.deeplay.grandmastery.core.Position;
import io.deeplay.grandmastery.core.Row;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.FigureType;
import io.deeplay.grandmastery.utils.Figures;
import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
  /**
   * Конструктор для слона.
   *
   * @param color Цвет фигуры
   */
  public Bishop(Color color) {
    super(color);
    this.figureType = FigureType.BISHOP;
  }

  @Override
  public boolean canMove(Board board, Move move, boolean withKingCheck) {
    var toCol = move.to().col().value();
    var toRow = move.to().row().value();
    var fromRow = move.from().row().value();
    var fromCol = move.from().col().value();

    if (!Figures.basicValidMove(move, board, withKingCheck)) {
      return false;
    }
    if (abs(toCol - fromCol) == abs(toRow - fromRow)) {
      return !Figures.hasFigureOnDiagonalBetweenPositions(board, fromRow, toRow, fromCol, toCol);
    }
    return false;
  }

  @Override
  public List<Move> getAllMoves(Board board, Position position) {
    var listMove = new ArrayList<Move>();
    int[] dx = {1, -1, 1, -1};
    int[] dy = {1, -1, -1, 1};

    for (int dir = 0; dir < 4; dir++) {
      int x = position.col().value() + dx[dir];
      int y = position.row().value() + dy[dir];
      while (0 <= x && x <= 7 && 0 <= y && y <= 7) {
        listMove.add(new Move(position, new Position(new Column(x), new Row(y)), null));
        x += dx[dir];
        y += dy[dir];
      }
    }
    return listMove.stream().filter(move -> canMove(board, move)).toList();
  }
}