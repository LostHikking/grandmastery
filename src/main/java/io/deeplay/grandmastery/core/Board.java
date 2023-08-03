package io.deeplay.grandmastery.core;

import io.deeplay.grandmastery.figures.Piece;

/**
 * Абстрактный класс, представляющий игровую доску. Данный класс определяет общий интерфейс для
 * игровых досок различных типов.
 */
public abstract class Board {

  /** Последний совершенный ход на доске. */
  private Move lastMove;

  public Board() {
    lastMove = null;
  }

  /**
   * Метод возвращает последний сделанный в партии ход.
   *
   * @return последний ход
   */
  public Move getLastMove() {
    return lastMove;
  }

  /**
   * Устанавливает последний ход в партии.
   *
   * @param lastMove последний совершенный ход.
   */
  public void setLastMove(Move lastMove) {
    this.lastMove = lastMove;
  }

  /**
   * Устанавливает фигуру на заданную позицию на доске.
   *
   * @param position позиция для размещения фигуры.
   * @param piece фигура, которую необходимо разместить на доске.
   */
  public abstract void setPiece(Position position, Piece piece);

  /**
   * Возвращает фигуру на заданной позиции на доске.
   *
   * @param position позиция, для которой нужно получить фигуру.
   * @return фигура, находящаяся на заданной позиции, или {@code null}, если позиция пуста.
   */
  public abstract Piece getPiece(Position position);

  /**
   * Возвращает фигуру на заданной позиции на доске.
   *
   * @param col номер столбца на доске.
   * @param row номер строки на доске.
   * @return фигура, находящаяся на заданных координатах, или {@code null}, если позиция пуста.
   */
  public Piece getPiece(int col, int row) {
    Position position = new Position(new Column(col), new Row(row));
    return getPiece(position);
  }

  /**
   * Удаляет фигуру с указанной позиции на доске.
   *
   * @param position позиция, с которой нужно удалить фигуру.
   */
  public abstract void removePiece(Position position);

  /**
   * Возвращает позицию черного короля на доске.
   *
   * @return позиция черного короля или {@code null}, если король не найден.
   */
  public abstract Position getBlackKingPosition();

  /**
   * Возвращает позицию белого короля на доске.
   *
   * @return позиция белого короля или {@code null}, если король не найден.
   */
  public abstract Position getWhiteKingPosition();
}