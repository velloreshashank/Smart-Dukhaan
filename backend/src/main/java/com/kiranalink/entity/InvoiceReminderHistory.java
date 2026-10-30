package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_reminder_history")
public class InvoiceReminderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(nullable = false)
    private String channel;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    @JsonProperty("message_content")
    private String messageContent;

    @Column(name = "delivery_status", nullable = false)
    @JsonProperty("delivery_status")
    private String deliveryStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public InvoiceReminderHistory() {}

    public InvoiceReminderHistory(Invoice invoice, String channel, String messageContent, String deliveryStatus) {
        this.invoice = invoice;
        this.channel = channel;
        this.messageContent = messageContent;
        this.deliveryStatus = deliveryStatus;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
