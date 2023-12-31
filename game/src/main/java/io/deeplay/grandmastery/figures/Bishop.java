package io.deeplay.grandmastery.figures;

import static java.lang.Math.abs;

import io.deeplay.grandmastery.core.Board;
import io.deeplay.grandmastery.core.Move;
import io.deeplay.grandmastery.core.Position;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.FigureType;
import io.deeplay.grandmastery.utils.Figures;
import java.util.List;

public class Bishop extends Piece {
  /**
   * Конструктор для слона.
   *
   * @param color Цвет фигуры
   */
  public Bishop(Color color) {
    super(color);
    figureType = FigureType.BISHOP;
  }

  @Override
  public boolean canMove(Board board, Move move, boolean withKingCheck, boolean withColorCheck) {
    var toCol = move.to().col().value();
    var toRow = move.to().row().value();
    var fromRow = move.from().row().value();
    var fromCol = move.from().col().value();

    if (!Figures.basicValidMove(move, board, withKingCheck, withColorCheck)) {
      return false;
    }
    if (abs(toCol - fromCol) == abs(toRow - fromRow)) {
      return Figures.hasNoFigureOnDiagonalBetweenPositions(board, fromRow, toRow, fromCol, toCol);
    }
    return false;
  }

  @Override
  protected List<Move> generateAllMoves(Board board, Position position) {
    return Figures.allDiagonalMoves(position).stream()
        .filter(move -> canMove(board, move) && simulationMoveAndCheck(board, move))
        .toList();
  }
}
