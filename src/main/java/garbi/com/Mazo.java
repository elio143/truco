package garbi.com;
import java.util.ArrayList;
import java.util.Collections;
public class Mazo {
    private ArrayList<Carta> cartas;
    public Mazo() {
        cartas = new ArrayList<>();
        String[] palos = {
            "espada",
            "basto",
            "oro",
            "copa"
        };
        int[] numeros = {
            1, 2, 3, 4, 5, 6, 7, 10, 11, 12
        };
        for (String palo : palos) {
            for (int numero : numeros) {
                cartas.add(new Carta(numero, palo));
            }
        }
    }
    public void mezclar() {
        Collections.shuffle(cartas);
    }
    public Carta sacarCarta() {
        if (cartas.isEmpty()) {
            return null;
        }

        return cartas.remove(0);
    }
    public int cantidadCartas() {
        return cartas.size();
    }
}
