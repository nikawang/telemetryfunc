package org.nika.wang;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */

    private enum status {COOL, HOT, WARM}

    class TelemetryItem {

        private double temperature;

        private status temperatureStatus;

        private boolean isNormalPressure;
        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        private double pressure;

        public TelemetryItem(double temperature, double pressure) {
            this.temperature = temperature;
            this.pressure = pressure;
        }

        public void setNormalPressure(boolean status)
        {
            isNormalPressure = status;
        }


        public void setTemperatureStatus(status status)
        {
            temperatureStatus = status;
        }
    }

    @FunctionName("generateSensorData")
    @EventHubOutput(
            name = "event",
            eventHubName = "", // blank because the value is included in the connection string
            connection = "EventHubConnectionString")
    public TelemetryItem generateSensorData(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "*/10 * * * * *")
                    String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Java Timer trigger function executed at: "
                + java.time.LocalDateTime.now());
        double temperature = Math.random() * 100;
        double pressure = Math.random() * 50;
        return new TelemetryItem(temperature, pressure);
    }

    @FunctionName("processSensorData")
    public void processSensorData(
            @EventHubTrigger(
                    name = "msg",
                    eventHubName = "", // blank because the value is included in the connection string
                    cardinality = Cardinality.ONE,
                    connection = "EventHubConnectionString")
                    TelemetryItem item,
            @CosmosDBOutput(
                    name = "databaseOutput",
                    databaseName = "TelemetryDb",
                    collectionName = "TelemetryInfo",
                    connectionStringSetting = "CosmosDBConnectionString")
                    OutputBinding<TelemetryItem> document,
            final ExecutionContext context) {

        context.getLogger().info("Event hub message received: " + item.toString());

        if (item.getPressure() > 30) {
            item.setNormalPressure(false);
        } else {
            item.setNormalPressure(true);
        }

        if (item.getTemperature() < 40) {
            item.setTemperatureStatus(status.COOL);
        } else if (item.getTemperature() > 90) {
            item.setTemperatureStatus(status.HOT);
        } else {
            item.setTemperatureStatus(status.WARM);
        }

        document.setValue(item);
    }
}
