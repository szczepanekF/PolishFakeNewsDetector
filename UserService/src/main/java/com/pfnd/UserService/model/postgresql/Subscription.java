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
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne          //TODO verify if that works XD
    private User user;
    private Date startDate;
    private Date expirationDate;
    private String subscriptionType; //TODO implement enum type for subscriptions and add custom authentication for them
    private String pfndApiKey;
}
