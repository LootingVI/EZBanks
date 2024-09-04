package de.flori.ezbanks.manager.impl;

import de.flori.ezbanks.manager.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private TransactionType type;
    private double amount;
    private long timestamp;
    private UUID player;

}
