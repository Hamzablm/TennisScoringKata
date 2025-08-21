package com.experiements.tennisscoringkata;

import com.experiements.tennisscoringkata.event.Event;
import com.experiements.tennisscoringkata.event.PointWon;
import com.experiements.tennisscoringkata.model.Player;
import com.experiements.tennisscoringkata.readmodel.GameProjection;

import java.util.ArrayList;
import java.util.List;

/**
 * Tennis scoring Kata implemented with an event‑sourcing approach.
 * <p>
 * Events are the single source of truth.  A *projection* rebuilds the score
 * after each event so that read‑side logic stays simple and disposable.
 * <p>
 */
public class TennisGame {

  private final List<Event> events = new ArrayList<>();        // the event log

  public void play(Player p) {
    if (fold().winner() != null) throw new IllegalStateException("Game already finished");
    PointWon evt = new PointWon(p);
    events.add(evt);
  }

  public boolean finished() {
    var projection = fold();
    return projection.winner() != null;
  }

  public String scoreHistory() {
    var projection = new GameProjection();
    var scoreHistory = new StringBuilder();
    for (var e : events) {
      projection.apply(e);
      scoreHistory.append(projection.renderLine());
    }
    return scoreHistory.toString();
  }

  private GameProjection fold() {
    var gp = new GameProjection();
    for (var e : events) gp.apply(e);
    return gp;
  }

  public TennisGame(String sequence) {
    for (char c : sequence.toCharArray()) {
      Player p = switch (c) {
        case 'A' -> Player.A;
        case 'B' -> Player.B;
        default -> throw new IllegalArgumentException("Only A or B allowed – found: " + c);
      };
      play(p);
      if (finished()) break; // ignore trailing chars once someone won
    }
  }

  public static void main(String[] args) {
    String sequence = args.length == 0 ? "ABABAA" : args[0].trim().toUpperCase();
    TennisGame game = new TennisGame(sequence);

    System.out.println(game.scoreHistory());

  }

}
