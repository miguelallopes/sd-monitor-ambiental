package pt.ue.ambiente.client.rest.sensor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import pt.ue.ambiente.client.rest.message.AmbienteMessagePublish;

public class ClientAmbienteSensorUE implements Runnable {

    public static final float TEMPERATURA_MAXIMA = 30.0f;
    public static final float TEMPERATURA_MINIMA = 15.0f;
    public static final int HUMIDADE_MAXIMA = 80;
    public static final int HUMIDADE_MINIMA = 30;

    public static final int MILISEGUNDOS_ENTRE_LEITURAS = 5000; // 5 segundos

    private final BlockingQueue<AmbienteMessagePublish> queueLeiturasAmbiente;

    private float ultimaTemperaturaLida;
    private int ultimaHumidadeLida;

    private final int idDispositivo;

    private final Random random = new Random();

    public ClientAmbienteSensorUE(int idDispositivo, BlockingQueue<AmbienteMessagePublish> queueLeiturasAmbiente) {
        if (idDispositivo <= 0) {
            throw new IllegalArgumentException("idDispositivo inválido (deve ser >= 1)");
        }

        if (queueLeiturasAmbiente == null) {
            throw new IllegalArgumentException("queueLeiturasAmbiente inválida (nula)");
        }

        this.idDispositivo = idDispositivo;
        this.queueLeiturasAmbiente = queueLeiturasAmbiente;

        // Temperatura entre 15 e 30
        ultimaTemperaturaLida = TEMPERATURA_MINIMA + (TEMPERATURA_MAXIMA - TEMPERATURA_MINIMA) * random.nextFloat();

        // Humidade entre 30 e 80
        ultimaHumidadeLida = HUMIDADE_MINIMA + random.nextInt(HUMIDADE_MAXIMA - HUMIDADE_MINIMA + 1);
    }

    public ClientAmbienteSensorUE(int idDispositivo) {
        this(idDispositivo, new LinkedBlockingQueue<>());
    }

    public int getIdDispositivo() {
        return this.idDispositivo;
    }

    public BlockingQueue<AmbienteMessagePublish> obterQueueLeiturasAmbiente() {
        return this.queueLeiturasAmbiente;
    }

    private float lerTemperatura() {
        // Variação entre -1 e +1
        float variacaoTemperatura = (random.nextFloat() * 2.0f) - 1.0f;

        // Garantir temperatura entre 15 e 30
        ultimaTemperaturaLida += variacaoTemperatura;
        if (ultimaTemperaturaLida < TEMPERATURA_MINIMA) {
            ultimaTemperaturaLida = TEMPERATURA_MINIMA;
        } else if (ultimaTemperaturaLida > TEMPERATURA_MAXIMA) {
            ultimaTemperaturaLida = TEMPERATURA_MAXIMA;
        }

        // Arredondar a duas casas décimais
        ultimaTemperaturaLida = new BigDecimal(ultimaTemperaturaLida).setScale(2, RoundingMode.HALF_EVEN).floatValue();

        return ultimaTemperaturaLida;
    }

    private int lerHumidade() {
        // Variação entre -1,0 ou +1
        int variacaoHumidade = random.nextInt(3) - 1;

        // Garantir humidade entre 0 e 100
        ultimaHumidadeLida += variacaoHumidade;
        if (ultimaHumidadeLida < HUMIDADE_MINIMA) {
            ultimaHumidadeLida = HUMIDADE_MINIMA;
        } else if (ultimaHumidadeLida > HUMIDADE_MAXIMA) {
            ultimaHumidadeLida = HUMIDADE_MAXIMA;
        }

        return ultimaHumidadeLida;
    }

    public synchronized AmbienteMessagePublish lerAmbiente() {
        float temperatura = lerTemperatura();
        int humidade = lerHumidade();
        String timestamp = OffsetDateTime.now().toString();

        return new AmbienteMessagePublish(this.idDispositivo, temperatura, humidade, timestamp);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                AmbienteMessagePublish leitura = this.lerAmbiente();
                queueLeiturasAmbiente.put(leitura);
                Thread.sleep(MILISEGUNDOS_ENTRE_LEITURAS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
