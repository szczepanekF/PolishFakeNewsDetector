package com.pfnd.UserService.model.postgresql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String method;
    private String currency;
    private float value;
    private Date startDate;
    private Date expirationDate;
    private String status;
    @ManyToOne
    private Subscription subscription;
    private String paymentApiKey;
}
