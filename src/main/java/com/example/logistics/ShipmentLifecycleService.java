package com.example.logistics;

import java.io.IOException;

public final class ShipmentLifecycleService {
    public enum Event { LABEL_CREATED, IN_TRANSIT, DELIVERED, EXCEPTION }

    public record ProofOfDelivery(String fileName, String contentType) {}
    public record Shipment(String trackingNumber, String recipient, Event event, ProofOfDelivery proof,
                           String exceptionNote) {}

    private final InfraiEmailClient email;

    public ShipmentLifecycleService(InfraiEmailClient email) {
        this.email = email;
    }

    public String notify(Shipment shipment) throws IOException, InterruptedException {
        return email.send(shipment.recipient(), subject(shipment), html(shipment));
    }

    private static String subject(Shipment s) {
        return switch (s.event()) {
            case LABEL_CREATED -> "Shipment label created: " + s.trackingNumber();
            case IN_TRANSIT -> "Shipment in transit: " + s.trackingNumber();
            case DELIVERED -> "Shipment delivered: " + s.trackingNumber();
            case EXCEPTION -> "Shipment exception: " + s.trackingNumber();
        };
    }

    private static String html(Shipment s) {
        String detail = switch (s.event()) {
            case LABEL_CREATED -> "The carrier label is ready.";
            case IN_TRANSIT -> "The parcel is moving through the carrier network.";
            case DELIVERED -> s.proof() == null ? "Delivery was recorded." :
                    "Delivery was recorded. Proof file: " + s.proof().fileName();
            case EXCEPTION -> "Please review the delivery exception: " + s.exceptionNote();
        };
        return "<p>Tracking " + s.trackingNumber() + ": " + detail + "</p>";
    }
}
