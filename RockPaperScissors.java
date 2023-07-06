import java.util.*;

public class RockPaperScissors {
    private static final int MAX_RECENT_MOVES = 300;
    private static final double DECAY_FACTOR = 0.9;
    private static final int THRESHOLD = 3;

    // 存储模式和它们出现的次数
    private final Map<String, Integer> patternCounts = new HashMap<>();

    // 存储模式和它们的优先级
    private final Map<String, Double> patternPriorities = new HashMap<>();

    // 对手的最近出招
    private String recentMoves = "";

    // 添加一个新的成员变量来存储每个转移的次数
    private int[][][] transitionCounts = new int[3][3][3];

    private int secondLastMove = -1;
    private int lastMove = -1;
    private static final int CYCLE_BUFFER_SIZE = 10;
    // 用于存储最近的动作
    private List<String> recentActions = new ArrayList<>();
    // 当前识别到的周期
    private String cycle = "";

    public void recordOpponentMove(String move) {
        recentMoves += move;
        if (recentMoves.length() > MAX_RECENT_MOVES) {
            recentMoves = recentMoves.substring(recentMoves.length() - MAX_RECENT_MOVES);
        }
        updatePatterns();

        // 更新转移次数
        int moveIndex = "RPS".indexOf(move);
        if (secondLastMove != -1 && lastMove != -1) {
            transitionCounts[secondLastMove][lastMove][moveIndex]++;
        }
        secondLastMove = lastMove;
        lastMove = moveIndex;
        recentActions.add(move);
        if (recentActions.size() > CYCLE_BUFFER_SIZE) {
            recentActions.remove(0);
        }

        // 检查是否有周期性模式
        cycle = detectCycle(recentActions);

    }

    private String detectCycle(List<String> actions) {
        for (int cycleLength = 1; cycleLength <= actions.size() / 2; cycleLength++) {
            boolean hasCycle = true;
            for (int i = 0; i < cycleLength; i++) {
                if (!actions.get(actions.size() - 1 - i).equals(actions.get(actions.size() - 1 - cycleLength - i))) {
                    hasCycle = false;
                    break;
                }
            }
            if (hasCycle) {
                return String.join("", actions.subList(actions.size() - cycleLength, actions.size()));
            }
        }
        return "";
    }
    private void updatePatterns() {
        for (int len = 1; len <= recentMoves.length(); len++) {
            String pattern = recentMoves.substring(recentMoves.length() - len);
            int count = patternCounts.getOrDefault(pattern, 0);
            patternCounts.put(pattern, count + 1);

            // 增加一个特殊的检查，以便识别长度为3的重复模式
            if (len == 3 && recentMoves.length() >= 9) {
                String lastThreePatterns = recentMoves.substring(recentMoves.length() - 9);
                if (lastThreePatterns.equals(pattern + pattern + pattern)) {
                    patternPriorities.put(pattern, Double.MAX_VALUE);
                    continue;
                }
            }

            if (count >= THRESHOLD) {
                double priority = Math.pow(len, count) * Math.pow(DECAY_FACTOR, recentMoves.length() - len);
                patternPriorities.put(pattern, priority);
            }
        }
    }


    public String makeMove() {
        // 如果对手的出招足够随机，我们也随机出招
        if (isOpponentRandom() || patternPriorities.isEmpty()) {
            // 这里使用一个简单的循环来实现伪随机
            int moveIndex = recentMoves.length() % 3;
            switch (moveIndex) {
                case 0:
                    return "R"; // 石头
                case 1:
                    return "P"; // 布
                case 2:
                    return "S"; // 剪刀
            }
        }

        // 如果我们检测到一个周期，就预测对手的下一步动作并选择能赢的动作
        if (!cycle.isEmpty()) {
            char predictedMove = cycle.charAt(0);
            cycle = cycle.substring(1) + predictedMove;
            switch (predictedMove) {
                case 'R':
                    return "P"; // 对手出石头，我出布
                case 'P':
                    return "S"; // 对手出布，我出剪刀
                case 'S':
                    return "R"; // 对手出剪刀，我出石头
            }
        }

        // 否则，我们按照上面的方法选择基于模式的出招
        // 使用马尔可夫模型预测
            if (secondLastMove != -1 && lastMove != -1) {
                int nextMove = 0;
                for (int i = 0; i < 3; i++) {
                    if (transitionCounts[secondLastMove][lastMove][i] > transitionCounts[secondLastMove][lastMove][nextMove]) {
                        nextMove = i;
                    }
                }

                // 如果马尔可夫模型预测出了一个动作，我们就用这个动作
                if (transitionCounts[secondLastMove][lastMove][nextMove] > 0) {
                    switch ((nextMove + 1) % 3) { // 选择可以打败预测出招的出招
                        case 0:
                            return "R";
                        case 1:
                            return "P";
                        default:
                            return "S";
                    }
                }}

        // 找到优先级最高的模式
        String bestPattern = Collections.max(patternPriorities.entrySet(), Map.Entry.comparingByValue()).getKey();
        // 根据优先级最高的模式选择出招
        if (bestPattern.length() == 3) {
            // 这是一个循环模式，我们预测对手的下一个动作是什么
            char predictedMove = bestPattern.charAt(recentMoves.length() % 3);
            switch (predictedMove) {
                case 'R':
                    return "P"; // 预测对手出石头，我出布
                case 'P':
                    return "S"; // 预测对手出布，我出剪刀
                case 'S':
                    return "R"; // 预测对手出剪刀，我出石头
            }
        } else {
            // 对于非循环模式，我们假设对手的下一个动作和他们的最后一个动作相同
            char lastMove = bestPattern.charAt(bestPattern.length() - 1);
            switch (lastMove) {
                case 'R':
                    return "P"; // 对手出石头，我出布
                case 'P':
                    return "S"; // 对手出布，我出剪刀
                case 'S':
                    return "R"; // 对手出剪刀，我出石头
            }
        }
        return "R";
    }








    private boolean isOpponentRandom() {
        int rockCount = 0, paperCount = 0, scissorsCount = 0;
        for (int i = 0; i < recentMoves.length(); i++) {
            switch (recentMoves.charAt(i)) {
                case 'R' -> rockCount++;
                case 'P' -> paperCount++;
                case 'S' -> scissorsCount++;
            }
        }

        double total = recentMoves.length();
        double expected = total / 3;

        double chiSquared = Math.pow(rockCount - expected, 2) / expected +
                Math.pow(paperCount - expected, 2) / expected +
                Math.pow(scissorsCount - expected, 2) / expected;

        return chiSquared <= 5.99;
    }

}
