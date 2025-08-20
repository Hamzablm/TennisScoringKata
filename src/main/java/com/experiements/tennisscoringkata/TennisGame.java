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
  private final GameProjection projection = new GameProjection(); // read model
  // how many events are already reflected in the projection
  private int applied = 0;

  public void play(Player p) {
    PointWon evt = new PointWon(p);
    events.add(evt);
  }

  public boolean finished() {
    while (applied < events.size()) {
      projection.apply(events.get(applied));
      applied++;
    }
    if (projection.winner() != null) {
      applied = 0;// reset how many events are already reflected in the projection
      projection.reset();
      return true;
    }
    return false;
  }

  public String score() {
    StringBuilder score = new StringBuilder();
    while (applied < events.size()) {
      projection.apply(events.get(applied));
      applied++;
      score.append(projection.render());
    }
    return score.toString();
  }

  public List<Event> history() {
    return List.copyOf(events);
  }

  public static void main(String[] args) {
    String sequence = args.length == 0 ? "ABABAA" : args[0].trim().toUpperCase();
    TennisGame game = new TennisGame();

    for (char c : sequence.toCharArray()) {
      Player p = switch (c) {
        case 'A' -> Player.A;
        case 'B' -> Player.B;
        default -> throw new IllegalArgumentException("Only A or B allowed – found: " + c);
      };
      game.play(p);
      if (game.finished()) break; // ignore trailing chars once someone won
    }

    System.out.println(game.score());

  }


}
