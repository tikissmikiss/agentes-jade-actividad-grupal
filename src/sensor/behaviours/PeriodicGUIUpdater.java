package sensor.behaviours;

import jade.core.behaviours.TickerBehaviour;
import sensor.SensorTemperatura;

/**
 * Actualiza la interfaz gráfica cada cierto tiempo
 * TODO: Se puede aprovechar para enviar datos cuando actuadorAID != null
 */
public final class PeriodicGUIUpdater extends TickerBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final SensorTemperatura sensor;

    public PeriodicGUIUpdater(SensorTemperatura sensor, long period) {
        super(sensor, period);
        this.sensor = sensor;
    }

    @Override
    public void onTick() {
        this.sensor.gui.update();
    }
}
