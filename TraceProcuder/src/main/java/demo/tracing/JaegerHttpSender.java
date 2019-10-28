/*
 * Copyright (c) 2016, Uber Technologies, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package demo.tracing;

import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Batch;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import okhttp3.*;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class JaegerHttpSender extends ThriftSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaegerHttpSender.class);
    private static final String HTTP_COLLECTOR_JAEGER_THRIFT_FORMAT_PARAM = "format=jaeger.thrift";
    private static final MediaType MEDIA_TYPE_THRIFT = MediaType.parse("application/x-thrift");
    private static final int ONE_MB_IN_BYTES = 1048576;

    private final Request.Builder requestBuilder;
    private final OkHttpClient httpClient;
    private final TDeserializer deserializer;

    public JaegerHttpSender(String jaegerHost, Integer jaegerPort) {

        super(ProtocolType.Binary, ONE_MB_IN_BYTES);

        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

        String url = String.format("%s:%s/api/traces", jaegerHost, jaegerPort);
        HttpUrl collectorUrl = HttpUrl.parse(String.format("%s?%s", url, HTTP_COLLECTOR_JAEGER_THRIFT_FORMAT_PARAM));
        if (collectorUrl == null) {
            throw new IllegalArgumentException("Could not parse url");
        }

        this.requestBuilder = new Request.Builder().url(collectorUrl);
        this.httpClient = new OkHttpClient.Builder().build();
        this.deserializer = new TDeserializer(protocolFactory);
    }

    private Batch deserialize(byte[] message) throws SenderException {
        Batch batch = new Batch();
        try {
            deserializer.deserialize(batch, message);
        } catch (Exception e) {
            throw new SenderException("Failed to deserialize message", e, 1);
        }
        return batch;
    }

    private int countSpans(Batch batch) {
        if (batch == null || batch.getSpans() == null) {
            return 0;
        }
        return batch.getSpans().size();
    }

    @Override
    public void send(Process process, List<Span> spans) throws SenderException {
        Batch batch = new Batch(process, spans);
        int size = countSpans(batch);
        LOGGER.info("sending {} span(s) to jaeger", size);

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_THRIFT, serialize(batch));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Request request = requestBuilder.post(requestBody).build();

        Response response;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new SenderException(String.format("Could not send %d spans", size), e, size);
        }

        if (response.isSuccessful()) {
            response.close();
            return;
        }

        String responseBody;
        try {
            responseBody = response.body() != null ? response.body().string() : "null";
        } catch (IOException e) {
            responseBody = "unable to read response";
        }
        String exceptionMessage = String.format("Could not send %d spans, response %d: %s", size, response.code(), responseBody);
        throw new SenderException(exceptionMessage, null, size);
    }
}
