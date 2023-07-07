import java.util.*;

public class RockPaperScissors {
    enum Move {
        ROCK, PAPER, SCISSORS;
        public static final int SIZE = Move.values().length;

        // 获取打败当前手势的手势
        public Move beat() {
            return Move.values()[(this.ordinal() + 1) % SIZE];
        }
    }

    private static final int MAX_RECENT_MOVES = 300;
    private static final double DECAY_FACTOR = 0.9;
    private static final int THRESHOLD = 3;
    private static final int MAX_SEQUENCES = 100;
    private static final int MAX_PATTERNS = 100;

    private final Map<List<Move>, Integer> patternCounts = new HashMap<>();
    private final Map<List<Move>, Double> patternPriorities = new HashMap<>();
    private final LinkedList<Move> recentMoves = new LinkedList<>();

    private final int[][][] transitionCounts = new int[Move.SIZE][Move.SIZE][Move.SIZE];
    private Move secondLastMove = null;
    private Move lastMove = null;

    private final List<Move> recentActions = new ArrayList<>();
    private final Map<List<Move>, Integer> sequences = new HashMap<>();
    private List<Move> cycle = new ArrayList<>();

    public void recordOpponentMove(Move move) {
        recentMoves.offerLast(move);
        if (recentMoves.size() > MAX_RECENT_MOVES) {
            recentMoves.pollFirst();
        }
        updatePatterns();

        if (secondLastMove != null && lastMove != null) {
            transitionCounts[secondLastMove.ordinal()][lastMove.ordinal()][move.ordinal()]++;
        }
        secondLastMove = lastMove;
        lastMove = move;

        recentActions.add(move);
        if (recentActions.size() > MAX_RECENT_MOVES) {
            recentActions.remove(0);
        }

        updateSequences(move);
        cycle = detectCycle(recentActions);
    }

    private void updateSequences(Move move) {
        for (int i = 0; i < recentMoves.size(); i++) {
            List<Move> sequence = new ArrayList<>(recentMoves.subList(i, recentMoves.size()));
            sequence.add(move);
            sequences.put(sequence, sequences.getOrDefault(sequence, 0) + 1);
        }

        cleanupMap(sequences, MAX_SEQUENCES);
    }

    private void updatePatterns() {
        for (int len = 1; len <= recentMoves.size(); len++) {
            List<Move> pattern = new ArrayList<>(recentMoves.subList(recentMoves.size() - len, recentMoves.size()));
            int count = patternCounts.getOrDefault(pattern, 0);
            patternCounts.put(pattern, count + 1);

            if (count >= THRESHOLD) {
                double priority = Math.pow(len, count) * Math.pow(DECAY_FACTOR, recentMoves.size() - len);
                patternPriorities.put(pattern, priority);
            }
        }

        cleanupMap(patternCounts, MAX_PATTERNS);
        cleanupMap(patternPriorities, MAX_PATTERNS);
    }

    private <K, V extends Comparable<V>> void cleanupMap(Map<K, V> map, int maxSize) {
        if (map.size() > maxSize) {
            List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue());
            map.clear();
            for (int i = sortedEntries.size() - maxSize; i < sortedEntries.size(); i++) {
                Map.Entry<K, V> entry = sortedEntries.get(i);
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private List<Move> detectCycle(List<Move> recentActions) {
        List<Move> bestCycle = new ArrayList<>();
        int bestScore = 0;

        for (Map.Entry<List<Move>, Integer> entry : sequences.entrySet()) {
            List<Move> sequence = entry.getKey();
            int score = entry.getValue();

            if (score >= 3 && sequence.size() > bestCycle.size()) {
                bestCycle = sequence;
                bestScore = score;
            }
        }

        return bestScore >= 2 ? bestCycle : new ArrayList<>();
    }

    public Move makeMove() {
        if (isOpponentRandom() || patternPriorities.isEmpty()) {
            return Move.values()[recentMoves.size() % Move.SIZE];
        }

        if (!cycle.isEmpty()) {
            Move predictedMove = cycle.get(0);
            return predictedMove.beat();
        }

        if (secondLastMove != null && lastMove != null) {
            int nextMoveIndex = 0;
            for (int i = 0; i < Move.SIZE; i++) {
                if (transitionCounts[secondLastMove.ordinal()][lastMove.ordinal()][i] > transitionCounts[secondLastMove.ordinal()][lastMove.ordinal()][nextMoveIndex]) {
                    nextMoveIndex = i;
                }
            }

            if (transitionCounts[secondLastMove.ordinal()][lastMove.ordinal()][nextMoveIndex] > 0) {
                return Move.values()[nextMoveIndex].beat();
            }
        }

        List<Move> bestPattern = Collections.max(patternPriorities.entrySet(), Map.Entry.comparingByValue()).getKey();
        if (bestPattern.size() == Move.SIZE) {
            Move predictedMove = bestPattern.get(recentMoves.size() % Move.SIZE);
            return predictedMove.beat();
        } else {
            Move lastMove = bestPattern.get(bestPattern.size() - 1);
            return lastMove.beat();
        }
    }
    private boolean isOpponentRandom() {
        int[] moveCounts = new int[Move.SIZE];
        for (Move move : recentMoves) {
            moveCounts[move.ordinal()]++;
        }

        double total = recentMoves.size();
        double expected = total / Move.SIZE;

        double chiSquared = 0;
        for (int count : moveCounts) {
            chiSquared += Math.pow(count - expected, 2) / expected;
        }

        return chiSquared <= 5.99;
    }

}
