package com.USWRandomChat.backend.Matching;

public class MatchedPair {
    private final Participant participant1;
    private final Participant participant2;

    public MatchedPair(Participant participant1, Participant participant2) {
        this.participant1 = participant1;
        this.participant2 = participant2;
    }

    public Participant getParticipant1() {
        return participant1;
    }

    public Participant getParticipant2() {
        return participant2;
    }
}
