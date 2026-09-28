package garbi.com;

import java.util.Random;
import java.util.Scanner;

public class Juego {
    private static final int NADIE = 0;
    private static final int JUGADOR = 1;
    private static final int COMPU = 2;

    private static final int NO_QUIERO = 0;
    private static final int QUIERO = 1;
    private static final int SUBO = 2;

    private static final int ENVIDO = 1;
    private static final int REAL_ENVIDO = 2;
    private static final int FALTA_ENVIDO = 3;

    private Jugador jugador;
    private Jugador computadora;
    private Mazo mazo;
    private Scanner entrada;
    private Random azar;
    private int puntosParaGanar;

    // Estado de la ronda
    private boolean jugadorEsMano;
    private int nivelTruco;      // 0 nada, 1 truco, 2 retruco, 3 vale cuatro
    private int ultimoCanto;     // quién hizo el último canto de truco aceptado
    private boolean rondaTerminada;
    private int manosJugador;
    private int manosCompu;
    private int manoActual;

    // Estado del envido
    private boolean envidoCantado;
    private int tantoJugador;
    private int tantoCompu;
    private int subidaElegida;    // tipo de canto con el que se sube
    private int tantoEstimado;    // lo que "cree" tener la computadora (con ruido)

    public Juego(int puntosParaGanar) {
        this.puntosParaGanar = puntosParaGanar;
        jugador = new Jugador("Jugador");
        computadora = new Jugador("Computadora");
        entrada = new Scanner(System.in);
        azar = new Random();
    }

    public void iniciar() {
        System.out.println("==============================");
        System.out.println("       TRUCO ARGENTINO");
        System.out.println("==============================");

        while (jugador.getPuntos() < puntosParaGanar
                && computadora.getPuntos() < puntosParaGanar) {
            jugarRonda();
            mostrarMarcador();
        }

        if (jugador.getPuntos() >= puntosParaGanar) {
            System.out.println("\n¡Ganaste la partida!");
        } else {
            System.out.println("\nGanó la computadora.");
        }
        entrada.close();
    }

    private void repartir() {
        mazo = new Mazo();
        mazo.mezclar();
        for (int i = 0; i < 3; i++) {
            jugador.recibirCarta(mazo.sacarCarta());
            computadora.recibirCarta(mazo.sacarCarta());
        }
        // el tanto se calcula ahora, antes de jugar cartas
        tantoJugador = jugador.calcularEnvido();
        tantoCompu = computadora.calcularEnvido();
    }

    // ================= RONDA =================

    private void jugarRonda() {
        repartir();
        nivelTruco = 0;
        ultimoCanto = NADIE;
        rondaTerminada = false;
        envidoCantado = false;
        manosJugador = 0;
        manosCompu = 0;
        jugadorEsMano = !jugadorEsMano;

        System.out.println("\n******** NUEVA RONDA ********");
        System.out.println(jugadorEsMano ? "Sos mano." : "La computadora es mano.");

        int[] res = new int[3];
        int lider = jugadorEsMano ? JUGADOR : COMPU;
        int ganador = NADIE;
        int jugadas = 0;

        while (jugadas < 3 && ganador == NADIE) {
            manoActual = jugadas + 1;
            System.out.println("\n--- Mano " + manoActual + " ---");
            Carta cJ;
            Carta cC;

            if (lider == JUGADOR) {
                cJ = turnoJugador();
                if (rondaTerminada) break;
                cC = turnoComputadora(cJ, manoActual);
                if (rondaTerminada) break;
            } else {
                cC = turnoComputadora(null, manoActual);
                if (rondaTerminada) break;
                cJ = turnoJugador();
                if (rondaTerminada) break;
            }

            int vJ = Truco.valorCarta(cJ);
            int vC = Truco.valorCarta(cC);

            if (vJ > vC) {
                res[jugadas] = JUGADOR;
                manosJugador++;
                System.out.println("Ganaste la mano.");
            } else if (vC > vJ) {
                res[jugadas] = COMPU;
                manosCompu++;
                System.out.println("Ganó la computadora la mano.");
            } else {
                res[jugadas] = NADIE;
                System.out.println("Parda.");
            }

            lider = (res[jugadas] == NADIE)
                    ? (jugadorEsMano ? JUGADOR : COMPU)
                    : res[jugadas];

            jugadas++;
            ganador = definirRonda(res, jugadas);
        }

        if (!rondaTerminada) {
            int puntos = valorRonda();
            if (ganador == JUGADOR) {
                jugador.sumarPuntos(puntos);
                System.out.println("\nGanaste la ronda (+" + puntos + ").");
            } else {
                computadora.sumarPuntos(puntos);
                System.out.println("\nGanó la computadora la ronda (+" + puntos + ").");
            }
        }
        vaciarManos();
    }

    // Reglas de parda. Devuelve JUGADOR, COMPU o NADIE (todavía no se define)
    private int definirRonda(int[] r, int jugadas) {
        int gJ = 0;
        int gC = 0;
        for (int i = 0; i < jugadas; i++) {
            if (r[i] == JUGADOR) gJ++;
            else if (r[i] == COMPU) gC++;
        }
        if (gJ == 2) return JUGADOR;
        if (gC == 2) return COMPU;

        if (jugadas >= 2) {
            if (r[0] == NADIE && r[1] != NADIE) return r[1];
            if (r[0] != NADIE && r[1] == NADIE) return r[0];
        }

        if (jugadas == 3) {
            if (r[2] != NADIE) return r[2];
            if (r[0] != NADIE) return r[0];
            return jugadorEsMano ? JUGADOR : COMPU;
        }
        return NADIE;
    }

    // ================= TURNOS =================

    // Devuelve la carta jugada, o null si la ronda terminó
    private Carta turnoJugador() {
        while (true) {
            System.out.println();
            jugador.mostrarCartas();
            System.out.println();
            System.out.println("1. Jugar carta");
            boolean puedeTruco = puedeCantar(JUGADOR);
            if (puedeTruco) {
                System.out.println("2. Cantar " + nombreCanto(nivelTruco + 1));
            }
            boolean puedeEnvido = envidoDisponible();
            if (puedeEnvido) {
                System.out.println("3. Cantar Envido");
                System.out.println("4. Cantar Real Envido");
                System.out.println("5. Cantar Falta Envido");
                System.out.println("(Tus tantos: " + tantoJugador + ")");
            }
            System.out.print("> ");
            int op = leerEntero();

            if (op == 1) {
                Carta c = pedirCarta();
                System.out.print("Jugaste: ");
                c.mostrarCarta();
                return c;
            } else if (op == 2 && puedeTruco) {
                cantar(JUGADOR);
                if (rondaTerminada) return null;
            } else if (op >= 3 && op <= 5 && puedeEnvido) {
                cantarEnvido(JUGADOR, op - 2);
                if (rondaTerminada) return null;
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }

    // cartaJugador es null si la computadora juega primero
    private Carta turnoComputadora(Carta cartaJugador, int mano) {
        // el envido tiene prioridad sobre el truco
        if (envidoDisponible()) {
            int tipo = computadoraQuiereEnvido();
            if (tipo > 0) {
                cantarEnvido(COMPU, tipo);
                if (rondaTerminada) return null;
            }
        }
        if (puedeCantar(COMPU) && computadoraQuiereCantar()) {
            cantar(COMPU);
            if (rondaTerminada) return null;
        }
        int pos = elegirCarta(cartaJugador, mano);
        Carta c = computadora.jugarCarta(pos);
        System.out.print("La computadora jugó: ");
        c.mostrarCarta();
        return c;
    }

    private Carta pedirCarta() {
        Carta carta = null;
        while (carta == null) {
            System.out.print("Elegí una carta para jugar: ");
            carta = jugador.jugarCarta(leerEntero() - 1);
            if (carta == null) {
                System.out.println("Carta no válida.");
            }
        }
        return carta;
    }

    // ================= TRUCO =================

    private boolean puedeCantar(int quien) {
        return nivelTruco < 3 && ultimoCanto != quien;
    }

    private void cantar(int quien) {
        int nuevo = nivelTruco + 1;

        while (true) {
            if (quien == JUGADOR) {
                System.out.println("\n¡Cantaste " + nombreCanto(nuevo) + "!");
            } else {
                System.out.println("\nLa computadora: ¡" + nombreCanto(nuevo) + "!");
            }

            int resp = (quien == JUGADOR)
                    ? respuestaComputadora(nuevo)
                    : respuestaJugador(nuevo);
            String responde = (quien == JUGADOR) ? "La computadora" : "Vos";

            if (resp == NO_QUIERO) {
                System.out.println(responde + ": No quiero.");
                int puntos = valorRonda();
                if (quien == JUGADOR) {
                    jugador.sumarPuntos(puntos);
                    System.out.println("Sumás " + puntos + ".");
                } else {
                    computadora.sumarPuntos(puntos);
                    System.out.println("La computadora suma " + puntos + ".");
                }
                rondaTerminada = true;
                return;
            }

            nivelTruco = nuevo;
            ultimoCanto = quien;
            System.out.println(responde + ": ¡Quiero!");

            if (resp == QUIERO) {
                System.out.println("La ronda vale " + valorRonda() + ".");
                return;
            }

            quien = (quien == JUGADOR) ? COMPU : JUGADOR;
            nuevo++;
        }
    }

    private int respuestaJugador(int nuevo) {
        System.out.println("1. Quiero");
        System.out.println("2. No quiero");
        if (nuevo < 3) {
            System.out.println("3. Quiero y subo con " + nombreCanto(nuevo + 1));
        }
        while (true) {
            System.out.print("> ");
            int op = leerEntero();
            if (op == 1) return QUIERO;
            if (op == 2) return NO_QUIERO;
            if (op == 3 && nuevo < 3) return SUBO;
            System.out.println("Opción no válida.");
        }
    }

    // ================= ENVIDO =================

    private boolean envidoDisponible() {
        return !envidoCantado && nivelTruco == 0 && manoActual == 1;
    }

    // Se puede subir con "tipo" si es más alto que el último,
    // o si es un segundo Envido sobre un único Envido.
    private boolean puedeSubirEnvido(int tipo, int ultimoTipo, int cantidad) {
        if (tipo > ultimoTipo) return true;
        return tipo == ENVIDO && ultimoTipo == ENVIDO && cantidad == 1;
    }

    private boolean hayEnvidoParaSubir(int ultimoTipo, int cantidad) {
        for (int t = ENVIDO; t <= FALTA_ENVIDO; t++) {
            if (puedeSubirEnvido(t, ultimoTipo, cantidad)) return true;
        }
        return false;
    }

    // "quien" hace el primer canto; si el otro sube, se invierten los roles
    private void cantarEnvido(int quien, int tipo) {
        envidoCantado = true;
        int acumulado = 0;   // puntos de los cantos ya aceptados
        int cantidad = 0;

        while (true) {
            cantidad++;
            if (quien == JUGADOR) {
                System.out.println("\n¡Cantaste " + nombreEnvido(tipo) + "!");
            } else {
                System.out.println("\nLa computadora: ¡" + nombreEnvido(tipo) + "!");
            }

            int resp = (quien == JUGADOR)
                    ? respuestaComputadoraEnvido(tipo, cantidad)
                    : respuestaJugadorEnvido(tipo, cantidad);
            String responde = (quien == JUGADOR) ? "La computadora" : "Vos";

            if (resp == NO_QUIERO) {
                System.out.println(responde + ": No quiero.");
                int puntos = Math.max(1, acumulado);
                if (quien == JUGADOR) {
                    jugador.sumarPuntos(puntos);
                    System.out.println("Sumás " + puntos + ".");
                } else {
                    computadora.sumarPuntos(puntos);
                    System.out.println("La computadora suma " + puntos + ".");
                }
                chequearFin();
                return;
            }

            if (tipo == FALTA_ENVIDO) {
                acumulado = valorFalta();
            } else {
                acumulado += (tipo == ENVIDO) ? 2 : 3;
            }
            System.out.println(responde + ": ¡Quiero!");

            if (resp == QUIERO) break;

            // Quiero y subo
            quien = (quien == JUGADOR) ? COMPU : JUGADOR;
            tipo = subidaElegida;
        }

        resolverTantos(acumulado);
    }

    private void resolverTantos(int puntos) {
        int ganador;
        System.out.println();
        if (jugadorEsMano) {
            System.out.println("Vos: Tengo " + tantoJugador + ".");
            if (tantoJugador >= tantoCompu) {
                System.out.println("La computadora: Son buenas.");
                ganador = JUGADOR;
            } else {
                System.out.println("La computadora: Tengo " + tantoCompu + ", son mejores.");
                ganador = COMPU;
            }
        } else {
            System.out.println("La computadora: Tengo " + tantoCompu + ".");
            if (tantoCompu >= tantoJugador) {
                System.out.println("Vos: Son buenas.");
                ganador = COMPU;
            } else {
                System.out.println("Vos: Tengo " + tantoJugador + ", son mejores.");
                ganador = JUGADOR;
            }
        }

        if (ganador == JUGADOR) {
            jugador.sumarPuntos(puntos);
            System.out.println("Ganaste el envido (+" + puntos + ").");
        } else {
            computadora.sumarPuntos(puntos);
            System.out.println("La computadora ganó el envido (+" + puntos + ").");
        }
        chequearFin();
    }

    private int respuestaJugadorEnvido(int tipo, int cantidad) {
        System.out.println("(Tus tantos: " + tantoJugador + ")");
        System.out.println("1. Quiero");
        System.out.println("2. No quiero");
        for (int t = ENVIDO; t <= FALTA_ENVIDO; t++) {
            if (puedeSubirEnvido(t, tipo, cantidad)) {
                System.out.println((t + 2) + ". Quiero y subo con " + nombreEnvido(t));
            }
        }
        while (true) {
            System.out.print("> ");
            int op = leerEntero();
            if (op == 1) return QUIERO;
            if (op == 2) return NO_QUIERO;
            int t = op - 2;
            if (t >= ENVIDO && t <= FALTA_ENVIDO && puedeSubirEnvido(t, tipo, cantidad)) {
                subidaElegida = t;
                return SUBO;
            }
            System.out.println("Opción no válida.");
        }
    }

    // ---------- IA del envido ----------

    // Devuelve el canto que hace la computadora por su cuenta (0 = no canta)
    private int computadoraQuiereEnvido() {
        double r = azar.nextDouble();

        if (tantoCompu >= 31) {
            if (r < 0.10) return FALTA_ENVIDO;
            if (r < 0.50) return REAL_ENVIDO;
            if (r < 0.85) return ENVIDO;
        } else if (tantoCompu >= 27) {
            if (r < 0.10) return REAL_ENVIDO;
            if (r < 0.60) return ENVIDO;
        } else if (tantoCompu >= 24) {
            if (r < 0.20) return ENVIDO;
        } else {
            // Farol: canta con tanto bajo (más si va perdiendo)
            double farol = 0.06;
            if (computadora.getPuntos() + 3 <= jugador.getPuntos()) farol = 0.12;
            if (r < farol) return ENVIDO;
        }
        return 0;
    }

    private int respuestaComputadoraEnvido(int tipo, int cantidad) {
        // ruido de -2 a +2 para que no sea totalmente predecible
        tantoEstimado = tantoCompu + azar.nextInt(5) - 2;

        int minimo = (tipo == ENVIDO) ? 25 : (tipo == REAL_ENVIDO ? 27 : 30);
        if (!jugadorEsMano) minimo++;   // si no es mano, pierde los empates

        boolean puedeSubir = hayEnvidoParaSubir(tipo, cantidad);

        if (puedeSubir && tantoEstimado >= 29 && azar.nextDouble() < 0.5) {
            subidaElegida = elegirSubidaCompu(tipo, cantidad);
            return SUBO;
        }
        if (tantoEstimado >= minimo) return QUIERO;

        // Tanto flojo: a veces miente
        double r = azar.nextDouble();
        if (puedeSubir && r < 0.06) {
            subidaElegida = elegirSubidaCompu(tipo, cantidad);
            return SUBO;                  // farol: sube sin tener
        }
        if (r < 0.20) return QUIERO;      // "no me la creo"
        return NO_QUIERO;
    }

    // Sube con el canto más bajo posible, salvo con tanto altísimo: a veces va por el más alto
    private int elegirSubidaCompu(int tipo, int cantidad) {
        int menor = -1;
        int mayor = -1;
        for (int t = ENVIDO; t <= FALTA_ENVIDO; t++) {
            if (puedeSubirEnvido(t, tipo, cantidad)) {
                if (menor == -1) menor = t;
                mayor = t;
            }
        }
        if (tantoEstimado >= 32 && azar.nextDouble() < 0.5) return mayor;
        return menor;
    }

    // ================= INTELIGENCIA DEL TRUCO =================

    private double fuerzaComputadora() {
        int n = computadora.cantidadCartas();
        if (n == 0) return 0;

        double mejor = 0;
        double suma = 0;
        for (int i = 0; i < n; i++) {
            int v = Truco.valorCarta(computadora.verCarta(i));
            suma += v;
            if (v > mejor) mejor = v;
        }
        double f = (mejor * 0.6 + (suma / n) * 0.4) / 14.0;

        if (manosCompu > manosJugador) f += 0.2;
        else if (manosJugador > manosCompu) f -= 0.2;

        return Math.max(0, Math.min(1, f));
    }

    private boolean computadoraQuiereCantar() {
        double f = fuerzaComputadora();
        int nuevo = nivelTruco + 1;

        double exigencia = 0.75 + 0.05 * nivelTruco;
        if (f >= exigencia && azar.nextDouble() < 0.6) return true;

        double probFarol = 0.10 / nuevo;
        if (computadora.getPuntos() + 3 <= jugador.getPuntos()) {
            probFarol *= 2;
        }
        return f < 0.45 && azar.nextDouble() < probFarol;
    }

    private int respuestaComputadora(int nuevo) {
        double f = fuerzaComputadora() + (azar.nextDouble() - 0.5) * 0.2;
        double minimo = 0.25 + 0.15 * nuevo;
        boolean puedeSubir = nuevo < 3;

        if (puedeSubir && f >= 0.80) return SUBO;
        if (f >= minimo) return QUIERO;

        double r = azar.nextDouble();
        if (puedeSubir && r < 0.08) return SUBO;
        if (r < 0.20) return QUIERO;
        return NO_QUIERO;
    }

    private int elegirCarta(Carta rival, int mano) {
        int n = computadora.cantidadCartas();
        int menor = 0;
        int mayor = 0;
        for (int i = 1; i < n; i++) {
            int v = Truco.valorCarta(computadora.verCarta(i));
            if (v < Truco.valorCarta(computadora.verCarta(menor))) menor = i;
            if (v > Truco.valorCarta(computadora.verCarta(mayor))) mayor = i;
        }

        if (rival != null) {
            int vRival = Truco.valorCarta(rival);
            int ganadora = -1;
            int igual = -1;
            for (int i = 0; i < n; i++) {
                int v = Truco.valorCarta(computadora.verCarta(i));
                if (v > vRival && (ganadora == -1
                        || v < Truco.valorCarta(computadora.verCarta(ganadora)))) {
                    ganadora = i;
                }
                if (v == vRival) igual = i;
            }
            if (ganadora != -1) return ganadora;
            if (igual != -1 && mano == 1) return igual;
            return menor;
        }

        if (mano == 1 && n == 3 && azar.nextInt(100) < 50) {
            for (int i = 0; i < n; i++) {
                if (i != menor && i != mayor) return i;
            }
        }
        return mayor;
    }

    // ================= AUXILIARES =================

    private int valorRonda() {
        return nivelTruco == 0 ? 1 : nivelTruco + 1;
    }

    // Lo que le falta al que va ganando para llegar al final
    private int valorFalta() {
        int mayor = Math.max(jugador.getPuntos(), computadora.getPuntos());
        return Math.max(1, puntosParaGanar - mayor);
    }

    // Si el envido dejó a alguien en el puntaje final, se corta la ronda
    private void chequearFin() {
        if (jugador.getPuntos() >= puntosParaGanar
                || computadora.getPuntos() >= puntosParaGanar) {
            rondaTerminada = true;
        }
    }

    private String nombreCanto(int nivel) {
        switch (nivel) {
            case 1: return "Truco";
            case 2: return "Retruco";
            case 3: return "Vale cuatro";
            default: return "";
        }
    }

    private String nombreEnvido(int tipo) {
        switch (tipo) {
            case ENVIDO: return "Envido";
            case REAL_ENVIDO: return "Real Envido";
            case FALTA_ENVIDO: return "Falta Envido";
            default: return "";
        }
    }

    private int leerEntero() {
        while (!entrada.hasNextInt()) {
            entrada.next();
            System.out.print("Ingresá un número: ");
        }
        return entrada.nextInt();
    }

    private void vaciarManos() {
        while (jugador.cantidadCartas() > 0) jugador.jugarCarta(0);
        while (computadora.cantidadCartas() > 0) computadora.jugarCarta(0);
    }

    private void mostrarMarcador() {
        System.out.println("\n==== MARCADOR ====");
        System.out.println("Jugador: " + jugador.getPuntos());
        System.out.println("Computadora: " + computadora.getPuntos());
        System.out.println("==================");
    }
}