import java.util.Random;

public class TestRockPaperScissors {
    public static void main(String[] args) {
        testAgainstRandom();
        testAgainstFixedPattern(RockPaperScissors.Move.PAPER, RockPaperScissors.Move.SCISSORS, RockPaperScissors.Move.ROCK);
        testAgainstFixedPattern(RockPaperScissors.Move.SCISSORS);
    }

    private static void testAgainstRandom() {
        RockPaperScissors rps = new RockPaperScissors();
        double winRate;

        winRate = testRandom(rps, 10000);
        System.out.printf("Win rate against random opponent: %.2f%%\n", winRate * 100);

        rps = null; // 释放实例
    }

    private static void testAgainstFixedPattern(RockPaperScissors.Move... pattern) {
        RockPaperScissors rps = new RockPaperScissors();
        double winRate;

        winRate = testFixedPattern(rps, pattern, 10000);
        System.out.print("Win rate against '");
        for (RockPaperScissors.Move move : pattern) {
            System.out.print(move.name());
        }
        System.out.printf("' opponent: %.2f%%\n", winRate * 100);

        rps = null; // 释放实例
    }

    private static double testRandom(RockPaperScissors rps, int numRounds) {
        Random random = new Random();
        int wins = 0;

        for (int i = 0; i < numRounds; i++) {
            RockPaperScissors.Move opponentMove = RockPaperScissors.Move.values()[random.nextInt(RockPaperScissors.Move.SIZE)];
            RockPaperScissors.Move myMove = rps.makeMove();
            rps.recordOpponentMove(opponentMove);

            if (didIWin(myMove, opponentMove)) {
                wins++;
            }
        }

        return (double) wins / numRounds;
    }

    private static double testFixedPattern(RockPaperScissors rps, RockPaperScissors.Move[] pattern, int numRounds) {
        int wins = 0;

        for (int i = 0; i < numRounds; i++) {
            RockPaperScissors.Move opponentMove = pattern[i % pattern.length];
            RockPaperScissors.Move myMove = rps.makeMove();
            rps.recordOpponentMove(opponentMove);

            if (didIWin(myMove, opponentMove)) {
                wins++;
            }
        }

        return (double) wins / numRounds;
    }

    private static boolean didIWin(RockPaperScissors.Move myMove, RockPaperScissors.Move opponentMove) {
        return (myMove == RockPaperScissors.Move.ROCK && opponentMove == RockPaperScissors.Move.SCISSORS) ||
                (myMove == RockPaperScissors.Move.PAPER && opponentMove == RockPaperScissors.Move.ROCK) ||
                (myMove == RockPaperScissors.Move.SCISSORS && opponentMove == RockPaperScissors.Move.PAPER);
    }
}
