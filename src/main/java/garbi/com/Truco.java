package garbi.com;
public class Truco {
    public static int valorCarta(Carta c) {
        int n = c.getNumero();
        String p = c.getPalo();
        if (n == 1 && p.equals("espada")) return 14;
        if (n == 1 && p.equals("basto"))  return 13;
        if (n == 7 && p.equals("espada")) return 12;
        if (n == 7 && p.equals("oro"))    return 11;
        if (n == 3)  return 10;
        if (n == 2)  return 9;
        if (n == 1)  return 8;
        if (n == 12) return 7;
        if (n == 11) return 6;
        if (n == 10) return 5;
        if (n == 7)  return 4;
        if (n == 6)  return 3;
        if (n == 5)  return 2;
        return 1; // el 4
    }
}