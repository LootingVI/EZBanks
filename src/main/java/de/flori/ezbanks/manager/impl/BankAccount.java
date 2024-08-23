package de.flori.ezbanks.manager.impl;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

    @Generated
    public BankAccount(String bankId, UUID ownerId, double balance, int pin, boolean suspended) {
        this.bankId = bankId;
        this.ownerId = ownerId;
        this.balance = balance;
        this.pin = pin;
        this.suspended = suspended;
    }

    private String bankId;
    private UUID ownerId;

    private double balance;

    private int pin;
    private boolean suspended;

    @Generated
    public String getBankId() {
        return bankId;
    }

    @Generated
    public UUID getOwnerId() {
        return ownerId;
    }

    @Generated
    public double getBalance() {
        return balance;
    }

    @Generated
    public int getPin() {
        return pin;
    }

    @Generated
    public boolean isSuspended() {
        return suspended;
    }


}
