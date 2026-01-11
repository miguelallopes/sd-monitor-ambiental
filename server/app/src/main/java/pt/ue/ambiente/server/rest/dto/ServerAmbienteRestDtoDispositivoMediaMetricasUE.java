package pt.ue.ambiente.server.rest.dto;

public class ServerAmbienteRestDtoDispositivoMediaMetricasUE {
    private int temperaturaMedia;
    private int humidadeMedia;

    public ServerAmbienteRestDtoDispositivoMediaMetricasUE(int temperaturaMedia, int humidadeMedia) {
        this.temperaturaMedia = temperaturaMedia;
        this.humidadeMedia = humidadeMedia;
    }

    public int getTemperaturaMedia() {
        return temperaturaMedia;
    }

    public int getHumidadeMedia() {
        return humidadeMedia;
    }
}
