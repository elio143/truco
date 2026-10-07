package garbi.com;

public class Truco {
    // Retorna una jerarquía numérica: a mayor número, más fuerte la carta
    public static int valorCarta(Carta c) {
        int n = c.getNumero();
        String p = c.getPalo();

        if (n == 1 && p.equals("espada")) return 14; // Ancho de espada
        if (n == 1 && p.equals("basto"))  return 13; // Ancho de basto
        if (n == 7 && p.equals("espada")) return 12; // 7 de espada
        if (n == 7 && p.equals("oro"))    return 11; // 7 de oro
        if (n == 3)  return 10;
        if (n == 2)  return 9;
        if (n == 1)  return 8; // Anchos falsos (copa y oro)
        if (n == 12) return 7; // Reyes
        if (n == 11) return 6; // Caballos
        if (n == 10) return 5; // Sotas
        if (n == 7)  return 4; // 7s falsos (basto y copa)
        if (n == 6)  return 3;
        if (n == 5)  return 2;
        return 1;              // Cuatros (las más bajas)
    }
}