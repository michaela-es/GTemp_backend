package gtemp.gtemp_io.dto;

import lombok.Data;

@Data
public class AddToWalletRequest {
    private Double amount;

    public AddToWalletRequest(Double amount) {
        this.amount = amount;
    }
}