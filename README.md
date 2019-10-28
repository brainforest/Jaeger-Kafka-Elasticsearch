# Jaeger-Kafka-Elasticsearch

This is demo for sending Jaeger trace spans into Kafka or directly into Jaeger Collector.

Step:

1- download and setup Elasticsearch and Kibana and simple run them.
2- download jaeger and unzip 
3- Jaeger can store data into elasticsearch or kafka. TraceProducer has methods for 
```java
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
                                .withFlushInterval(1000)
                                .withMaxQueueSize(10000)
                                .withSender(
                          //              new KafkaSenderConfiguration(bootstrapServers, tracingTopic)
                                        new HTTPSenderConfiguration("http://localhost",14268)
                                ))
                .getTracer();
    }
```
Based on which store you choose (default it is HTTPSender)

4- To start querying from elasticsearch 
   
    ./jaeger-query --span-storage.type elasticsearch

5- Run jeager collector to get spans and store into elasticsearch

    ./jaeger-collector --span-storage.type elasticsearch 
    
6- Run TraceProducer and hit http://localhost:8080/checkout

note : if you want to use Kafka, you need to download Kafka and run it. Change Trace config into Kafka. In that case, jaeger-collector will take spans from KafkaReader and save in memory. Just simple, run jeager-all-in-one instead of jaeger-collector or query.
