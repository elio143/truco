package garbi.com;

public class Carta {
    // Atributos de la carta
    private int numero;
    private String palo;

    // Constructor: crea la carta con su número y palo
    public Carta(int numero, String palo) {
        this.numero = numero;
        this.palo = palo;
    }

    // Devuelve el número de la carta
    public int getNumero() { return numero; }

    // Devuelve el palo de la carta ("espada", "basto", etc.)
    public String getPalo() { return palo; }

    // Calcula el valor individual de esta carta para el Envido
    // Las figuras (10, 11, 12) valen 0; las demás valen su número
    public int getValorEnvido() {
        return numero >= 10 ? 0 : numero;
    }

    // Muestra la carta en formato de texto legible (ej: "7 de espada")
    @Override
    public String toString() {
        return numero + " de " + palo;
    }
}