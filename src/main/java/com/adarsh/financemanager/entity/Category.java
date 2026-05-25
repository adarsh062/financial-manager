package com.adarsh.financemanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    /**
     * false = system default (cannot be deleted)
     * true  = user-created custom category
     */
    @Column(nullable = false)
    private boolean isCustom;

    /**
     * Soft-delete flag — custom categories are marked deleted rather than removed.
     */
    @Column(nullable = false)
    private boolean isDeleted = false;

    /**
     * null for default categories, set for custom categories.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
