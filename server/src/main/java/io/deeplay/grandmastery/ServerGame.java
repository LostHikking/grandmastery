package io.deeplay.grandmastery;

import io.deeplay.grandmastery.core.GameController;
import io.deeplay.grandmastery.core.Player;
import io.deeplay.grandmastery.core.PlayerInfo;
import io.deeplay.grandmastery.domain.ChessType;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.GameState;
import io.deeplay.grandmastery.exceptions.GameException;
import io.deeplay.grandmastery.exceptions.QueryException;
import java.io.IOException;
import java.net.Socket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ServerGame implements Runnable {
  private final ServerController serverController;
  private final GameController gameController;

  /**
   * Конструктор для объектов типа ServerGame.
   *
   * @throws RuntimeException Ошибка инициализации игрока/игроков
   */
  public ServerGame(Player playerOne, Player playerTwo, ChessType chessType, Socket socket) {
    this.serverController = new ServerController(new ServerDao(playerOne, playerTwo, socket));

    gameController = new GameController(playerOne, playerTwo);
    gameController.beginPlay(chessType);
    try {
      if (playerOne instanceof FarmPlayer farmPlayer) {
        farmPlayer.init(gameController.getBoard());
      }
      if (playerTwo instanceof FarmPlayer farmPlayer) {
        farmPlayer.init(gameController.getBoard());
      }
    } catch (QueryException e) {
      throw new RuntimeException(e);
    }

    log.info("Игра создана");
  }

  @Override
  public void run() {
    try {
      serverController.notifyStartGame(gameController.getBoard());
      log.info("Игра запущенна");

      while (!gameController.isGameOver()) {
        try {
          PlayerInfo currentPlayer = gameController.getCurrentPlayer();
          gameController.nextMove();
          if (gameController.isGameOver()) {
            if (!gameController.isSurrender() && !gameController.isDrawWithOffer()) {
              serverController
                  .serverDao()
                  .notifySuccessMove(
                      currentPlayer.getColor(),
                      gameController.getGameHistory().getLastMove(),
                      gameController.getBoard());
            }

            break;
          }

          if (currentPlayer != gameController.getCurrentPlayer()) {
            var color = gameController.getOpponentPlayer().getColor();
            serverController.notifySuccessMove(
                color, gameController.getGameHistory().getLastMove(), gameController.getBoard());

            log.info("Сделан ход цветом - " + color);
          }
        } catch (GameException e) {
          var color = gameController.getCurrentPlayer().getColor();
          serverController.notifyWrongMove(color);

          log.error("Некорректный ход цветом - " + color);
        }
      }

      log.info("Игра закончена");

      serverController.sendResult(
          gameController.getGameStatus(), gameController.getGameHistory().getBoards());
      serverController.close();
    } catch (IOException | ServerException e) {
      var color = gameController.getCurrentPlayer().getColor();
      var gameStatus =
          color == Color.WHITE
              ? GameState.TECHNICAL_DEFEAT_WHITE
              : GameState.TECHNICAL_DEFEAT_BLACK;
      try {
        serverController.sendResult(gameStatus, gameController.getGameHistory().getBoards());
      } catch (IOException ex) {
        log.error("Ошибка при отправке результатов игры клиентам");
        throw new RuntimeException(ex);
      }
    }
  }
}
