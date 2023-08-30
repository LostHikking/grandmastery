package io.deeplay.grandmastery.core;

import io.deeplay.grandmastery.domain.ChessType;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.GameMode;
import io.deeplay.grandmastery.domain.GameState;
import java.io.IOException;

public interface UI {
  GameMode selectMode() throws IOException;

  ChessType selectChessType() throws IOException;

  Color selectColor() throws IOException;

  String inputPlayerName(Color color) throws IOException;

  void showMove(Move move, Color color);

  void showResultGame(GameState gameState);

  void printHelp() throws IOException;

  void showBoard(Board board, Color color);

  void incorrectMove();

  String inputMove(String playerName) throws IOException;

  boolean confirmSur() throws IOException;

  boolean answerDraw() throws IOException;

  void close();

  void addLog(String log);
}
