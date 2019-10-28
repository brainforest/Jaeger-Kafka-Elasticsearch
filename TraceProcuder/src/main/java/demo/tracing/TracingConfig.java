package demo.tracing;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class TracingConfig {


    private String tracingTopic = "tracing-topic";
    private String applicationName = "rest-app";
    private String bootstrapServers="localhost:9092";

/*
    @Bean(name = "Jeager")
    public Tracer tracerJaeger() {
        io.jaegertracing.Configuration.SamplerConfiguration samplerConfiguration = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1);
        io.jaegertracing.Configuration.ReporterConfiguration reporterConfiguration = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        io.jaegertracing.Configuration entry = new io.jaegertracing.Configuration("entry").withSampler(samplerConfiguration).withReporter(reporterConfiguration);
        return  entry.getTracer();
    }*/

    @Bean
    public Tracer tracer() {
        return io.jaegertracing.Configuration.fromEnv(applicationName)
                .withSampler(
                        io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
                                .withType(ConstSampler.TYPE)
                                .withParam(1))
                .withReporter(
                        io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
                                .withLogSpans(true)
                           //     .withFlushInterval(1000)
                           //     .withMaxQueueSize(10000)
                                .withSender(
                          //              new KafkaSenderConfiguration(bootstrapServers, tracingTopic)
                                        new HTTPSenderConfiguration("http://localhost",14268)
                                ))
                .getTracer();
    }

    @PostConstruct
    public void registerToGlobalTracer() {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer());
        }
    }
}
