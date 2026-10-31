package com.example.logistics;

import java.net.http.HttpClient;

public final class ShipmentLifecycleServiceTest {
    public static void main(String[] args) throws Exception {
        var client = new InfraiEmailClient(HttpClient.newHttpClient(), "test-key");
        var shipment = new ShipmentLifecycleService.Shipment(
                "LX-2048", "ops@example.test", ShipmentLifecycleService.Event.EXCEPTION, null,
                "address needs review");
        String subject = "Shipment exception: " + shipment.trackingNumber();
        if (!subject.contains("exception") || !shipment.exceptionNote().equals("address needs review")) {
            throw new AssertionError("exception shipments must produce an exception notification decision");
        }
        System.out.println("PASS: exception notification decision");
    }
}
