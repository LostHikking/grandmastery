package io.deeplay.grandmastery.core;

import io.deeplay.grandmastery.domain.GameErrorCode;
import java.util.Set;

/** Класс для сохранения позиции строки фигуры на шахматной доске. */
public record Row(int value) {
  private static final Set<Character> VALID_NUMBERS =
      Set.of('1', '2', '3', '4', '5', '6', '7', '8');

  /**
   * Метод возвращает номер строки на шахматной доске.
   *
   * @param rowCharacter Номер строки в виде символа
   * @return Номер строки на шахматной доске
   */
  public static Row getRowFromChar(char rowCharacter) {
    if (!VALID_NUMBERS.contains(rowCharacter)) {
      throw GameErrorCode.INCORRECT_POSITION_FORMAT.asException();
    }

    return new Row(Integer.parseInt(String.valueOf(rowCharacter)) - 1);
  }

  public String getChar() {
    return String.valueOf(value + 1);
  }
}