package demo.tracing;

import io.jaegertracing.spi.Sender;

public class HTTPSenderConfiguration extends io.jaegertracing.Configuration.SenderConfiguration {

    private final String host;
    private final Integer port;

    public HTTPSenderConfiguration(String host, Integer port) {
        super();
        this.host = host;
        this.port = port;
    }

    @Override
    public Sender getSender() {
        return new JaegerHttpSender(host, port);
    }
}
