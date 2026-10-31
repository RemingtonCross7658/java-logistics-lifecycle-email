package com.example.logistics;

import java.net.http.HttpClient;

public final class LogisticsDemo {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        String recipient = System.getenv("DEMO_EMAIL_TO");
        if (key == null || key.isBlank() || recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY and DEMO_EMAIL_TO are required");
        }
        var client = new InfraiEmailClient(HttpClient.newHttpClient(), key);
        var service = new ShipmentLifecycleService(client);
        var shipment = new ShipmentLifecycleService.Shipment(
                "LX-2048", recipient, ShipmentLifecycleService.Event.DELIVERED,
                new ShipmentLifecycleService.ProofOfDelivery("pod-LX-2048.pdf", "application/pdf"), "");
        System.out.println(service.notify(shipment));
    }
}
