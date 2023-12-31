package io.deeplay.grandmastery;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.tools.javac.Main;
import io.deeplay.grandmastery.core.Board;
import io.deeplay.grandmastery.core.Move;
import io.deeplay.grandmastery.core.Player;
import io.deeplay.grandmastery.domain.ChessType;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.dto.AcceptMove;
import io.deeplay.grandmastery.dto.CreateFarmGameRequest;
import io.deeplay.grandmastery.dto.CreateFarmGameResponse;
import io.deeplay.grandmastery.dto.IDto;
import io.deeplay.grandmastery.dto.SendMove;
import io.deeplay.grandmastery.dto.WaitMove;
import io.deeplay.grandmastery.exceptions.GameException;
import io.deeplay.grandmastery.exceptions.QueryException;
import io.deeplay.grandmastery.service.ConversationService;
import io.deeplay.grandmastery.utils.Boards;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class FarmPlayer extends Player {
  private final String host;
  private final int port;

  private Socket socket;
  private BufferedReader in;
  private BufferedWriter out;
  private final ChessType chessType;

  /**
   * Конструктор с параметрами.
   *
   * @param name Имя
   * @param color Цвет
   * @param chessType Тип шахмат
   * @throws IllegalStateException Ошибка создания FarmPlayer
   * @throws IOException ошибка при чтении конфиг файла, для подключения к бот-ферме.
   */
  public FarmPlayer(String name, Color color, ChessType chessType) throws IOException {
    super(name, color);
    this.chessType = chessType;

    try (InputStream config =
        Main.class.getClassLoader().getResourceAsStream("config.properties")) {
      Properties properties = new Properties();
      properties.load(config);

      host = properties.getProperty("bot_farm_host");
      port = Integer.parseInt(properties.getProperty("bot_farm_port"));
    }

    log.info("Создали FarmPlayer - " + name);
  }

  /**
   * Метод инициализирует FarmPlayer.
   *
   * @param board Доска
   * @throws QueryException Ошибка при отправке/получении данных
   * @throws IllegalStateException Ошибка при создании бота на фабрике
   */
  public void init(Board board) throws QueryException {
    try {
      socket = new Socket(host, port);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
    } catch (IOException e) {
      log.error("Ошибка при создании игрока - " + name);
      throw new IllegalStateException(e);
    }

    var status =
        query(
                new CreateFarmGameRequest(name, color, chessType, Boards.getString(board)),
                CreateFarmGameResponse.class)
            .getStatus();
    if (!"OK".equals(status)) {
      throw new IllegalStateException("Bot not found");
    }
  }

  @Override
  public Move createMove() throws GameException {
    try {
      if (gameHistory.isEmpty()) {
        var json = ConversationService.serialize(new WaitMove());
        send(json);

        var response = in.readLine();
        log.info("Получили данные - " + response);
        return ConversationService.deserialize(response, SendMove.class).getMove();
      }

      var dto = new AcceptMove(this.gameHistory.getLastMove());
      var json = ConversationService.serialize(dto);
      send(json);

      var dto2 = new WaitMove();

      return query(dto2, SendMove.class).getMove();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean answerDraw() throws GameException {
    return false;
  }

  /**
   * Функция отправляет запрос на сервер и возвращает ответ от него.
   *
   * @param dto Request Dto
   * @return Response Dto
   * @throws QueryException Ошибка выполнения запроса
   */
  public <T extends IDto> T query(IDto dto, Class<T> clazz) throws QueryException {
    try {
      var json = ConversationService.serialize(dto);
      send(json);

      var response = in.readLine();
      log.info("Получили данные - " + response);

      return ConversationService.deserialize(response, clazz);
    } catch (IOException e) {
      throw new QueryException(e.getMessage());
    }
  }

  /**
   * Метод отправляет данные в BufferedWriter.
   *
   * @param json Данные
   * @throws IOException Ошибка при отправке данных
   */
  public void send(String json) throws IOException {
    log.info("Отправили данные - " + json);
    out.write(json);
    out.newLine();
    out.flush();
  }
}
