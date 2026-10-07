package garbi.com;

import java.util.ArrayList;

public class Jugador {
    private String nombre;
    private ArrayList<Carta> mano = new ArrayList<>();
    private int puntos = 0;

    // Constructor: le asigna un nombre al jugador
    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    // Agrega una carta a la mano del jugador
    public void recibirCarta(Carta carta) {
        mano.add(carta);
    }

    // Imprime en pantalla las cartas que tiene el jugador
    public void mostrarMano() {
        System.out.println("Cartas de " + nombre + ":");
        for (int i = 0; i < mano.size(); i++) {
            System.out.println((i + 1) + ". " + mano.get(i));
        }
    }

    // Juega la carta elegida y la remueve de la mano
    public Carta jugarCarta(int indice) {
        if (indice >= 0 && indice < mano.size()) {
            return mano.remove(indice);
        }
        return null;
    }

    // Vacía la mano para iniciar una nueva ronda
    public void limpiarMano() {
        mano.clear();
    }

    // Calcula los puntos del Envido
    public int calcularEnvido() {
        int maximo = 0;

        // 1. Busca la carta con valor más alto por si no tiene dos del mismo palo
        for (Carta c : mano) {
            if (c.getValorEnvido() > maximo) maximo = c.getValorEnvido();
        }

        // 2. Si tiene 2 cartas del mismo palo, le suma 20 + el valor de ambas
        for (int i = 0; i < mano.size(); i++) {
            for (int j = i + 1; j < mano.size(); j++) {
                Carta a = mano.get(i);
                Carta b = mano.get(j);
                if (a.getPalo().equals(b.getPalo())) {
                    int suma = 20 + a.getValorEnvido() + b.getValorEnvido();
                    if (suma > maximo) maximo = suma;
                }
            }
        }
        return maximo;
    }

    // Métodos para consultar datos o modificar puntos
    public String getNombre() { return nombre; }
    public int getPuntos() { return puntos; }
    public void sumarPuntos(int pts) { puntos += pts; }
    public int cantidadCartas() { return mano.size(); }
}