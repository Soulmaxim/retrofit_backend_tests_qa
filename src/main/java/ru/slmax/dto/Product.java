package ru.slmax.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@ToString
public class Product {
    private Integer id;
    private String title;
    private Integer price;
    private String categoryTitle;
}
