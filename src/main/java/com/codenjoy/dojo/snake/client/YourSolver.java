package com.codenjoy.dojo.snake.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.snake.model.Elements;

import javax.xml.bind.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {
    private Dice dice;
    private Board board;
    private StatLee statlee;

    private Point prevApple;
    private Point prevBadApple;
    private int prevSize;

    public YourSolver(Dice dice) {
        this.dice = dice;
        this.statlee =  new StatLee();
        this.prevSize = 2;
        this.prevApple = new PointImpl();
        this.prevBadApple = new PointImpl();
    }

    public int invertVervical(int val, int dimY) {
        return dimY - val - 1;
    }

    ArrayList<PointLee> getOrderSnake(Board board) {
        ArrayList<PointLee> snake = new ArrayList<>();
        int sizeY = board.getField()[0].length;
        Point currentPoint = board.getHead();
        Direction currentDirection = Direction.RIGHT;

        snake.add(new PointLee(currentPoint.getX(), invertVervical(currentPoint.getY(), sizeY)));
        Elements currentElement = board.getAt(currentPoint);
        if (currentElement == Elements.HEAD_UP) {
            currentDirection = Direction.DOWN;
        } else if (currentElement == Elements.HEAD_RIGHT) {
            currentDirection = Direction.LEFT;
        } else if (currentElement == Elements.HEAD_DOWN) {
            currentDirection = Direction.UP;
        } else if (currentElement == Elements.HEAD_LEFT) {
            currentDirection = Direction.RIGHT;
        }
        currentPoint = currentPoint.copy();
        currentPoint.change(currentDirection);
        Direction prevDirection = currentDirection;
        boolean isTail = false;

        while(!isTail) {
            snake.add(new PointLee(currentPoint.getX(), invertVervical(currentPoint.getY(), sizeY)));
            currentElement = board.getAt(currentPoint);

            if (currentElement == Elements.TAIL_END_UP) {
                isTail = true;
            } else if (currentElement == Elements.TAIL_END_RIGHT) {
                isTail = true;
            } else if (currentElement == Elements.TAIL_END_DOWN) {
                isTail = true;
            } else if (currentElement == Elements.TAIL_END_LEFT) {
                isTail = true;
            } else if (currentElement == Elements.TAIL_HORIZONTAL) {
                currentDirection = prevDirection;
            } else if (currentElement == Elements.TAIL_VERTICAL) {
                currentDirection = prevDirection;
            } else if (currentElement == Elements.TAIL_LEFT_DOWN) {
                if(prevDirection == Direction.RIGHT) {
                    currentDirection = Direction.DOWN;
                } else {
                    currentDirection = Direction.LEFT;
                }
            } else if (currentElement == Elements.TAIL_LEFT_UP) {
                if(prevDirection == Direction.RIGHT) {
                    currentDirection = Direction.UP;
                } else {
                    currentDirection = Direction.LEFT;
                }
            } else if (currentElement == Elements.TAIL_RIGHT_DOWN) {
                if(prevDirection == Direction.LEFT) {
                    currentDirection = Direction.DOWN;
                } else {
                    currentDirection = Direction.RIGHT;
                }
            } else if (currentElement == Elements.TAIL_RIGHT_UP) {
                if(prevDirection == Direction.LEFT) {
                    currentDirection = Direction.UP;
                } else {
                    currentDirection = Direction.RIGHT;
                }
            } else {
                isTail = true;
            }
            currentPoint = currentPoint.copy();
            currentPoint.change(currentDirection);
            prevDirection = currentDirection;
        }

        return snake;
    }

    @Override
    public String get(Board board) {
        long l = System.currentTimeMillis();
        this.board = board;
        char[][] field = board.getField();
        int sizeX = field.length;
        int sizeY = field[0].length;

        Direction direction = board.getSnakeDirection();
        if(direction == null) direction = Direction.RIGHT;

        Point me = board.getHead();
        List<Point> snake = board.getSnake();
        int size = snake.size();
        List<Point> walls = board.getWalls();

        List<Point> apples = board.getApples();
        Point apple = null;
        if (apples.size() == 0) return direction.toString(); else apple = apples.get(0);
        //apple = new PointImpl(4, 12);

        List<Point> stones = board.getStones();
        Point badApple = null;
        if (stones.size() == 0) return direction.toString(); else badApple = stones.get(0);
        //badApple = new PointImpl(3, 12);

        System.out.println(statlee.toString());
        System.out.println(String.format("Lenght: %s\n", size));

        if (board.isGameOver()
                || size == prevSize - 1 // при наезде на себя затирает ячейку
                || walls.contains(me)
                || me.itsMe(prevBadApple) && size < 12) {
            statlee.reset(prevSize);
            return direction.toString();
        }
        prevSize = size;

        if (me.itsMe(prevApple)) {
            statlee.addApple();
        }
        prevApple = apple;

        if (me.itsMe(prevBadApple)) {
            statlee.addBadApple();
        }
        prevBadApple = badApple;

        statlee.step();
        PointLee src = new PointLee(me.getX(), invertVervical(me.getY(), sizeY));
        PointLee dst = new PointLee(apple.getX(), invertVervical(apple.getY(), sizeY));
        PointLee badDst = new PointLee(badApple.getX(), invertVervical(badApple.getY(), sizeY));

        ArrayList<PointLee> orderSnake = getOrderSnake(board);
        BoardLee boardLee = new BoardLee(sizeX, sizeY);
        snake.forEach(p -> boardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        walls.forEach(p -> boardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        stones.forEach(p -> boardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        Optional<List<PointLee>> solution = boardLee.trace(src, dst, orderSnake);

        BoardLee nextBoardLee = new BoardLee(sizeX, sizeY);
        orderSnake.forEach(p -> nextBoardLee.setObstacle(p.x(), p.y()));
        walls.forEach(p -> nextBoardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        Optional<List<PointLee>> nextSolution = nextBoardLee.trace(dst, badDst, orderSnake);

        if (solution.isPresent() && nextSolution.isPresent()) {
            List<PointLee> path = solution.get();
            PointLee p = path.stream().skip(1).findFirst().get();
            int to_x = p.x();
            int to_y = invertVervical(p.y(), sizeY);
            if (to_y < me.getY()) return Direction.DOWN.toString();
            if (to_y > me.getY()) return Direction.UP.toString();
            if (to_x < me.getX()) return Direction.LEFT.toString();
            if (to_x > me.getX()) return Direction.RIGHT.toString();
        }

        ArrayList<PointLee> badOrderSnake = getOrderSnake(board);
        BoardLee badBoardLee = new BoardLee(sizeX, sizeY);
        snake.forEach(p -> badBoardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        walls.forEach(p -> badBoardLee.setObstacle(p.getX(), invertVervical(p.getY(), sizeY)));
        Optional<List<PointLee>> badSolution = badBoardLee.trace(src, badDst, badOrderSnake);

        if (badSolution.isPresent()) {

            List<PointLee> path = badSolution.get();
            PointLee p = path.stream().skip(1).findFirst().get();
            int to_x = p.x();
            int to_y = invertVervical(p.y(), sizeY);
            if (to_y < me.getY()) return Direction.DOWN.toString();
            if (to_y > me.getY()) return Direction.UP.toString();
            if (to_x < me.getX()) return Direction.LEFT.toString();
            if (to_x > me.getX()) return Direction.RIGHT.toString();
        }

        System.out.println("Can't reach apple");
        return direction.toString();
    }

    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                "http://206.81.16.237/codenjoy-contest/board/player/bbw5gyvqhl0a6pseoxx7?code=2597044733341786045",
                new YourSolver(new RandomDice()),
                new Board());
    }

}
