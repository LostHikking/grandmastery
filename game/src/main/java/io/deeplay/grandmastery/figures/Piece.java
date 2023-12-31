package io.deeplay.grandmastery.figures;

import io.deeplay.grandmastery.core.Board;
import io.deeplay.grandmastery.core.GameStateChecker;
import io.deeplay.grandmastery.core.Move;
import io.deeplay.grandmastery.core.Position;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.FigureType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Piece {
  protected final Color color;
  protected FigureType figureType;
  protected List<Move> moves;
  @Setter protected boolean isMoved;

  /**
   * Конструктор для создания игровой фигуры определенного цвета.
   *
   * @param color цвет игровой фигуры.
   */
  public Piece(Color color) {
    this.color = color;
    isMoved = false;
    moves = null;
  }

  /**
   * Выполняет перемещение фигуры на доске в соответствии с переданным ходом.
   *
   * @param board доска
   * @param move ход
   * @return true, если ход был выполнен успешно и фигура перемещена, false, если ход недопустим или
   *     не удалось переместить фигуру
   */
  public boolean move(Board board, Move move) {
    if (canMove(board, move) && this.simulationMoveAndCheck(board, move)) {
      Piece piece = board.removePiece(move.from());
      board.removePiece(move.to());
      board.setPiece(move.to(), piece);
      board.clearMoves();

      isMoved = true;
      return true;
    }

    return false;
  }

  /**
   * Моделирует ход на шахматной доске и проверяет, вызывает ли этот ход шах.
   *
   * @param board Доска.
   * @param move Ход.
   * @return {@code true}, если ход не вызывает шах, иначе {@code false}.
   */
  protected boolean simulationMoveAndCheck(Board board, Move move) {
    Piece piece = board.removePiece(move.from());
    Piece removePiece = board.removePiece(move.to());
    board.setPiece(move.to(), piece);

    final boolean isCheck = GameStateChecker.isCheck(board, this.color);

    board.removePiece(move.to());
    board.setPiece(move.from(), piece);
    if (removePiece != null) {
      board.setPiece(move.to(), removePiece);
    }

    return !isCheck;
  }

  /**
   * Проверяет, может ли фигура выполнить ход на доске.
   *
   * @param board доска
   * @param move ход
   * @param withKingCheck флаг, который включает или отключает проверку взятия короля
   * @param withColorCheck флаг, для проверки хода на фигуру того же цвета
   * @return true, если фигура может выполнить указанный ход, иначе false
   */
  public abstract boolean canMove(
      Board board, Move move, boolean withKingCheck, boolean withColorCheck);

  /**
   * Проверяет, может ли фигура выполнить ход на доске.
   *
   * @param board доска
   * @param move ход
   * @return true, если фигура может выполнить указанный ход, иначе false
   */
  public boolean canMove(Board board, Move move) {
    if (moves != null && moves.contains(move)) {
      return true;
    }
    return canMove(board, move, true, true);
  }

  /**
   * Генерирует все возможные ходы для фигуры с указанной позиции на доске.
   *
   * @param board доска
   * @param position позиция фигуры на доске
   * @return {@code List<Move>} список всех возможных ходов
   */
  protected abstract List<Move> generateAllMoves(Board board, Position position);

  /**
   * Получает все возможные ходы для фигуры с указанной позиции на доске.
   *
   * @param board доска.
   * @param position позиция фигуры на доске.
   * @return {@code List<Move>} список всех возможных ходов или пустой список если позиция пуста.
   */
  @SuppressWarnings("ReferenceEquality")
  public List<Move> getAllMoves(Board board, Position position) {
    if (board.getPiece(position) != this) {
      return Collections.emptyList();
    }

    if (moves == null) {
      moves = generateAllMoves(board, position);
    }
    return moves;
  }

  public void clearMoves() {
    moves = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Piece piece)) {
      return false;
    }
    return getColor() == piece.getColor() && getFigureType() == piece.getFigureType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getColor(), getFigureType());
  }
}
