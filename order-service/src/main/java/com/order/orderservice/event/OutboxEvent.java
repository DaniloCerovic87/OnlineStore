package com.order.orderservice.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "order_outbox")
public class OutboxEvent {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String aggregateType;

  private String aggregateId;

  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  private String payload;

  @Enumerated(EnumType.STRING)
  private Status status = Status.NEW;

  private int attempts = 0;

  private Instant createdAt = Instant.now();

  private Instant lastAttemptAt;

  private Instant sentAt;

  public enum Status { NEW, SENT, FAILED }
}
