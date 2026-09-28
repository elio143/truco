package garbi.com;
import java.util.ArrayList;
public class Jugador {
    private String nombre;
    private ArrayList<Carta> cartas;
    private int puntos;               // <-- NUEVO
    public Jugador(String nombre) {
        this.nombre = nombre;
        cartas = new ArrayList<>();
    }
    public void recibirCarta(Carta carta) {
        cartas.add(carta);
    }
    public void mostrarCartas() {
        System.out.println("Cartas de " + nombre + ":");
        for (int i = 0; i < cartas.size(); i++) {
            System.out.print((i + 1) + ". ");
            cartas.get(i).mostrarCarta();
        }
    }
    public Carta jugarCarta(int posicion) {
        if (posicion < 0 || posicion >= cartas.size()) {
            return null;
        }
        return cartas.remove(posicion);
    }
    public String getNombre() {
        return nombre;
    }
    // ---------- NUEVO ----------
    public int getPuntos() {
        return puntos;
    }
    public void sumarPuntos(int cantidad) {
        puntos += cantidad;
    }
    public int cantidadCartas() {
        return cartas.size();
    }
    public Carta verCarta(int posicion) {
    return cartas.get(posicion);
    }
    public int calcularEnvido() {
    int mejor = 0;
    for (Carta c : cartas) {
        mejor = Math.max(mejor, c.getValorEnvido());
    }
    for (int i = 0; i < cartas.size(); i++) {
        for (int j = i + 1; j < cartas.size(); j++) {
            Carta a = cartas.get(i);
            Carta b = cartas.get(j);
            if (a.getPalo().equals(b.getPalo())) {
                mejor = Math.max(mejor, 20 + a.getValorEnvido() + b.getValorEnvido());
            }
        }
    }
    return mejor;
    }
    // ---------------------------
}
