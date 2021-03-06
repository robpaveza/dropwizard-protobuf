package io.dropwizard.jersey.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

/**
 * A Jersey provider which enables using Protocol Buffers to parse request
 * entities into objects and generate response entities from objects.
 */
@Provider
@Consumes(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
@Produces(ProtocolBufferMediaType.APPLICATION_PROTOBUF_AND_TEXT_FORMAT)
public class ProtocolBufferMessageBodyProvider implements MessageBodyReader<Message>,
        MessageBodyWriter<Message> {

    private final static String UTF8_ENCODING = "UTF-8";

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    public Message readFrom(final Class<Message> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream) throws IOException {

        try {
            final Method newBuilder = type.getMethod("newBuilder");
            final Message.Builder builder = (Message.Builder)newBuilder.invoke(type);
            return builder.mergeFrom(entityStream).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public long getSize(final Message m, final Class<?> type,
            final Type genericType, final Annotation[] annotations,
            final MediaType mediaType) {
        try {
            if (mediaType.getSubtype().indexOf("text-format") > -1) {
                String formatted = TextFormat.printToUnicodeString(m);
                return formatted.getBytes(UTF8_ENCODING).length;
            }
        }
        catch (UnsupportedEncodingException uee) {

        }

        return m.getSerializedSize();
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(final Message m, final Class<?> type,
            final Type genericType, final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException {
        
        if (mediaType.getSubtype().indexOf("text-format") > -1) {
            entityStream.write(m.toString().getBytes(UTF8_ENCODING));
        } else {
            entityStream.write(m.toByteArray());
        }
    }
}
