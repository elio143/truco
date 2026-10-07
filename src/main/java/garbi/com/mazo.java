package garbi.com;

import java.util.ArrayList;
import java.util.Collections;

public class Mazo {
    // Lista donde guardamos todas las cartas
    private ArrayList<Carta> cartas = new ArrayList<>();

    // Constructor: genera las 40 cartas (sin 8 ni 9)
    public Mazo() {
        String[] palos = {"espada", "basto", "oro", "copa"};
        int[] numeros = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12};

        // Genera la combinación de cada palo con cada número
        for (String palo : palos) {
            for (int numero : numeros) {
                cartas.add(new Carta(numero, palo));
            }
        }
    }

    // Mezcla las cartas aleatoriamente
    public void mezclar() {
        Collections.shuffle(cartas);
    }

    // Saca y devuelve la primera carta del mazo
    public Carta sacarCarta() {
        if (cartas.isEmpty()) return null;
        return cartas.remove(0);
    }
}