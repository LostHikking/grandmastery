package io.deeplay.grandmastery.botfarm;

import io.deeplay.grandmastery.botfarm.bots.BotFactory;
import io.deeplay.grandmastery.botfarm.utils.FarmUtils;
import io.deeplay.grandmastery.dto.CreateFarmGameRequest;
import io.deeplay.grandmastery.dto.CreateFarmGameResponse;
import io.deeplay.grandmastery.service.ConversationService;
import io.deeplay.grandmastery.utils.Boards;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreatePlayer implements Runnable {
  private final Socket socket;
  @Getter private final BufferedReader in;
  @Getter private final BufferedWriter out;

  /**
   * Конструктор.
   *
   * @param socket Сокет
   * @param in BufferedReader
   * @param out BufferedWriter
   */
  public CreatePlayer(Socket socket, BufferedReader in, BufferedWriter out) {
    this.socket = socket;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    BotFarm.PLAYERS.execute(getGame());
  }

  /**
   * Метод создаёт игру.
   *
   * @return Игра
   * @throws IllegalStateException Ошибка при создании игры
   */
  public RunGame getGame() {
    try {
      var req = ConversationService.deserialize(in.readLine(), CreateFarmGameRequest.class);
      var player = BotFactory.create(req.getName(), req.getColor());

      log.info(player.getName());
      log.info(player.getColor().name());

      var json = ConversationService.serialize(new CreateFarmGameResponse("OK"));
      FarmUtils.send(out, json);
      return new RunGame(
          player,
          new ClientPlayer(socket, in, out, player.getColor().getOpposite()),
          Boards.fromString(req.getBoard()));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}