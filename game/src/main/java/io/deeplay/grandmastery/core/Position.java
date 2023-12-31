package io.deeplay.grandmastery.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.deeplay.grandmastery.domain.GameErrorCode;

/** Класс для сохранения позиции фигуры на шахматной доске. */
@JsonSerialize
public record Position(Column col, Row row) {

  /*** Метод возвращает позицию по строке.
   *
   * @param stringPos Позиция записанная в строке, например e2
   * @return Позицию на шахматной доске
   */
  public static Position fromString(String stringPos) {
    if (stringPos.length() != 2) {
      throw GameErrorCode.INCORRECT_POSITION_FORMAT.asException();
    }

    var col = Column.getColFromChar(stringPos.charAt(0));
    var row = Row.getRowFromChar(stringPos.charAt(1));

    return new Position(col, row);
  }

  public static String getString(Position position) {
    return position.col.getChar() + position.row.getChar();
  }
}
