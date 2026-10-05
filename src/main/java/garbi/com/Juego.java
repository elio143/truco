package garbi.com;

import java.util.Scanner;

public class Juego {
    private Jugador jugador = new Jugador("Jugador"), compu = new Jugador("Computadora");
    private Mazo mazo;
    private Scanner entrada = new Scanner(System.in);
    private int puntosParaGanar, nivelTruco, ultimoCanto, manoActual;
    private int tantoJ, tantoC;
    private boolean manoJ, finRonda, envidoCantado;

    public Juego(int puntosParaGanar) { this.puntosParaGanar = puntosParaGanar; }

    public void iniciar() {
        System.out.println("=== TRUCO ARGENTINO ===");
        while (jugador.getPuntos() < puntosParaGanar && compu.getPuntos() < puntosParaGanar) {
            jugarRonda();
            System.out.println("\nMARCADOR -> Vos: " + jugador.getPuntos() + " | Compu: " + compu.getPuntos());
        }
        System.out.println(jugador.getPuntos() >= puntosParaGanar ? "\n¡Ganaste la partida!" : "\nGanó la computadora.");
        entrada.close();
    }

    private void jugarRonda() {
        mazo = new Mazo(); mazo.mezclar();
        for (int i = 0; i < 3; i++) { jugador.recibirCarta(mazo.sacarCarta()); compu.recibirCarta(mazo.sacarCarta()); }
        tantoJ = jugador.calcularEnvido(); tantoC = compu.calcularEnvido();
        nivelTruco = 0; ultimoCanto = 0; finRonda = false; envidoCantado = false;
        manoJ = !manoJ;

        System.out.println("\n*** NUEVA RONDA *** (" + (manoJ ? "Sos mano" : "Compu es mano") + ")");
        int[] res = new int[3];
        int lider = manoJ ? 1 : 2, gan = 0;

        for (int i = 0; i < 3 && gan == 0; i++) {
            manoActual = i + 1;
            System.out.println("\n--- Mano " + manoActual + " ---");
            Carta cJ = null, cC = null;

            if (lider == 1) {
                if ((cJ = turnoJ()) == null || finRonda) break;
                if ((cC = turnoC(cJ)) == null || finRonda) break;
            } else {
                if ((cC = turnoC(null)) == null || finRonda) break;
                if ((cJ = turnoJ()) == null || finRonda) break;
            }

            int vJ = Truco.valorCarta(cJ), vC = Truco.valorCarta(cC);
            if (vJ > vC) { res[i] = 1; System.out.println("Ganaste la mano."); }
            else if (vC > vJ) { res[i] = 2; System.out.println("Ganó la computadora."); }
            else System.out.println("Parda.");

            lider = res[i] != 0 ? res[i] : (manoJ ? 1 : 2);
            gan = evaluarGanadorRonda(res, i + 1);
        }

        if (!finRonda) {
            int pts = nivelTruco == 0 ? 1 : nivelTruco + 1;
            Jugador g = gan == 1 ? jugador : compu;
            g.sumarPuntos(pts);
            System.out.println((gan == 1 ? "Ganaste" : "Ganó la compu") + " la ronda (+" + pts + ").");
        }
        vaciarManos();
    }

    private int evaluarGanadorRonda(int[] r, int j) {
        int gJ = 0, gC = 0;
        for (int i = 0; i < j; i++) { if (r[i] == 1) gJ++; if (r[i] == 2) gC++; }
        if (gJ == 2) return 1; if (gC == 2) return 2;
        if (j >= 2 && r[0] == 0 && r[1] != 0) return r[1];
        if (j >= 2 && r[0] != 0 && r[1] == 0) return r[0];
        if (j == 3) return r[2] != 0 ? r[2] : (r[0] != 0 ? r[0] : (manoJ ? 1 : 2));
        return 0;
    }

    // ================= TURNOS =================

    private Carta turnoJ() {
        while (true) {
            jugador.mostrarCartas();
            System.out.println("\n1. Jugar carta");
            if (nivelTruco < 3 && ultimoCanto != 1) System.out.println("2. Cantar " + nombreTruco(nivelTruco + 1));
            if (!envidoCantado && nivelTruco == 0 && manoActual == 1) System.out.println("3. Envido | 4. Real Envido | 5. Falta Envido (Tus tantos: " + tantoJ + ")");
            
            int op = leer();
            if (op == 1) {
                System.out.print("Elegí carta: ");
                Carta c = jugador.jugarCarta(leer() - 1);
                if (c != null) { System.out.print("Jugaste: "); c.mostrarCarta(); return c; }
            } else if (op == 2 && nivelTruco < 3 && ultimoCanto != 1) {
                cantarTruco(1); if (finRonda) return null;
            } else if (op >= 3 && op <= 5 && !envidoCantado && nivelTruco == 0 && manoActual == 1) {
                cantarEnvido(1, op - 2); if (finRonda) return null;
            } else System.out.println("Opción no válida.");
        }
    }

    private Carta turnoC(Carta rival) {
        if (!envidoCantado && nivelTruco == 0 && manoActual == 1 && tantoC >= 26) {
            cantarEnvido(2, tantoC >= 30 ? 2 : 1); if (finRonda) return null;
        }
        if (nivelTruco < 3 && ultimoCanto != 2 && cartasCompuBuenas()) {
            cantarTruco(2); if (finRonda) return null;
        }
        int pos = elegirCartaCompu(rival);
        Carta c = compu.jugarCarta(pos);
        System.out.print("La compu jugó: "); c.mostrarCarta();
        return c;
    }

    // ================= TRUCO Y ENVIDO =================

    private void cantarTruco(int quien) {
        int nuevo = nivelTruco + 1;
        System.out.println("\n" + (quien == 1 ? "¡Cantaste " : "La compu: ¡") + nombreTruco(nuevo) + "!");
        
        boolean quiere = quien == 1 ? cartasCompuBuenas() : (respuestaJ("Quiero " + nombreTruco(nuevo)) == 1);
        if (!quiere) {
            System.out.println((quien == 1 ? "La compu" : "Vos") + ": No quiero.");
            Jugador g = quien == 1 ? jugador : compu;
            g.sumarPuntos(nivelTruco == 0 ? 1 : nivelTruco + 1);
            finRonda = true;
        } else {
            nivelTruco = nuevo; ultimoCanto = quien;
            System.out.println((quien == 1 ? "La compu" : "Vos") + ": ¡Quiero!");
        }
    }

    private void cantarEnvido(int quien, int tipo) {
        envidoCantado = true;
        System.out.println("\n" + (quien == 1 ? "¡Cantaste " : "La compu: ¡") + nombreEnvido(tipo) + "!");
        
        boolean quiere = quien == 1 ? tantoC >= 25 : (respuestaJ("Quiero " + nombreEnvido(tipo)) == 1);
        if (!quiere) {
            System.out.println((quien == 1 ? "La compu" : "Vos") + ": No quiero.");
            Jugador g = quien == 1 ? jugador : compu;
            g.sumarPuntos(1);
        } else {
            System.out.println((quien == 1 ? "La compu" : "Vos") + ": ¡Quiero!");
            boolean ganaJ = manoJ ? tantoJ >= tantoC : tantoJ > tantoC;
            System.out.println("\nTantos -> Vos: " + tantoJ + " | Compu: " + tantoC);
            Jugador g = ganaJ ? jugador : compu;
            int pts = tipo == 3 ? Math.max(1, puntosParaGanar - Math.max(jugador.getPuntos(), compu.getPuntos())) : (tipo == 1 ? 2 : 3);
            g.sumarPuntos(pts);
            System.out.println((ganaJ ? "Ganaste" : "La compu ganó") + " el envido (+" + pts + ").");
        }
        if (jugador.getPuntos() >= puntosParaGanar || compu.getPuntos() >= puntosParaGanar) finRonda = true;
    }

    // ================= IA SIMPLE Y AUXILIARES =================

    private boolean cartasCompuBuenas() {
        if (compu.cantidadCartas() == 0) return false;
        return Truco.valorCarta(compu.verCarta(0)) >= 8;
    }

    private int elegirCartaCompu(Carta rival) {
        if (rival != null) {
            for (int i = 0; i < compu.cantidadCartas(); i++) {
                if (Truco.valorCarta(compu.verCarta(i)) > Truco.valorCarta(rival)) return i;
            }
        }
        return 0;
    }

    private int respuestaJ(String msj) {
        System.out.println("1. " + msj + "\n2. No quiero");
        return leer();
    }

    private String nombreTruco(int n) { return n == 1 ? "Truco" : (n == 2 ? "Retruco" : "Vale cuatro"); }
    private String nombreEnvido(int t) { return t == 1 ? "Envido" : (t == 2 ? "Real Envido" : "Falta Envido"); }
    private int leer() { while (!entrada.hasNextInt()) entrada.next(); return entrada.nextInt(); }
    private void vaciarManos() { while (jugador.cantidadCartas() > 0) jugador.jugarCarta(0); while (compu.cantidadCartas() > 0) compu.jugarCarta(0); }
}