package com.order.orderservice.util;

public final class AvroJsonUtil {
  public static <T extends org.apache.avro.specific.SpecificRecord> String toJson(T record) {
    try (var out = new java.io.ByteArrayOutputStream()) {
      var writer = new org.apache.avro.specific.SpecificDatumWriter<T>(record.getSchema());
      var enc = org.apache.avro.io.EncoderFactory.get().jsonEncoder(record.getSchema(), out);
      writer.write(record, enc);
      enc.flush();
      return out.toString(java.nio.charset.StandardCharsets.UTF_8);
    } catch (java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }

  public static <T extends org.apache.avro.specific.SpecificRecord> T fromJson(
          String json, org.apache.avro.Schema schema) {
    try {
      var dec = org.apache.avro.io.DecoderFactory.get().jsonDecoder(schema, json);
      var reader = new org.apache.avro.specific.SpecificDatumReader<T>(schema);
      return reader.read(null, dec);
    } catch (java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
