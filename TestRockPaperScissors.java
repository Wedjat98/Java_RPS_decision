import java.util.Random;

public class TestRockPaperScissors {
    private static final String[] MOVES = {"R", "P", "S"};

    public static void main(String[] args) {
        testAgainstRandom();
        testAgainstFixedPattern("RSP");
        testAgainstFixedPattern("S");
    }

    private static void testAgainstRandom() {
        RockPaperScissors rps = new RockPaperScissors();
        double winRate;

        winRate = testRandom(rps, 10000);
        System.out.printf("Win rate against random opponent: %.2f%%\n", winRate * 100);

        // 删除实例
        rps = null;
    }

    private static void testAgainstFixedPattern(String pattern) {
        RockPaperScissors rps = new RockPaperScissors();
        double winRate;

        winRate = testFixedPattern(rps, pattern, 10000);
        System.out.printf("Win rate against '%s' opponent: %.2f%%\n", pattern, winRate * 100);

        // 删除实例
        rps = null;
    }

    private static double testRandom(RockPaperScissors rps, int numRounds) {
        Random random = new Random();
        int wins = 0;

        for (int i = 0; i < numRounds; i++) {
            String opponentMove = MOVES[random.nextInt(MOVES.length)];
            String myMove = rps.makeMove();
            rps.recordOpponentMove(opponentMove);

            if (didIWin(myMove, opponentMove)) {
                wins++;
            }
        }

        return (double) wins / numRounds;
    }

    private static double testFixedPattern(RockPaperScissors rps, String pattern, int numRounds) {
        int wins = 0;

        for (int i = 0; i < numRounds; i++) {
            String opponentMove = pattern.charAt(i % pattern.length()) + "";
            String myMove = rps.makeMove();
            rps.recordOpponentMove(opponentMove);

            if (didIWin(myMove, opponentMove)) {
                wins++;
            }
        }

        return (double) wins / numRounds;
    }

    private static boolean didIWin(String myMove, String opponentMove) {
        return (myMove.equals("R") && opponentMove.equals("S")) ||
                (myMove.equals("P") && opponentMove.equals("R")) ||
                (myMove.equals("S") && opponentMove.equals("P"));
    }
}
