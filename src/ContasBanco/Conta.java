package ContasBanco;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Conta {
    private String id;
    private String nome;
    private BigDecimal saldo;

    public Conta(String id, String nome, BigDecimal saldo) {
        this.id = id;
        this.nome = nome;
        this.saldo = saldo;
    }

    public String getID() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getSaldo() { return saldo; }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo.setScale(2, RoundingMode.HALF_EVEN);
    }
}
