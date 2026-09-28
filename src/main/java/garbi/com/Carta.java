package garbi.com;
public class Carta {
    private int numero;
    private String palo;
    public Carta(int numero, String palo) {
        this.numero = numero;
        this.palo = palo;
    }
    public int getNumero() {
        return numero;
    }
    public String getPalo() {
        return palo;
    }
    public void mostrarCarta() {
        System.out.println(numero + " de " + palo);
    }
    public int getValorEnvido() {
    return numero >= 10 ? 0 : numero;
    }
}
