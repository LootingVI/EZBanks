package de.flori.ezbanks.manager.impl;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    private String bankId;
    private UUID ownerUuid;

    private double balance;

    private int pin;
    private boolean suspended;

}
