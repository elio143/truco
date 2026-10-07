package garbi.com;

public class Main {
    // Método principal que ejecuta Java al iniciar el programa
    public static void main(String[] args) {
        // Se crea el juego fijando el objetivo en 15 puntos
        Juego juego = new Juego(15);
        
        // Arranca la partida
        juego.iniciar();
    }
}