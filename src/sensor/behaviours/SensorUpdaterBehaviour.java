package sensor.behaviours;

import jade.core.behaviours.TickerBehaviour;
import sensor.SensorTemperatura;

/**
 * Se ejecuta periodicamente cada un tiempo predefinido
 */
final class SensorUpdaterBehaviour extends TickerBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final SensorTemperatura sensor;

    public SensorUpdaterBehaviour(SensorTemperatura sensor, long period) {
        super(sensor, period);
        this.sensor = sensor;
    }

    @Override
    protected void onTick() {
        this.sensor.logger.info("Agente " + myAgent.getLocalName() + ": Actualizando datos...");
        this.sensor.sendDataToActuador();
    }
}
