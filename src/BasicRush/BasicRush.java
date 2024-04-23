package BasicRush;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import CustomUnitClasses.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class BasicRush extends AbstractionLayerAI {
    private UnitTypeTable unitTypeTable;
    private Player player;
    private GameState game;
    private PhysicalGameState board;

    // Unit types
    private UnitType WORKER, LIGHT, HEAVY, RANGED, BASE, BARRACKS;

    // Player units
    private List<Unit> units, bases, barracks, workers, light, heavy, ranged;

    // Enemy units
    private List<Unit> _units, _bases, _barracks, _workers, _light, _heavy, _ranged;

    // Global resources
    private List<Unit> resources;

    // Controller for all units, gets called every game tick (cycle)
    @Override
    public PlayerAction getAction(int player, GameState game) {
        setActionState(player, game);

        new Base();
        new Workers();

        return translateActions(player, game);
    }

    /*
     * Base Behavior
     */
    private class Base {
        public Base() {
            bases.forEach(base -> {
                if (base.isIdle(game))
                    assignTask(base);
            });
        }

        private void assignTask(Unit base) {
            // Train a worker if we have enough resources
            if (player.getResources() >= WORKER.cost)
                train(base, WORKER);
        }
    }

    /*
     * Worker Behavior
     */

    private class Workers {
        public Workers() {
            // Remove workers that are dead
            workers.removeIf(worker -> !units.contains(worker));

            workers.forEach(worker -> {
                if (worker.isIdle(game))
                    assignTask(worker);
            });
        }

        private void assignTask(Unit worker) {
            Unit enemy = findClosest(_units, worker);

            if (enemy == null)
                return;

            // Attack enemy units if in range
            if (worker.getAttackRange() > 0 && distance(worker, enemy) <= worker.getAttackRange()) {
                attack(worker, enemy);
                return;
            }

            // Move towards the enemy if not in range
            move(worker, enemy.getX(), enemy.getY());
        }
    }

    private Unit findClosest(List<Unit> units, Unit reference) {
        return units.stream().min(Comparator.comparingInt(u -> distance(u, reference))).orElse(null);
    }

    private int distance(Unit u1, Unit u2) {
        return Math.abs(u1.getX() - u2.getX()) + Math.abs(u1.getY() - u2.getY());
    }

    public BasicRush(UnitTypeTable unitTypeTable) {
        this(unitTypeTable, new AStarPathFinding());
    }

    public BasicRush(UnitTypeTable unitTypeTable, PathFinding pf) {
        super(pf);
        this.unitTypeTable = unitTypeTable;
        setUnitTypes();
        resetUnits();
    }

    @Override
    public void reset(UnitTypeTable unitTypeTable) {
        super.reset(unitTypeTable);
    }

    @Override
    public AI clone() {
        return new BasicRush(unitTypeTable, pf);
    }

    public void setActionState(int player, GameState game) {
        this.player = game.getPlayer(player);
        this.game = game;
        board = game.getPhysicalGameState();

        resetUnits();
        for (Unit unit : board.getUnits()) {
            if (unit.getPlayer() == player) {
                units.add(unit);
                switch (unit.getType().name) {
                    case "Base":
                        bases.add(unit);
                        break;
                    case "Barracks":
                        barracks.add(unit);
                        break;
                    case "Worker":
                        workers.add(unit);
                        break;
                    case "Light":
                        light.add(unit);
                        break;
                    case "Heavy":
                        heavy.add(unit);
                        break;
                    case "Ranged":
                        ranged.add(unit);
                        break;
                }
            } else if (unit.getPlayer() >= 0) {
                _units.add(unit);
                switch (unit.getType().name) {
                    case "Base":
                        _bases.add(unit);
                        break;
                    case "Barracks":
                        _barracks.add(unit);
                        break;
                    case "Worker":
                        _workers.add(unit);
                        break;
                    case "Light":
                        _light.add(unit);
                        break;
                    case "Heavy":
                        _heavy.add(unit);
                        break;
                    case "Ranged":
                        _ranged.add(unit);
                        break;
                }
            } else {
                resources.add(unit);
            }
        }
    }

    private void setUnitTypes() {
        for (UnitType unitType : unitTypeTable.getUnitTypes()) {
            switch (unitType.name) {
                case "Worker":
                    WORKER = unitType;
                    break;
                case "Light":
                    LIGHT = unitType;
                    break;
                case "Heavy":
                    HEAVY = unitType;
                    break;
                case "Ranged":
                    RANGED = unitType;
                    break;
                case "Base":
                    BASE = unitType;
                    break;
                case "Barracks":
                    BARRACKS = unitType;
                    break;
            }
        }
    }

    private void resetUnits() {
        units = new ArrayList<>();
        _units = new ArrayList<>();
        bases = new ArrayList<>();
        barracks = new ArrayList<>();
        workers = new ArrayList<>();
        light = new ArrayList<>();
        heavy = new ArrayList<>();
        ranged = new ArrayList<>();
        _bases = new ArrayList<>();
        _barracks = new ArrayList<>();
        _workers = new ArrayList<>();
        _light = new ArrayList<>();
        _heavy = new ArrayList<>();
        _ranged = new ArrayList<>();
        resources = new ArrayList<>();
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }
}