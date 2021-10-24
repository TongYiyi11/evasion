package edu.nyu.cs.hps.evasion.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameState state;

    public enum WallCreationType {
        NONE,
        HORIZONTAL,
        VERTICAL,
        DIAGONAL,
        COUNTERDIAGONAL
    }

    public Game(int maxWalls, int wallPlacementDelay) {
        state = new GameState(maxWalls, wallPlacementDelay);
    }

    public boolean tick(WallCreationType hunterWallAction, List<Integer> hunterWallsToDelete, Point preyMovement) {
        removeWalls(hunterWallsToDelete);                      // remove walls
        Point prevHunterPos = new Point(state.hunterPosAndVel.pos);
        state.hunterPosAndVel = move(state.hunterPosAndVel);   // update hunter position
        doBuildAction(prevHunterPos, hunterWallAction);        // create walls

        if (canPreyMove()) {                                   // update prey position
            state.preyPos = move(new PositionAndVelocity(state.preyPos, preyMovement)).pos;
        }
        state.ticknum++;                         // update current time
        if (state.wallTimer > 0) {               // update remain time to build next wall
            state.wallTimer--;
        }
        return captured();
    }

    private boolean isOccupied(Point p) {
        if (p.x < 0 || p.x >= state.boardSize.x || p.y < 0 || p.y >= state.boardSize.y) {
            return true;
        }
        for (Wall wall : state.walls) {
            if (wall.occupies(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean addWall(Wall wall) {
        if (state.walls.size() < state.maxWalls && state.wallTimer <= 0) {
            state.walls.add(wall);
            state.wallTimer = state.wallPlacementDelay;
            return true;
        } else {
            // TODO 7: generate message: cannot build wall due to time limit between wall creation
            // TODO 8: generate message: cannot build wall due to max wall limit
            return false;
        }
    }

    private void removeWalls(List<Integer> indexList) {
        List<Wall> newWalls = new ArrayList<>();
        for (int i = 0; i < state.walls.size(); ++i) {
            if (!indexList.contains(i)) {
                newWalls.add(state.walls.get(i));
            }
        }
        state.walls = newWalls;
    }

    private boolean captured() {
        if (state.hunterPosAndVel.pos.distance(state.preyPos) <= 4.0) {
            List<Point> pts = BresenhamsAlgorithm.pointsBetween(state.hunterPosAndVel.pos, state.preyPos);
            for (Point pt : pts) {
                if (isOccupied(pt)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean canPreyMove() {
        return (state.ticknum % 2) != 0;
    }

    private boolean doBuildAction(Point pos, WallCreationType action) {
        // TODO 9: cannot build wall to squish H ??
        if (action == WallCreationType.HORIZONTAL) {
            Point greater = new Point(pos);
            Point lesser = new Point(pos);
            while (!isOccupied(greater)) {
                if (greater.equals(state.hunterPosAndVel.pos) || greater.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                greater.x++;
            }
            while (!isOccupied(lesser)) {
                if (lesser.equals(state.hunterPosAndVel.pos) || lesser.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                lesser.x--;
            }
            HorizontalWall horizontalWall = new HorizontalWall(pos.y, lesser.x + 1, greater.x - 1);
            return addWall(horizontalWall);
        } else if (action == WallCreationType.VERTICAL) {
            Point greater = new Point(pos);
            Point lesser = new Point(pos);
            while (!isOccupied(greater)) {
                if (greater.equals(state.hunterPosAndVel.pos) || greater.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                greater.y++;
            }
            while (!isOccupied(lesser)) {
                if (lesser.equals(state.hunterPosAndVel.pos) || lesser.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                lesser.y--;
            }
            VerticalWall verticalWall = new VerticalWall(pos.x, lesser.y + 1, greater.y - 1);
            return addWall(verticalWall);
        } else if (action == WallCreationType.DIAGONAL) {
            Point greater = new Point(pos);
            Point lesser = new Point(pos);
            int count = 0;
            int builddirection = 0;
            while (!isOccupied(greater)) {
                if (greater.equals(state.hunterPosAndVel.pos) || greater.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                if (count % 2 == 0) {
                    greater.y++;
                } else {
                    greater.x++;
                }
                count++;
            }
            if (count % 2 == 0) {
                greater.x--;
            } else {
                greater.y--;
            }
            count = 0;
            while (!isOccupied(lesser)) {
                if (lesser.equals(state.hunterPosAndVel.pos) || lesser.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                if (count % 2 == 0) {
                    lesser.x--;
                } else {
                    lesser.y--;
                }
                count++;
            }
            if (count % 2 == 0) {
                lesser.y++;
                builddirection = 0; //this means we start building by moving in the x direction
            } else {
                lesser.x++;
                builddirection = 1; //this means we start building by moving in the y direction
            }
            DiagonalWall diagonalWall = new DiagonalWall(lesser.x, greater.x, lesser.y, greater.y, builddirection);
            return addWall(diagonalWall);
        } else if (action == WallCreationType.COUNTERDIAGONAL) {
            Point greater = new Point(pos);
            Point lesser = new Point(pos);
            int count = 0;
            int builddirection = 0;
            while (!isOccupied(greater)) {
                if (greater.equals(state.hunterPosAndVel.pos) || greater.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                if (count % 2 == 0) {
                    greater.y--;
                } else {
                    greater.x++;
                }
                count++;
            }
            if (count % 2 == 0) {
                greater.x--;
            } else {
                greater.y++;
            }
            count = 0;
            while (!isOccupied(lesser)) {
                if (lesser.equals(state.hunterPosAndVel.pos) || lesser.equals(state.preyPos)) {
                    // TODO 6: generate message: cannot build wall due to touching H or P
                    return false;
                }
                if (count % 2 == 0) {
                    lesser.x--;
                } else {
                    lesser.y++;
                }
                count++;
            }
            if (count % 2 == 0) {
                lesser.y--;
                builddirection = 0; //this means we start building by moving in the x direction
            } else {
                lesser.x++;
                builddirection = 1; //this means we start building by moving in the y direction
            }
            CounterDiagonalWall counterDiagonalWall = new CounterDiagonalWall(lesser.x, greater.x, lesser.y, greater.y, builddirection);
            return addWall(counterDiagonalWall);
        }
        return false;
    }

    private PositionAndVelocity move(PositionAndVelocity posAndVel) {
        PositionAndVelocity newPosAndVel = new PositionAndVelocity(posAndVel);
        // clamp velocity between -1 and 1
        newPosAndVel.vel.x = Math.min(Math.max(newPosAndVel.vel.x, -1), 1);
        newPosAndVel.vel.y = Math.min(Math.max(newPosAndVel.vel.y, -1), 1);
        Point target = add(newPosAndVel.pos, newPosAndVel.vel);
        if (!isOccupied(target)) {
            newPosAndVel.pos = target;
        } else {    // bouncing
            if (newPosAndVel.vel.x == 0 || newPosAndVel.vel.y == 0) {
                // TODO 3: deal with diagonal, counter diagonal cases and corresponding special case 3
                if (newPosAndVel.vel.x != 0) {
                    boolean oneUp = isOccupied(add(newPosAndVel.pos, new Point(0, 1)));
                    boolean oneDown = isOccupied(add(newPosAndVel.pos, new Point(0, -1)));
                    if (oneUp && oneDown) {
                        newPosAndVel.vel.x = -newPosAndVel.vel.x;
                    } else if (oneUp) {
                        newPosAndVel.vel.x = 0;
                        newPosAndVel.vel.y = -1;
                        newPosAndVel.pos.y -= 1;
                    } else if (oneDown) {
                        newPosAndVel.vel.x = 0;
                        newPosAndVel.vel.y = 1;
                        newPosAndVel.pos.y += 1;
                    } else {
                        boolean oneUpTwoRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x * 2, 1)));
                        boolean oneDownTwoRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x * 2, -1)));
                        if ((oneUpTwoRight && oneDownTwoRight) || (!oneUpTwoRight && !oneDownTwoRight)) {
                            newPosAndVel.vel.x = -newPosAndVel.vel.x;
                        } else if (oneDownTwoRight) {
                            newPosAndVel.vel.x = 0;
                            newPosAndVel.vel.y = -1;
                            newPosAndVel.pos.y -= 1;
                        } else {
                            newPosAndVel.vel.x = 0;
                            newPosAndVel.vel.y = 1;
                            newPosAndVel.pos.y += 1;
                        }
                    }

                } else {
                    boolean oneRight = isOccupied(add(newPosAndVel.pos, new Point(1, 0)));
                    boolean oneLeft = isOccupied(add(newPosAndVel.pos, new Point(-1, 0)));
                    if (oneRight && oneLeft) {
                        newPosAndVel.vel.y = -newPosAndVel.vel.y;
                    } else if (oneRight) {
                        newPosAndVel.vel.x = -1;
                        newPosAndVel.vel.y = 0;
                        newPosAndVel.pos.x -= 1;
                    } else if (oneLeft) {
                        newPosAndVel.vel.x = 1;
                        newPosAndVel.vel.y = 0;
                        newPosAndVel.pos.y += 1;
                    } else {
                        boolean twoUpOneRight = isOccupied(add(newPosAndVel.pos, new Point(1, newPosAndVel.vel.y * 2)));
                        boolean twoUpOneLeft = isOccupied(add(newPosAndVel.pos, new Point(-1, newPosAndVel.vel.y * 2)));
                        if ((twoUpOneRight && twoUpOneLeft) || (!twoUpOneRight && !twoUpOneLeft)) {
                            newPosAndVel.vel.y = -newPosAndVel.vel.y;
                        } else if (twoUpOneLeft) {
                            newPosAndVel.vel.x = -1;
                            newPosAndVel.vel.y = 0;
                            newPosAndVel.pos.x -= 1;
                        } else {
                            newPosAndVel.vel.x = 1;
                            newPosAndVel.vel.y = 0;
                            newPosAndVel.pos.y += 1;
                        }
                    }
                }
            } else {
                boolean oneRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x, 0)));
                boolean oneUp = isOccupied(add(newPosAndVel.pos, new Point(0, newPosAndVel.vel.y)));
                if (oneRight && oneUp) {  // hit corner
                    newPosAndVel.vel.x = -newPosAndVel.vel.x;
                    newPosAndVel.vel.y = -newPosAndVel.vel.y;
                } else if (oneRight) {    // hit vertical wall
                    newPosAndVel.vel.x = -newPosAndVel.vel.x;
                    newPosAndVel.pos.y = target.y;
                } else if (oneUp) {       // hit horizontal wall
                    newPosAndVel.vel.y = -newPosAndVel.vel.y;
                    newPosAndVel.pos.x = target.x;
                } else {                  // expand search
                    boolean twoUpOneRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x, newPosAndVel.vel.y * 2)));
                    boolean oneUpTwoRight = isOccupied(add(newPosAndVel.pos, new Point(newPosAndVel.vel.x * 2, newPosAndVel.vel.y)));
                    if ((twoUpOneRight && oneUpTwoRight) || (!twoUpOneRight && !oneUpTwoRight)) {   // hit corner or diagonal/counter diagonal
                        newPosAndVel.vel.x = -newPosAndVel.vel.x;
                        newPosAndVel.vel.y = -newPosAndVel.vel.y;
                    } else if (twoUpOneRight) {   // hit vertical wall end
                        // TODO 4: deal with special case 3
                        newPosAndVel.vel.x = -newPosAndVel.vel.x;
                        newPosAndVel.pos.y = target.y;
                    } else {                      // hit horizontal wall end
                        // TODO 5: deal with special case 3
                        newPosAndVel.vel.y = -newPosAndVel.vel.y;
                        newPosAndVel.pos.x = target.x;
                    }
                }
            }
        }
        return newPosAndVel;
    }

    private static Point add(Point a, Point b) {
        return new Point(a.x + b.x, a.y + b.y);
    }

    public GameState getState() {
        return state;
    }
}