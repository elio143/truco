package garbi.com;

import java.util.Scanner;

public class Juego {
    private Jugador jugador = new Jugador("Jugador");
    private Jugador compu = new Jugador("Computadora");
    private Scanner entrada = new Scanner(System.in);
    private int puntosParaGanar;

    private int nivelTruco = 1;     // 1: Normal(1pt), 2: Truco(2pts), 3: Retruco(3pts), 4: Vale 4(4pts)
    private boolean envidoJugado = false; // Controla que el envido solo se juegue una vez por ronda

    // Constructor: define el límite de puntos del partido (ej: 15 o 30)
    public Juego(int puntosParaGanar) {
        this.puntosParaGanar = puntosParaGanar;
    }

    // Inicia el bucle principal del partido
    public void iniciar() {
        System.out.println("=== TRUCO ARGENTINO ===");

        while (jugador.getPuntos() < puntosParaGanar && compu.getPuntos() < puntosParaGanar) {
            jugarRonda();
            System.out.println("\nMARCADOR -> Vos: " + jugador.getPuntos() + " | Compu: " + compu.getPuntos());
        }

        if (jugador.getPuntos() >= puntosParaGanar) {
            System.out.println("\n¡Ganaste la partida!");
        } else {
            System.out.println("\nGanó la computadora.");
        }
    }

    // Ejecuta una ronda completa (3 manos de cartas)
    private void jugarRonda() {
        Mazo mazo = new Mazo();
        mazo.mezclar();

        // Reparte 3 cartas a cada participante
        jugador.limpiarMano();
        compu.limpiarMano();
        for (int i = 0; i < 3; i++) {
            jugador.recibirCarta(mazo.sacarCarta());
            compu.recibirCarta(mazo.sacarCarta());
        }

        nivelTruco = 1;
        envidoJugado = false;

        int victoriasJ = 0;
        int victoriasC = 0;

        // Se juegan hasta 3 manos por ronda
        for (int mano = 1; mano <= 3; mano++) {
            System.out.println("\n--- Mano " + mano + " ---");
            Carta cartaJ = menuJugador(mano);

            // Si se retiró alguien por un truco "No quiero", se corta la ronda
            if (cartaJ == null) {
                return;
            }

            // La computadora tira la primera carta disponible de su mano
            Carta cartaC = compu.jugarCarta(0);
            System.out.println("La compu jugó: " + cartaC);

            // Compara los valores de las cartas según las reglas del Truco
            int valJ = Truco.valorCarta(cartaJ);
            int valC = Truco.valorCarta(cartaC);

            if (valJ > valC) {
                System.out.println("-> Ganaste esta mano.");
                victoriasJ++;
            } else if (valC > valJ) {
                System.out.println("-> Ganó la computadora esta mano.");
                victoriasC++;
            } else {
                System.out.println("-> Empate (Parda).");
            }

            // Si un jugador gana 2 manos, finaliza la ronda
            if (victoriasJ == 2 || victoriasC == 2) break;
        }

        // Asigna el puntaje acumulado por Truco
        int puntosRonda = (nivelTruco == 1) ? 1 : nivelTruco;
        if (victoriasJ > victoriasC) {
            jugador.sumarPuntos(puntosRonda);
            System.out.println("\n¡Sumaste " + puntosRonda + " punto(s) por ganar la ronda!");
        } else if (victoriasC > victoriasJ) {
            compu.sumarPuntos(puntosRonda);
            System.out.println("\nLa compu suma " + puntosRonda + " punto(s) por ganar la ronda.");
        } else {
            System.out.println("\nEmpate total en la ronda. Nadie suma por Truco.");
        }
    }

    // Despliega el menú interactivo para el jugador
    private Carta menuJugador(int manoActual) {
        while (true) {
            jugador.mostrarMano();

            // Muestra los tantos del Envido solo en la primera mano si aún no se cantó
            if (!envidoJugado && manoActual == 1) {
                System.out.println("Tus tantos de Envido: " + jugador.calcularEnvido());
            }

            System.out.println("\nOpciones:");
            System.out.println("1. Jugar carta");

            if (!envidoJugado && manoActual == 1) {
                System.out.println("2. Cantar Envido");
                System.out.println("3. Cantar Real Envido");
                System.out.println("4. Cantar Falta Envido");
            }

            if (nivelTruco < 4) {
                String siguienteTruco = (nivelTruco == 1) ? "Truco" : (nivelTruco == 2) ? "Retruco" : "Vale 4";
                System.out.println("5. Cantar " + siguienteTruco);
            }

            System.out.print("Elegí una opción: ");
            int op = entrada.nextInt();

            if (op == 1) {
                System.out.print("Elegí la carta (1, 2 o 3): ");
                int cOp = entrada.nextInt() - 1;
                Carta jugada = jugador.jugarCarta(cOp);
                if (jugada != null) return jugada;
                System.out.println("Carta inválida.");

            } else if (op >= 2 && op <= 4 && !envidoJugado && manoActual == 1) {
                cantarEnvido(op);

            } else if (op == 5 && nivelTruco < 4) {
                boolean continua = cantarTruco();
                if (!continua) return null; // Si no quiere el Truco, termina la ronda
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }

    // Gestiona los cantos del Envido, la decisión de la compu y los puntos
    private void cantarEnvido(int opcion) {
        envidoJugado = true;
        int ptsApuesta = (opcion == 2) ? 2 : (opcion == 3) ? 3 : (puntosParaGanar - Math.max(jugador.getPuntos(), compu.getPuntos()));
        String nombreCanto = (opcion == 2) ? "Envido" : (opcion == 3) ? "Real Envido" : "Falta Envido";

        System.out.println("\n¡Cantaste " + nombreCanto + "!");

        int tantoJ = jugador.calcularEnvido();
        int tantoC = compu.calcularEnvido();

        // Si la computadora tiene 23 o más de Envido, acepta ("Quiero")
        if (tantoC >= 23) {
            System.out.println("La compu dice: ¡Quiero!");
            System.out.println("Tus tantos: " + tantoJ + " | Tantos compu: " + tantoC);

            if (tantoJ >= tantoC) {
                jugador.sumarPuntos(ptsApuesta);
                System.out.println("¡Ganaste el Envido! (+" + ptsApuesta + " pts)");
            } else {
                compu.sumarPuntos(ptsApuesta);
                System.out.println("La compu ganó el Envido. (+" + ptsApuesta + " pts)");
            }
        } else {
            System.out.println("La compu dice: No quiero.");
            jugador.sumarPuntos(1);
            System.out.println("Sumaste 1 punto por el Envido no querido.");
        }
    }

    // Gestiona los aumentos de apuesta (Truco, Retruco y Vale 4)
    private boolean cantarTruco() {
        int siguienteNivel = nivelTruco + 1;
        String nombreCanto = (siguienteNivel == 2) ? "Truco" : (siguienteNivel == 3) ? "Retruco" : "Vale 4";

        System.out.println("\n¡Cantaste " + nombreCanto + "!");

        // La computadora acepta el canto con un 70% de probabilidad
        boolean acepta = Math.random() < 0.7;

        if (acepta) {
            nivelTruco = siguienteNivel;
            System.out.println("La compu dice: ¡Quiero!");
            return true;
        } else {
            System.out.println("La compu dice: No quiero.");
            int ptsGanados = (nivelTruco == 1) ? 1 : nivelTruco;
            jugador.sumarPuntos(ptsGanados);
            System.out.println("Ganaste la ronda (+" + ptsGanados + " pts).");
            return false;
        }
    }
}