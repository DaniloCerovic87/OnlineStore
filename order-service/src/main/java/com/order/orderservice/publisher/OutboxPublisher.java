package com.order.orderservice.publisher;

import com.order.orderservice.event.kafka.OrderPlacedEvent;
import com.order.orderservice.event.kafka.OutboxEvent;
import com.order.orderservice.repository.OutboxEventRepository;
import com.order.orderservice.util.AvroJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

  private static final String TOPIC = "order-placed";
  private static final int MAX_ATTEMPTS = 10;

  @Scheduled(fixedDelayString = "${outbox.publisher.delay:500}")
  public void publishBatch() {
    var batch = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc((OutboxEvent.Status.NEW));
    for (var e : batch) {
      try {
        OrderPlacedEvent placedEvent = AvroJsonUtil.fromJson(e.getPayload(), OrderPlacedEvent.getClassSchema());
        kafkaTemplate.send(TOPIC, placedEvent).get();

        e.setStatus(OutboxEvent.Status.SENT);
        e.setSentAt(Instant.now());
        outboxEventRepository.save(e);

      } catch (Exception ex) {
        log.warn("Outbox publish failed id={} attempt={} : {}", e.getId(), e.getAttempts(), ex.toString());
        e.setAttempts(e.getAttempts() + 1);
        if (e.getAttempts() >= MAX_ATTEMPTS) {
          e.setStatus(OutboxEvent.Status.FAILED);
        }
        e.setLastAttemptAt(Instant.now());
        outboxEventRepository.save(e);
      }
    }
  }
}
