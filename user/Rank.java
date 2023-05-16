package com.example.assignmentdistributed.user;

import java.io.Serializable;

public class Rank {
    private List<Queue<UserHandler>> rankedModeQueues;

    public Server(int port) {
        // ...
        rankedModeQueues = new ArrayList<>();
        for (int i = 0; i < Rank.values().length; i++) {
            rankedModeQueues.add(new LinkedList<>());
        }
    }

    public void addUserToRankedQueue(UserHandler userHandler){
        int rank = userHandler.user.getRank();
        lock.lock();
        try {
            Queue<UserHandler> queue = rankedModeQueues.get(rank);
            queue.add(userHandler);
            System.out.println("SERVER - User added to queue - user: " + userHandler.user.getUsername() + "  rank: " + rank);
        } finally {
            lock.unlock();
        }
    }

    public void removeUserFromRankedQueue(UserHandler userHandler){
        int rank = userHandler.user.getRank();
        lock.lock();
        try {
            Queue<UserHandler> queue = rankedModeQueues.get(rank);
            boolean removed = queue.remove(userHandler);
            if (removed) {
                System.out.println("REMOVED USER " + userHandler.user.getUsername() + " FROM RANKED QUEUE " + rank);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getRankedQueueLength(int rank){
        return rankedModeQueues.get(rank).size();
    }

    

    public enum Rank {
        IRON(1, 300),
        BRONZE(301, 600),
        SILVER(601, 900),
        GOLD(901, 1200),
        PLATINUM(1201, 1500),
        DIAMOND(1501, 1800),
        MASTER(1801, 2100),
        CHALLENGER(2101, Integer.MAX_VALUE);

        private final int minRange;
        private final int maxRange;

        Rank(int minRange, int maxRange) {
            this.minRange = minRange;
            this.maxRange = maxRange;
        }

        public static Rank getRankByrank(int rank) {
            for (Rank r : Rank.values()) {
                if (rank >= r.minRange && rank <= r.maxRange) {
                    return r;
                }
            }
            return null;
        }
    }
}
