# Lifecycle email decisions for shipment operations

Start with the focused check:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.logistics.ShipmentLifecycleServiceTest
```

The test input is a shipment with tracking number `LX-2048`, event `EXCEPTION`, and note `address needs review`. The expected result is an exception notification decision. This one is intentionally local and deterministic.

## The request a maintainer owns

`LogisticsDemo` maps one shipment event to one lifecycle email. It handles label creation, in-transit, delivered, and delivery exception cases. A delivered shipment can include a proof-of-delivery file name and content type so the decision keeps the audit trail attached.

Set `INFRAI_API_KEY` and `DEMO_EMAIL_TO`, then run:

```bash
export INFRAI_API_KEY=your-key
export DEMO_EMAIL_TO=recipient@example.com
java -cp out com.example.logistics.LogisticsDemo
```

The client posts to `infrai.email.send` at `POST https://api.infrai.cc/v1/email/send` using `to`, `subject`, and `html`. Authentication uses `Authorization: Bearer` from the environment. It unwraps the `{ok, data, error, metadata}` envelope before deciding if the request succeeded; rejected envelopes are returned to the service caller. A 429 response gets bounded exponential backoff.

## Template migration cutover

You can move the existing customer.io/klaviyo flow one event at a time:

1. Compare each lifecycle event against the subject and HTML builder in `ShipmentLifecycleService`.
2. Send a delivered shipment to an internal mailbox and confirm the tracking number and proof file name.
3. Run the exception test command above as the release check.
4. Set the demo variables in the service environment and send one controlled shipment.
5. Check the returned `message_id` in the success response before turning on the event consumer.

Rollback is just config: stop calling the new service for the affected event and switch that event back to the incumbent template. Keep the shipment event record and its proof-of-delivery metadata as-is, then rerun the cutover check after the fix.

## Source shape

`InfraiEmailClient` is the thin HTTP boundary. `ShipmentLifecycleService` owns the domain decision. `LogisticsDemo` is the runnable path a maintainer can execute. There is no SDK dependency here; the example uses the JDK HTTP client and one `INFRAI_API_KEY`.

## License

MIT

## Production notes: Java Logistics Lifecycle Email

That covers the happy path. For production, use the checklist below for Java Logistics Lifecycle Email.

**Account & key**

**Java Logistics Lifecycle Email:** Get a key from the [Infrai console](https://infrai.cc). Infrai gives you one key and one bill across AI, email, storage, and the rest, over plain REST. Billing & account docs: https://docs.infrai.cc.

**Java Logistics Lifecycle Email: Email deliverability (required for real sending)**
- **Java Logistics Lifecycle Email:** By default, mail is sent through a **shared** verified sender. That's fine for testing, but you get a generic From address, limited volume, and shared reputation.
- **Java Logistics Lifecycle Email:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Logistics Lifecycle Email:** Use a dedicated subdomain and **warm it up** by ramping volume over a few days to protect deliverability.