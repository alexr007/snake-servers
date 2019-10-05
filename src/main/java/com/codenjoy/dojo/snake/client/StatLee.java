package com.codenjoy.dojo.snake.client;

import java.util.List;

public class StatLee {
    private int appleSteps;
    private int currentSteps;
    private long steps;
    private int currentApples;
    private int badApples;
    private int[] apples;
    private int[] stepsToApples;

    public StatLee() {
        this.apples = new int[120];
        this.stepsToApples = new int[120];
    }

    void reset(int deadLenght) {
        appleSteps = 0;
        currentSteps = 0;
        currentApples = 0;
    }

    int getCurrentSteps() {
        return currentSteps;
    }

    int getAppleSteps() {
        return appleSteps;
    }

    long getSteps() {
        return steps;
    }

    int getCurrentApples() {
        return currentApples;
    }

    int getBadApples() {
        return badApples;
    }

    void addApple() {
        apples[currentApples]++;
        stepsToApples[currentApples] += appleSteps;
        currentApples++;
        appleSteps = 0;
    }

    void addBadApple() {
        currentApples -= 10;
        badApples++;
    }

    void step() {
        steps++;
        currentSteps++;
        appleSteps++;
    }

    String applesToString() {
        String result = "";

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 30; col++) {
                if(col > 0 && col % 10 == 0) {
                    result = result + "|";
                } else if(col > 0 && col % 5 == 0) {
                    result = result + ":";
                }
                result = result + String.format("%4d ", apples[row * 30 + col]);
            }
            result = result + "\n";
        }
        return result;
    }

    String averageToString() {
        String result = "";

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 30; col++) {
                if(col > 0 && col % 10 == 0) {
                    result = result + "|";
                } else if(col > 0 && col % 5 == 0) {
                    result = result + ":";
                }

                int averageSteps;
                if(apples[row * 30 + col] == 0) {
                    averageSteps = 0;
                } else {
                    averageSteps = stepsToApples[row * 30 + col] / apples[row * 30 + col];
                }
                result = result + String.format("%4d ", averageSteps);
            }
            result = result + "\n";
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format(
            "Steps: %s, current: %s, to apple: %s. Bad apples: %s\n" +
            "Apples: \n%s" +
            "Average steps to apple: \n%s",
                getSteps(),
                getCurrentSteps(),
                getAppleSteps(),
                getBadApples(),
                applesToString(),
                averageToString()
        );
    }
}
