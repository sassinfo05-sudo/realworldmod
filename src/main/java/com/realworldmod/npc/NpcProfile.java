package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;

import java.util.UUID;

/**
 * Persistent record for a single simulated citizen. Backed 1:1 by a row in
 * the {@code citizens} SQLite table (see {@link NpcDatabase}).
 */
public final class NpcProfile {
    private final UUID id;
    private String name;
    private String homeAddress;
    private String workplaceAddress;
    private long incomeCentsPerPayPeriod;
    private int workStartHour;
    private int workEndHour;
    private int homeX;
    private int homeY;
    private int homeZ;
    private int workplaceX;
    private int workplaceY;
    private int workplaceZ;
    private DailyState currentState;

    public NpcProfile(UUID id, String name, String homeAddress, String workplaceAddress,
                       long incomeCentsPerPayPeriod, int workStartHour, int workEndHour,
                       int homeX, int homeY, int homeZ,
                       int workplaceX, int workplaceY, int workplaceZ,
                       DailyState currentState) {
        this.id = id;
        this.name = name;
        this.homeAddress = homeAddress;
        this.workplaceAddress = workplaceAddress;
        this.incomeCentsPerPayPeriod = incomeCentsPerPayPeriod;
        this.workStartHour = workStartHour;
        this.workEndHour = workEndHour;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.workplaceX = workplaceX;
        this.workplaceY = workplaceY;
        this.workplaceZ = workplaceZ;
        this.currentState = currentState;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String homeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String workplaceAddress() {
        return workplaceAddress;
    }

    public void setWorkplaceAddress(String workplaceAddress) {
        this.workplaceAddress = workplaceAddress;
    }

    public long incomeCentsPerPayPeriod() {
        return incomeCentsPerPayPeriod;
    }

    public void setIncomeCentsPerPayPeriod(long incomeCentsPerPayPeriod) {
        this.incomeCentsPerPayPeriod = incomeCentsPerPayPeriod;
    }

    public int workStartHour() {
        return workStartHour;
    }

    public int workEndHour() {
        return workEndHour;
    }

    public int homeX() {
        return homeX;
    }

    public int homeY() {
        return homeY;
    }

    public int homeZ() {
        return homeZ;
    }

    public int workplaceX() {
        return workplaceX;
    }

    public int workplaceY() {
        return workplaceY;
    }

    public int workplaceZ() {
        return workplaceZ;
    }

    public DailyState currentState() {
        return currentState;
    }

    public void setCurrentState(DailyState currentState) {
        this.currentState = currentState;
    }
}
